package org.alku.life_contract.market;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.alku.life_contract.Life_contract;

/**
 * 升华的发放口。
 * <p>
 * 所有"白给"的升华都从这里走，好处是：数量、提示文案、发放方式只有一份实现，
 * 以后要调经济只要改这一处 + 各个调用点的数量。
 * <p>
 * 现有来源（与玩法绑定，不是凭空加的系统）：
 * <ul>
 *     <li>击杀生物：由 {@code SublimationDropHandler} 掉落（原有机制）；</li>
 *     <li>击杀玩家、全场首杀：见 {@link #awardPlayerKill} / {@link #awardFirstBlood}；</li>
 *     <li>存活里程碑（每 3 分钟）与每次缩圈：给还在场上的玩家；</li>
 *     <li>事件（孢潮推进）开场补给。</li>
 * </ul>
 */
public final class SublimationRewards {

    private SublimationRewards() {
    }

    /** 给单个玩家发升华；背包放不下就掉在脚下，绝不凭空吞掉。 */
    public static void award(ServerPlayer player, int amount) {
        award(player, amount, null);
    }

    /**
     * @param reasonKey 语言键，非空时会在聊天栏提示一句；null 表示静默发放（例如击杀掉落已经在物品里体现）
     */
    public static void award(ServerPlayer player, int amount, String reasonKey) {
        if (player == null || amount <= 0 || player.hasDisconnected()) {
            return;
        }

        int remaining = amount;
        int max = Math.max(1, Life_contract.SUBLIMATION.get().getDefaultMaxStackSize());
        while (remaining > 0) {
            int chunk = Math.min(remaining, max);
            ItemStack stack = new ItemStack(Life_contract.SUBLIMATION.get(), chunk);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= chunk;
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();

        if (reasonKey != null) {
            player.sendSystemMessage(Component.translatable("gui.life_contract.shop.reward",
                    Component.translatable(reasonKey), amount));
        }
    }

    /** 给所有"还在场上"的玩家发（旁观/创造不参与，与事件系统口径一致）。 */
    public static void awardAll(MinecraftServer server, int amount, String reasonKey) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }
            award(player, amount, reasonKey);
        }
    }

    /** 击杀玩家奖励。 */
    public static void awardPlayerKill(ServerPlayer killer) {
        award(killer, PLAYER_KILL_REWARD, "gui.life_contract.shop.reason.player_kill");
    }

    /** 全场首杀奖励（每局一次）。 */
    public static void awardFirstBlood(ServerPlayer killer) {
        award(killer, FIRST_BLOOD_REWARD, "gui.life_contract.shop.reason.first_blood");
    }

    public static final int PLAYER_KILL_REWARD = 15;
    public static final int FIRST_BLOOD_REWARD = 20;
    /** 存活里程碑：每 3 分钟一次。 */
    public static final int SURVIVAL_REWARD = 6;
    public static final int SURVIVAL_INTERVAL_SECONDS = 180;
    /** 每次缩圈的补给。 */
    public static final int SHRINK_REWARD = 8;
    /** 孢潮推进开场的补给。 */
    public static final int SPORE_SURGE_REWARD = 10;
}
