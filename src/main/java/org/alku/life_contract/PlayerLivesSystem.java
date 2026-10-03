package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.alku.life_contract.compat.CaerulaArborCompat;
import org.alku.life_contract.events.GameEventManager;

/**
 * 玩家命数系统：
 * <ul>
 *     <li>对局开始时每名参赛玩家默认 5 条命</li>
 *     <li><b>只要还有命，致命伤害不会致死</b>：伤害被抵消，立刻回满血、
 *         获得 5 秒抗性提升 IV，并扣 1 条命（"浴血重生"）</li>
 *     <li>命数归零后再受到致命伤害才会真正死亡，并直接进入旁观模式出局</li>
 * </ul>
 * 无法被"浴血重生"拦下的伤害（虚空、<code>/kill</code> 等
 * {@link DamageTypeTags#BYPASSES_INVULNERABILITY} 伤害）仍按普通死亡处理：
 * 扣 1 条命并正常重生，扣到 0 时出局。
 * <p>
 * 命数记录在本模组自己的玩家持久化数据里，不依赖 caerula_arbor；
 * 该模组存在时会同步写入它的 <code>player_lives</code>，让既有的 LP 名牌显示保持一致。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerLivesSystem {

    /** 每名参赛玩家的默认命数。 */
    public static final int DEFAULT_LIVES = 5;

    /** 浴血重生后获得的抗性提升：5 秒、IV 级。 */
    private static final int REVIVE_RESISTANCE_TICKS = 5 * 20;
    private static final int REVIVE_RESISTANCE_AMPLIFIER = 3;

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

    // ==================== 浴血重生 ====================

    /**
     * 致命伤害拦截。
     * <p>
     * 这里用的是 {@link LivingDamageEvent.Pre}，它的伤害值已经过护甲、附魔与
     * 饰品减免，因此判定"这一下会不会打死人"是准确的；把伤害改成 0 之后
     * 原版流程不会扣血、也不会触发死亡，玩家原地满血复活。
     */
    @SubscribeEvent
    public static void onLethalDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isInGame(player)) {
            return;
        }
        if (!isTracked(player)) {
            return;
        }
        // 虚空、/kill 一类无视无敌帧的伤害不吃这一套，避免在虚空里反复烧命
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        int lives = getLives(player);
        if (lives <= 0) {
            // 已经没有命了，正常死亡 -> 出局
            return;
        }

        // 护甲之后的最终伤害；伤害吸收要先替玩家挡掉一部分
        float incoming = event.getNewDamage();
        float survivable = player.getHealth() + player.getAbsorptionAmount();
        if (incoming + 1.0E-4F < survivable) {
            return;
        }

        event.setNewDamage(0.0F);
        reviveWithFullHealth(player, lives - 1);
    }

    /** 扣 1 条命、回满血、给 5 秒抗性提升 IV，并播报反馈。 */
    private static void reviveWithFullHealth(ServerPlayer player, int remaining) {
        setLives(player, remaining);

        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                REVIVE_RESISTANCE_TICKS, REVIVE_RESISTANCE_AMPLIFIER, false, true, true));

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    40, 0.6D, 0.8D, 0.6D, 0.35D);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        player.sendSystemMessage(Component.literal(remaining > 0
                ? "§c[浴血重生] §f致命伤害被抵消，你回到了满血状态，还剩 §c" + remaining + " §f条命。"
                : "§c[浴血重生] §f致命伤害被抵消，你回到了满血状态。§4这是你最后一条命了！"));
    }

    private static boolean isInGame(ServerPlayer player) {
        return GameEventManager.isGameActive() && GameEventManager.isPlayerPartOfGame(player.getUUID());
    }

    // ==================== 真正的死亡 ====================

    /** 死亡扣命：只有"浴血重生"拦不下的伤害（虚空、/kill 等）会走到这里。 */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isInGame(player)) return;
        if (!isTracked(player)) return;

        int current = getLives(player);
        if (current <= 0) {
            // 已经没有命了，直接出局
            eliminate(player);
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
        if (!isInGame(player)) return;
        if (isTracked(player) && getLives(player) <= 0) {
            player.setGameMode(GameType.SPECTATOR);
        }
    }
}
