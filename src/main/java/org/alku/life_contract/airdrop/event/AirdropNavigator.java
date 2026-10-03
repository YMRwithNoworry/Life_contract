package org.alku.life_contract.airdrop.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.alku.life_contract.airdrop.entity.AirdropEntity;
import org.alku.life_contract.airdrop.network.AirdropPayload;
import org.alku.life_contract.airdrop.network.NetworkHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AirdropNavigator {

    private static final Set<UUID> particlesEnabled = new HashSet<>();

    /** 当前正在被追踪线指向的玩家：只有它们需要在丢失目标时收一次清除包。 */
    private static final Set<UUID> trackingActive = new HashSet<>();

    /** 诱饵信号按维度隔离，修复跨维度串扰 bug */
    private static final Map<ResourceLocation, Map<String, DecoySignal>> decoySignals = new ConcurrentHashMap<>();
    private static final long DECOY_DURATION_MS = 120 * 1000;

    public static class DecoySignal {
        public final double x;
        public final double z;
        public final long expireTime;
        public final UUID ownerId;

        public DecoySignal(double x, double z, UUID ownerId) {
            this.x = x;
            this.z = z;
            this.ownerId = ownerId;
            this.expireTime = System.currentTimeMillis() + DECOY_DURATION_MS;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public Vec3 getPosition() {
            return new Vec3(x, 64, z);
        }
    }

    public static void addDecoySignal(Level level, double x, double z) {
        ResourceLocation dimKey = level.dimension().location();
        decoySignals.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>())
                .put(UUID.randomUUID().toString(), new DecoySignal(x, z, null));
    }

    public static void addDecoySignal(Level level, double x, double z, UUID ownerId) {
        ResourceLocation dimKey = level.dimension().location();
        decoySignals.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>())
                .put(UUID.randomUUID().toString(), new DecoySignal(x, z, ownerId));
    }

    private static List<DecoySignal> getActiveDecoySignals(Level level) {
        ResourceLocation dimKey = level.dimension().location();
        Map<String, DecoySignal> dimDecoys = decoySignals.getOrDefault(dimKey, Collections.emptyMap());
        dimDecoys.values().removeIf(DecoySignal::isExpired);
        if (dimDecoys.isEmpty()) {
            decoySignals.remove(dimKey);
        }
        return new ArrayList<>(dimDecoys.values());
    }

    public static void setParticlesEnabled(Player player, boolean enabled) {
        if (enabled) {
            particlesEnabled.add(player.getUUID());
        } else {
            particlesEnabled.remove(player.getUUID());
            trackingActive.remove(player.getUUID());
        }
    }

    /** 玩家离线时清理状态，避免长期运行后集合只增不减。 */
    public static void forgetPlayer(UUID playerId) {
        particlesEnabled.remove(playerId);
        trackingActive.remove(playerId);
    }

    public static boolean isParticlesEnabled(Player player) {
        return particlesEnabled.contains(player.getUUID());
    }

    public static void toggleParticles(Player player) {
        if (isParticlesEnabled(player)) {
            setParticlesEnabled(player, false);
            player.sendSystemMessage(Component.literal("§c[空投导航] 导航线已关闭"));
        } else {
            setParticlesEnabled(player, true);
            player.sendSystemMessage(Component.literal("§a[空投导航] 导航线已开启"));
        }
    }

    /**
     * 已加载的空投实体按维度登记，避免每次导航都做“整个世界边界”级别的实体扫描
     * （600×600 边界 ≈ 1300 个区块，每次扫描要遍历数万个实体分区，是服务端 TPS 的主要开销）。
     */
    private static final Map<ResourceLocation, Set<UUID>> TRACKED_AIRDROPS = new ConcurrentHashMap<>();

    public static void trackAirdrop(Level level, Entity entity) {
        if (!(entity instanceof AirdropEntity)) return;
        TRACKED_AIRDROPS
                .computeIfAbsent(level.dimension().location(), key -> ConcurrentHashMap.newKeySet())
                .add(entity.getUUID());
    }

    public static void untrackAirdrop(Level level, Entity entity) {
        Set<UUID> tracked = TRACKED_AIRDROPS.get(level.dimension().location());
        if (tracked == null) return;
        tracked.remove(entity.getUUID());
        if (tracked.isEmpty()) {
            TRACKED_AIRDROPS.remove(level.dimension().location());
        }
    }

    private static List<AirdropEntity> getActiveAirdrops(ServerLevel level) {
        Set<UUID> tracked = TRACKED_AIRDROPS.get(level.dimension().location());
        if (tracked == null || tracked.isEmpty()) {
            return List.of();
        }

        List<AirdropEntity> active = new ArrayList<>(tracked.size());
        for (UUID id : tracked) {
            Entity entity = level.getEntity(id);
            if (entity == null) {
                // 区块卸载后实体不再可用，顺手清理登记
                tracked.remove(id);
                continue;
            }
            if (entity instanceof AirdropEntity airdrop && airdrop.isAlive() && !airdrop.isClaimed()) {
                active.add(airdrop);
            }
        }
        if (tracked.isEmpty()) {
            TRACKED_AIRDROPS.remove(level.dimension().location());
        }
        return active;
    }

    /** 供 /contract perf 查看导航状态规模。 */
    public static String debugSummary() {
        int tracked = 0;
        for (Set<UUID> ids : TRACKED_AIRDROPS.values()) {
            tracked += ids.size();
        }
        int decoys = 0;
        for (Map<String, DecoySignal> perDimension : decoySignals.values()) {
            decoys += perDimension.size();
        }
        return "登记空投 " + tracked + " / 诱饵 " + decoys
                + " / 导航开启 " + particlesEnabled.size() + " / 追踪中 " + trackingActive.size();
    }

    public static void updateNavigation(ServerLevel level) {
        List<AirdropEntity> activeAirdrops = getActiveAirdrops(level);

        List<DecoySignal> activeDecoys = getActiveDecoySignals(level);

        // 既没有目标也没有追踪线时直接返回，连玩家列表都不必遍历
        if (activeAirdrops.isEmpty() && activeDecoys.isEmpty() && trackingActive.isEmpty()) {
            return;
        }

        if (activeAirdrops.isEmpty() && activeDecoys.isEmpty()) {
            // 没有空投时只给“当前确实有追踪线”的玩家发一次清除包，
            // 否则每 5 tick 都会给每个开启导航的玩家重复发送空包。
            if (!trackingActive.isEmpty()) {
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    if (player.level() != level) continue;
                    if (trackingActive.remove(player.getUUID())) {
                        NetworkHandler.sendToPlayer(player, AirdropPayload.clear());
                    }
                }
                trackingActive.clear();
            }
            return;
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) continue;

            AirdropEntity nearestAirdrop = activeAirdrops.stream()
                    .min(Comparator.comparingDouble(a -> a.distanceToSqr(player)))
                    .orElse(null);

            DecoySignal nearestDecoy = findNearestDecoyForPlayer(player, activeDecoys);

            if (nearestAirdrop != null) {
                double distance = Math.sqrt(nearestAirdrop.distanceToSqr(player));
                String direction = getDirectionToAirdrop(player, nearestAirdrop);

                // 动作栏每 10 tick 提示一次即可，避免每 5 tick 刷一条消息
                if (level.getGameTime() % 10L == 0L) {
                    Component actionBarMsg = Component.literal(
                            String.format("§6§l[空投导航] §e%.0fm §b%s", distance, direction));
                    player.sendSystemMessage(actionBarMsg, true);
                }

                if (isParticlesEnabled(player)) {
                    Vec3 targetPos = nearestAirdrop.position();
                    NetworkHandler.sendToPlayer(player,
                            new AirdropPayload(true, targetPos.x, targetPos.y, targetPos.z));
                    trackingActive.add(player.getUUID());
                }
            } else if (nearestDecoy != null && isParticlesEnabled(player)) {
                Vec3 decoyPos = nearestDecoy.getPosition();
                double distance = player.position().distanceTo(decoyPos);

                NetworkHandler.sendToPlayer(player,
                        new AirdropPayload(true, decoyPos.x, decoyPos.y, decoyPos.z));
                trackingActive.add(player.getUUID());

                Component actionBarMsg = Component.literal(
                        String.format("§d§l[伪装信号] §e%.0fm", distance));
                player.sendSystemMessage(actionBarMsg, true);
            } else if (isParticlesEnabled(player) && trackingActive.remove(player.getUUID())) {
                // 该玩家刚刚失去目标，清除追踪线
                NetworkHandler.sendToPlayer(player, AirdropPayload.clear());
            }
        }
    }

    private static DecoySignal findNearestDecoyForPlayer(ServerPlayer player, List<DecoySignal> decoys) {
        if (decoys.isEmpty()) return null;

        DecoySignal nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (DecoySignal decoy : decoys) {
            double dist = player.distanceToSqr(decoy.x, player.getY(), decoy.z);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = decoy;
            }
        }

        if (nearest == null) return null;

        for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
            if (other == player || other.level() != player.level()) continue;
            double otherDist = other.distanceToSqr(nearest.x, other.getY(), nearest.z);
            if (otherDist < nearestDist) {
                return null;
            }
        }

        return nearest;
    }

    private static String getDirectionToAirdrop(Player player, AirdropEntity airdrop) {
        Vec3 playerPos = player.position();
        Vec3 airdropPos = airdrop.position();

        double dx = airdropPos.x - playerPos.x;
        double dz = airdropPos.z - playerPos.z;

        double targetAngle = Math.toDegrees(Math.atan2(-dx, dz));
        if (targetAngle < 0) targetAngle += 360;

        float playerYaw = player.getYHeadRot();
        double yaw = ((playerYaw % 360) + 360) % 360;

        double relativeAngle = targetAngle - yaw;
        if (relativeAngle > 180) relativeAngle -= 360;
        if (relativeAngle < -180) relativeAngle += 360;

        if (relativeAngle >= -22.5 && relativeAngle < 22.5) {
            return "↑ 前";
        } else if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
            return "↗ 右前";
        } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
            return "→ 右";
        } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
            return "↘ 右后";
        } else if (relativeAngle >= 157.5 || relativeAngle < -157.5) {
            return "↓ 后";
        } else if (relativeAngle >= -157.5 && relativeAngle < -112.5) {
            return "↙ 左后";
        } else if (relativeAngle >= -112.5 && relativeAngle < -67.5) {
            return "← 左";
        } else {
            return "↖ 左前";
        }
    }
}
