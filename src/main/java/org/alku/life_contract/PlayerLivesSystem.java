package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.alku.life_contract.compat.CaerulaArborCompat;
import org.alku.life_contract.events.GameEventManager;

/**
 * 玩家命数系统：
 * <ul>
 *     <li>对局开始时每名参赛玩家默认 5 条命</li>
 *     <li>每次死亡扣 1 条命</li>
 *     <li>命数只剩 1 条时再次死亡，直接进入旁观模式出局</li>
 * </ul>
 * 命数记录在本模组自己的玩家持久化数据里，不依赖 caerula_arbor；
 * 该模组存在时会同步写入它的 `player_lives`，让既有的 LP 名牌显示保持一致。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerLivesSystem {

    /** 每名参赛玩家的默认命数。 */
    public static final int DEFAULT_LIVES = 5;

    private static final String TAG_LIVES = "LifeContractLives";

    private PlayerLivesSystem() {
    }

    /** 开新一局或中途入队时把命数重置为默认值。 */
    public static void resetLives(ServerPlayer player) {
        setLives(player, DEFAULT_LIVES);
    }

    /** 读取命数，未登记（-1）表示该玩家不在对局中。 */
    public static int getLives(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(TAG_LIVES) ? data.getInt(TAG_LIVES) : -1;
    }

    public static boolean isTracked(ServerPlayer player) {
        return getLives(player) >= 0;
    }

    public static void setLives(ServerPlayer player, int lives) {
        int value = Math.max(0, lives);
        player.getPersistentData().putInt(TAG_LIVES, value);
        // caerula_arbor 存在时同步它的生命数字段（该字段最小为 1）
        CaerulaArborCompat.setLifePoints(player, Math.max(1, value));
    }

    /** 清除命数记录：对局结束后名牌回退到 caerula_arbor 自身的数值。 */
    public static void clearLives(ServerPlayer player) {
        player.getPersistentData().remove(TAG_LIVES);
    }

    /** 供名牌同步使用：本模组已登记的玩家用本模组的命数，否则回退到 caerula_arbor。 */
    public static int getLivesForDisplay(ServerPlayer player) {
        int lives = getLives(player);
        return lives >= 0 ? lives : CaerulaArborCompat.getLifePoints(player);
    }

    /** 死亡扣命：命数归零时进入旁观模式。 */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!GameEventManager.isGameActive() || !GameEventManager.isPlayerPartOfGame(player.getUUID())) return;
        if (!isTracked(player)) return;

        int current = getLives(player);
        if (current <= 0) {
            // 已经出局的玩家不再重复扣命
            return;
        }

        int remaining = current - 1;
        setLives(player, remaining);

        if (remaining <= 0) {
            eliminate(player);
        } else {
            player.sendSystemMessage(Component.literal("§e[生命] §f你还剩 §c" + remaining + " §f条命。"));
        }
    }

    /** 出局：直接进入旁观模式。 */
    private static void eliminate(ServerPlayer player) {
        player.setGameMode(GameType.SPECTATOR);
        player.sendSystemMessage(Component.literal("§c[出局] §f生命数已耗尽，你进入旁观模式。"));
        if (player.getServer() != null) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§c[出局] §f" + player.getName().getString() + " 生命数已耗尽，进入旁观模式。"), false);
        }
    }

    /** 复活的队友回到场上，并至少保留 1 条命。 */
    public static void reviveAsSurvivor(ServerPlayer player) {
        if (isTracked(player) && getLives(player) <= 0) {
            setLives(player, 1);
        }
        player.setGameMode(GameType.SURVIVAL);
    }

    /** 死亡重生会新建玩家实体，这里把命数带到新实体上。 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) return;
        if (!event.isWasDeath()) return;

        CompoundTag originalData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();
        if (originalData.contains(TAG_LIVES)) {
            newData.putInt(TAG_LIVES, originalData.getInt(TAG_LIVES));
        }
    }

    /** 命数已耗尽的玩家重生后必须保持旁观，避免复活后又能继续战斗。 */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!GameEventManager.isGameActive() || !GameEventManager.isPlayerPartOfGame(player.getUUID())) return;
        if (isTracked(player) && getLives(player) <= 0) {
            player.setGameMode(GameType.SPECTATOR);
        }
    }
}
