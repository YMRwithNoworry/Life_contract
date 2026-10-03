package org.alku.life_contract.accessory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.PlayerLivesSystem;

import java.util.EnumSet;

/**
 * 饰品效果的"情境"条件。
 * <p>
 * 一条效果可以声明 {@code when}，只有条件成立时才生效，让饰品"看场合"而不是常驻数值。
 * 每 10 tick 为每个玩家求值一次（{@link #evaluate}），结果放进 {@link EnumSet} 复用，
 * 避免对同一次扫描里的每条效果重复做实体检索。
 */
public enum AccessoryCondition {

    /** 无条件。 */
    ALWAYS("", ""),

    LOW_HEALTH("生命 ≤35%", "HP <= 35%"),
    MID_HEALTH("生命 ≤60%", "HP <= 60%"),
    FULL_HEALTH("满血", "Full HP"),
    HURT("生命 ≤70%", "HP <= 70%"),

    NIGHT("夜晚", "Night"),
    DAY("白天", "Day"),
    UNDERGROUND("地下（y<55）", "Underground (y<55)"),
    HIGH_ALTITUDE("高空（y>120）", "High altitude (y>120)"),
    LOW_LIGHT("光照 ≤7", "Light <= 7"),

    IN_WATER("水中", "In water"),
    IN_RAIN("雨中", "In rain"),
    BURNING("着火", "On fire"),
    SNEAKING("潜行", "Sneaking"),
    SPRINTING("疾跑", "Sprinting"),
    HUNGRY("饥饿 ≤6", "Hunger <= 6"),

    RECENT_KILL("10 秒内击杀过", "Killed within 10s"),
    LOW_LIVES("本队生命 ≤2", "Team lives <= 2"),
    LAST_STAND("队友全灭", "Last one standing"),
    OUTNUMBERED("被围（12 格内敌人多于队友）", "Outnumbered within 12 blocks"),
    NEAR_BORDER("距边界 ≤64 格", "Within 64 blocks of the border"),

    IN_NETHER("下界", "In the Nether"),
    IN_END("末地", "In the End");

    private static final double NEARBY_RADIUS = 12.0D;
    private static final double BORDER_ALERT_DISTANCE = 64.0D;
    private static final int RECENT_KILL_TICKS = 200;

    private final String zh;
    private final String en;

    AccessoryCondition(String zh, String en) {
        this.zh = zh;
        this.en = en;
    }

    public String displayZh() {
        return zh;
    }

    public String displayEn() {
        return en;
    }

    /** 求出该玩家当前满足的全部条件。 */
    public static EnumSet<AccessoryCondition> evaluate(ServerPlayer player) {
        EnumSet<AccessoryCondition> set = EnumSet.of(ALWAYS);

        float health = player.getHealth();
        float max = Math.max(1.0F, player.getMaxHealth());
        float ratio = health / max;
        if (ratio <= 0.35F) {
            set.add(LOW_HEALTH);
        }
        if (ratio <= 0.60F) {
            set.add(MID_HEALTH);
        }
        if (ratio <= 0.70F) {
            set.add(HURT);
        }
        if (health >= max - 0.01F) {
            set.add(FULL_HEALTH);
        }

        Level level = player.level();
        if (level.dimension() == Level.NETHER) {
            set.add(IN_NETHER);
        } else if (level.dimension() == Level.END) {
            set.add(IN_END);
        } else {
            long time = level.getDayTime() % 24000L;
            if (time < 12000L) {
                set.add(DAY);
            } else {
                set.add(NIGHT);
            }
        }

        double y = player.getY();
        if (y < 55.0D) {
            set.add(UNDERGROUND);
        }
        if (y > 120.0D) {
            set.add(HIGH_ALTITUDE);
        }

        BlockPos pos = player.blockPosition();
        if (level.getMaxLocalRawBrightness(pos) <= 7) {
            set.add(LOW_LIGHT);
        }
        if (player.isInWater()) {
            set.add(IN_WATER);
        }
        if (level.isRaining() && level.canSeeSky(pos)) {
            set.add(IN_RAIN);
        }
        if (player.isOnFire()) {
            set.add(BURNING);
        }
        if (player.isShiftKeyDown()) {
            set.add(SNEAKING);
        }
        if (player.isSprinting()) {
            set.add(SPRINTING);
        }
        if (player.getFoodData().getFoodLevel() <= 6) {
            set.add(HUNGRY);
        }

        int tick = player.getServer() == null ? 0 : player.getServer().getTickCount();
        if (tick - AccessoryState.lastKillTick(player) <= RECENT_KILL_TICKS) {
            set.add(RECENT_KILL);
        }

        if (PlayerLivesSystem.isTracked(player) && PlayerLivesSystem.getLives(player) <= 2) {
            set.add(LOW_LIVES);
        }

        if (player.getServer() != null) {
            int teammates = 0;
            for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
                if (other != player && !other.isSpectator() && ContractEvents.isSameTeam(player, other)) {
                    teammates++;
                }
            }
            if (teammates == 0) {
                set.add(LAST_STAND);
            }

            AABB box = player.getBoundingBox().inflate(NEARBY_RADIUS);
            int enemies = 0;
            int nearbyAllies = 0;
            for (Player other : level.getEntitiesOfClass(Player.class, box)) {
                if (other == player || other.isSpectator()) {
                    continue;
                }
                if (ContractEvents.isSameTeam(player, other)) {
                    nearbyAllies++;
                } else {
                    enemies++;
                }
            }
            for (var mob : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, box)) {
                if (mob instanceof Enemy) {
                    enemies++;
                }
            }
            if (enemies > nearbyAllies) {
                set.add(OUTNUMBERED);
            }
        }

        if (level.getWorldBorder().getDistanceToBorder(player.getX(), player.getZ()) <= BORDER_ALERT_DISTANCE) {
            set.add(NEAR_BORDER);
        }

        return set;
    }

    public static AccessoryCondition byId(String id) {
        if (id == null || id.isBlank()) {
            return ALWAYS;
        }
        for (AccessoryCondition condition : values()) {
            if (condition.name().equalsIgnoreCase(id)) {
                return condition;
            }
        }
        return ALWAYS;
    }
}
