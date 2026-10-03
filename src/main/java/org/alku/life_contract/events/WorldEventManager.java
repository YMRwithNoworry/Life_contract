package org.alku.life_contract.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.PerfProfiler;
import org.alku.life_contract.NetworkHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 游戏事件系统（对应 `杂物/table-7b913f6a-5239-449c-947d-9da551655309.csv` 的设计）：
 * <ul>
 *     <li>孢潮推进：第 5 分钟起在圈内随机位置生成感染精英</li>
 *     <li>清道夫悬赏：每淘汰 2 人，标记全场 K/D 最高者，击杀它可永久提升生命上限</li>
 *     <li>净化裂隙：第 9 分钟生成 3 个安全气泡，泡内玩家持续获得生命回复与饱和</li>
 *     <li>终局过载：剩 3 人时全体感染升级为 2 级，缩圈伤害 +100%</li>
 *     <li>孢子雨：随机事件，暴露在天幕下的玩家感染值上升，躲进遮蔽处才会恢复</li>
 * </ul>
 * 事件随对局进行，暂停游戏时同步暂停。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class WorldEventManager {

    private static final int SPORE_SURGE_MINUTE = 5;
    private static final int PURIFICATION_RIFT_MINUTE = 9;
    private static final int ENDGAME_PLAYER_COUNT = 3;
    private static final int ELIMINATION_THRESHOLD = 2;

    private static final int SPORE_SURGE_DURATION_SECONDS = 45;
    private static final int SPORE_SURGE_ELITE_MIN = 12;
    private static final int SPORE_SURGE_ELITE_RANDOM = 12;

    private static final int SAFE_BUBBLE_COUNT = 3;
    private static final double SAFE_BUBBLE_RADIUS = 15.0D;
    private static final int SAFE_BUBBLE_DURATION_SECONDS = 60;

    private static final int SPORE_RAIN_DURATION_SECONDS = 60;
    private static final int SPORE_RAIN_EXPOSURE_TICKS_PER_STAGE = 200;
    private static final int SPORE_RAIN_MAX_STAGE = 2;
    private static final int SPORE_RAIN_EFFECT_REFRESH_TICKS = 100;
    private static final int SPORE_RAIN_RECOVERY_TICKS = 200;
    /** 暴露计时结算间隔：一次结算补上这么多 tick，等价于逐 tick 结算。 */
    private static final int SPORE_RAIN_ACCOUNTING_INTERVAL_TICKS = 5;

    private static final int RANDOM_EVENT_CHECK_INTERVAL = 300;
    private static final int RANDOM_EVENT_MIN_INTERVAL = 300;
    private static final double RANDOM_EVENT_CHANCE = 0.3D;

    /** 缩圈伤害倍率：终局过载下 2.0 表示 +100%。 */
    private static final double ENDGAME_BORDER_DAMAGE_MULTIPLIER = 2.0D;
    private static final int ENDGAME_INFECTION_AMPLIFIER = 1;
    private static final int ENDGAME_INFECTION_REFRESH_TICKS = 1200;
    private static final int ENDGAME_INFECTION_DURATION_TICKS = 24000;

    private static final int BOUNTY_GLOW_DURATION_TICKS = 120;
    private static final int BOUNTY_GLOW_REFRESH_MARGIN_TICKS = 60;
    private static final int BUBBLE_EFFECT_DURATION_TICKS = 60;
    private static final int BUBBLE_EFFECT_REFRESH_MARGIN_TICKS = 20;
    private static final int BUBBLE_OUTLINE_INTERVAL_TICKS = 8;

    private static final String[] ELITE_MOB_IDS = {
        "spore:knight", "spore:griefer", "spore:braiomil", "spore:leaper",
        "spore:slasher", "spore:spitter", "spore:howler", "spore:stalker",
        "spore:brute", "spore:scavenger"
    };

    private static final String[] EVENT_NAMES = {
        "spore_surge", "bounty", "purification_rift", "endgame_overload", "spore_rain"
    };

    private static final Random RANDOM = new Random();

    private static boolean active;
    private static ServerLevel level;
    private static int initialPlayerCount;

    private static boolean sporeSurgeActive;
    private static boolean sporeSurgeScheduled;
    private static boolean sporeSurgeStopped;
    private static long sporeSurgeStartTick;

    private static boolean purificationRiftActive;
    private static boolean purificationRiftScheduled;
    private static boolean purificationRiftStopped;

    private static boolean endgameOverloadActive;
    private static boolean endgameOverloadStopped;
    private static long lastOverloadRefreshTick;

    private static boolean sporeRainActive;
    private static long sporeRainStartTick;
    private static final Map<UUID, Integer> sporeRainExposureTicks = new HashMap<>();

    private static UUID bountyTarget;
    private static int bountyKillReward;
    private static final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    private static int totalEliminations;

    private static final List<SafeBubble> safeBubbles = new ArrayList<>();

    private static long lastRandomEventTick = -1L;
    private static String lastSyncSignature = "";

    private WorldEventManager() {
    }

    /** 安全气泡：泡内不受感染生物侵扰，并持续给予队友恢复效果。 */
    public static final class SafeBubble {
        private final Vec3 position;
        private final double radius;
        private final int colorIndex;
        private int durationTicks;

        private SafeBubble(Vec3 position, double radius, int durationTicks, int colorIndex) {
            this.position = position;
            this.radius = radius;
            this.durationTicks = durationTicks;
            this.colorIndex = colorIndex;
        }

        public Vec3 getPosition() { return position; }
        public double getRadius() { return radius; }
        public int getColorIndex() { return colorIndex; }
        public int getDurationTicks() { return durationTicks; }
        public boolean isInside(Vec3 pos) { return position.distanceTo(pos) <= radius; }

        public AABB getBounds() {
            return new AABB(
                position.x - radius, position.y - radius, position.z - radius,
                position.x + radius, position.y + radius, position.z + radius
            );
        }
    }

    private static final class PlayerStats {
        private int kills;
        private int deaths;

        private double killDeathRatio() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }
    }

    // ==================== 生命周期 ====================

    /** 对局开始时清空上一局的事件状态。 */
    public static void startGame(ServerLevel gameLevel, int participantCount) {
        active = true;
        level = gameLevel;
        initialPlayerCount = participantCount;

        sporeSurgeActive = false;
        sporeSurgeScheduled = false;
        sporeSurgeStopped = false;
        sporeSurgeStartTick = 0L;

        purificationRiftActive = false;
        purificationRiftScheduled = false;
        purificationRiftStopped = false;

        endgameOverloadActive = false;
        endgameOverloadStopped = false;
        lastOverloadRefreshTick = 0L;

        sporeRainActive = false;
        sporeRainStartTick = 0L;
        sporeRainExposureTicks.clear();

        clearBountySilently();
        playerStats.clear();
        totalEliminations = 0;
        safeBubbles.clear();
        lastRandomEventTick = -1L;
        lastSyncSignature = "";
    }

    /** 对局结束时清理所有事件。 */
    public static void reset() {
        if (level != null) {
            releaseBountyGlow();
        }
        sporeSurgeActive = false;
        sporeSurgeScheduled = false;
        sporeSurgeStopped = false;
        purificationRiftActive = false;
        purificationRiftScheduled = false;
        purificationRiftStopped = false;
        endgameOverloadActive = false;
        endgameOverloadStopped = false;
        sporeRainActive = false;
        sporeRainStartTick = 0L;
        safeBubbles.clear();
        sporeRainExposureTicks.clear();
        clearBountySilently();
        playerStats.clear();
        totalEliminations = 0;
        lastRandomEventTick = -1L;
        lastSyncSignature = "";
        active = false;
        // 先同步清空后的状态，再释放关卡引用
        syncToClients();
        level = null;
    }

    public static boolean isRunning() {
        return active;
    }

    // ==================== 状态查询 ====================

    public static boolean isSporeSurgeActive() {
        return sporeSurgeActive && getSporeSurgeRemainingSeconds() > 0;
    }

    public static int getSporeSurgeRemainingSeconds() {
        if (!sporeSurgeActive || level == null || sporeSurgeStartTick <= 0L) return 0;
        long elapsed = (level.getGameTime() - sporeSurgeStartTick) / 20L;
        return (int) Math.max(0, SPORE_SURGE_DURATION_SECONDS - elapsed);
    }

    public static boolean isPurificationRiftActive() {
        return purificationRiftActive && !safeBubbles.isEmpty();
    }

    public static int getSafeBubbleRemainingSeconds() {
        return safeBubbles.isEmpty() ? 0 : safeBubbles.get(0).getDurationTicks() / 20;
    }

    public static List<SafeBubble> getSafeBubbles() {
        return Collections.unmodifiableList(safeBubbles);
    }

    public static boolean isEndgameOverloadActive() {
        return endgameOverloadActive;
    }

    public static boolean isSporeRainActive() {
        return sporeRainActive;
    }

    public static int getSporeRainRemainingSeconds() {
        if (!sporeRainActive || level == null || sporeRainStartTick <= 0L) return 0;
        long elapsed = (level.getGameTime() - sporeRainStartTick) / 20L;
        return (int) Math.max(0, SPORE_RAIN_DURATION_SECONDS - elapsed);
    }

    public static boolean isBountyActive() {
        return bountyTarget != null;
    }

    public static String getBountyTargetName() {
        if (bountyTarget == null || level == null) return "";
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(bountyTarget);
        return target != null ? target.getName().getString() : "";
    }

    public static int getBountyKillReward() {
        return bountyKillReward;
    }

    public static int getTotalEliminations() {
        return totalEliminations;
    }

    // ==================== 事件触发（管理指令可调用） ====================

    public static void forceTriggerSporeSurge(ServerLevel targetLevel) {
        level = targetLevel;
        sporeSurgeScheduled = true;
        sporeSurgeStopped = false;
        sporeSurgeStartTick = targetLevel.getGameTime();
        sporeSurgeActive = true;
        triggerSporeSurge();
        syncToClients();
    }

    public static void forceTriggerBountyHunt() {
        triggerBountyHunt();
        syncToClients();
    }

    public static void forceTriggerPurificationRift(ServerLevel targetLevel) {
        level = targetLevel;
        purificationRiftScheduled = true;
        purificationRiftStopped = false;
        triggerPurificationRift();
        syncToClients();
    }

    public static void forceTriggerEndgameOverload(ServerLevel targetLevel) {
        level = targetLevel;
        endgameOverloadStopped = false;
        triggerEndgameOverload();
        syncToClients();
    }

    public static void forceTriggerSporeRain(ServerLevel targetLevel) {
        level = targetLevel;
        startSporeRain(targetLevel.getGameTime());
        broadcast(Component.literal("§2[孢子雨] §f孢子雨事件已强制触发！"));
        syncToClients();
    }

    public static void stopSporeSurge() {
        sporeSurgeActive = false;
        sporeSurgeStartTick = 0L;
        sporeSurgeStopped = true;
        broadcast(Component.literal("§a[游戏事件] §f孢潮推进已停止。"));
        syncToClients();
    }

    public static void clearBounty() {
        releaseBountyGlow();
        clearBountySilently();
        broadcast(Component.literal("§a[游戏事件] §f悬赏已清除。"));
        syncToClients();
    }

    public static void stopPurificationRift() {
        purificationRiftActive = false;
        purificationRiftStopped = true;
        safeBubbles.clear();
        broadcast(Component.literal("§a[游戏事件] §f净化裂隙已停止。"));
        syncToClients();
    }

    public static void stopEndgameOverload() {
        endgameOverloadActive = false;
        endgameOverloadStopped = true;
        broadcast(Component.literal("§a[游戏事件] §f终局过载已停止。"));
        syncToClients();
    }

    public static void stopSporeRain() {
        sporeRainActive = false;
        sporeRainStartTick = 0L;
        sporeRainExposureTicks.clear();
        broadcast(Component.literal("§a[游戏事件] §f孢子雨已停止。"));
        syncToClients();
    }

    public static String[] eventNames() {
        return EVENT_NAMES.clone();
    }

    // ==================== 主循环 ====================

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long perfStart = PerfProfiler.begin();
        try {
            tickEvents(event);
        } finally {
            PerfProfiler.end("WorldEventManager", perfStart);
        }
    }

    private static void tickEvents(ServerTickEvent.Post event) {
        if (level == null || event.getServer().overworld() != level) return;

        long currentTick = level.getGameTime();
        // 对局进行中才按时间推进自动事件；管理指令强制触发的事件不受此限制
        if (active) {
            if (!GameEventManager.isGameActive() || GameEventManager.isGamePaused()) return;
            tickScheduledEvents(currentTick, GameEventManager.getElapsedSeconds());
        }

        if (endgameOverloadActive && currentTick - lastOverloadRefreshTick >= ENDGAME_INFECTION_REFRESH_TICKS) {
            lastOverloadRefreshTick = currentTick;
            applyEndgameInfection();
        }

        if (sporeRainActive) {
            tickSporeRain(currentTick);
        }

        tickSafeBubbles(currentTick);
        tickBountyGlow();

        // 同步只在可见状态变化时发包，这里放缓检查频率即可
        if (currentTick % 10L == 0L) {
            syncToClients();
        }
    }

    /** 按对局时间推进的事件时间表。 */
    private static void tickScheduledEvents(long currentTick, long elapsedSeconds) {
        if (!sporeSurgeActive && !sporeSurgeScheduled && !sporeSurgeStopped
                && elapsedSeconds >= SPORE_SURGE_MINUTE * 60L) {
            sporeSurgeScheduled = true;
            sporeSurgeStartTick = currentTick;
            sporeSurgeActive = true;
            triggerSporeSurge();
        }

        if (!purificationRiftActive && !purificationRiftScheduled && !purificationRiftStopped
                && elapsedSeconds >= PURIFICATION_RIFT_MINUTE * 60L) {
            purificationRiftScheduled = true;
            triggerPurificationRift();
        }

        if (!endgameOverloadActive && !endgameOverloadStopped && initialPlayerCount > ENDGAME_PLAYER_COUNT) {
            int survivors = getSurvivalPlayers().size();
            if (survivors > 0 && survivors <= ENDGAME_PLAYER_COUNT) {
                triggerEndgameOverload();
            }
        }

        if (elapsedSeconds > 0 && currentTick % RANDOM_EVENT_CHECK_INTERVAL == 0
                && (lastRandomEventTick < 0L || currentTick - lastRandomEventTick >= RANDOM_EVENT_MIN_INTERVAL)) {
            tryTriggerRandomEvent(currentTick);
        }

        if (sporeSurgeActive && sporeSurgeStartTick > 0L
                && (currentTick - sporeSurgeStartTick) / 20L >= SPORE_SURGE_DURATION_SECONDS) {
            sporeSurgeActive = false;
            sporeSurgeStartTick = 0L;
            broadcast(Component.literal("§c[孢潮推进] §f孢潮已经退去。"));
        }

        if (purificationRiftActive && safeBubbles.isEmpty()) {
            purificationRiftActive = false;
        }

        if (sporeRainActive && sporeRainStartTick > 0L
                && (currentTick - sporeRainStartTick) / 20L >= SPORE_RAIN_DURATION_SECONDS) {
            sporeRainActive = false;
            sporeRainStartTick = 0L;
            sporeRainExposureTicks.clear();
            broadcast(Component.literal("§2[孢子雨] §f孢子雨已停止！"));
        }
    }

    private static void tryTriggerRandomEvent(long currentTick) {
        if (RANDOM.nextDouble() > RANDOM_EVENT_CHANCE) return;

        List<String> available = new ArrayList<>(4);
        if (!sporeSurgeActive && !sporeSurgeStopped) available.add("spore_surge");
        if (!purificationRiftActive && !purificationRiftStopped) available.add("purification_rift");
        if (bountyTarget == null) available.add("bounty");
        if (!sporeRainActive) available.add("spore_rain");
        if (available.isEmpty()) return;

        String selected = available.get(RANDOM.nextInt(available.size()));
        switch (selected) {
            case "spore_surge" -> {
                sporeSurgeStartTick = currentTick;
                sporeSurgeActive = true;
                triggerSporeSurge();
                broadcast(Component.literal("§c[随机事件] §f孢潮推进突然袭来！"));
            }
            case "purification_rift" -> {
                triggerPurificationRift();
                broadcast(Component.literal("§b[随机事件] §f净化裂隙突然开启！"));
            }
            case "bounty" -> {
                triggerBountyHunt();
                broadcast(Component.literal("§e[随机事件] §f清道夫悬赏已经发布！"));
            }
            case "spore_rain" -> {
                startSporeRain(currentTick);
                broadcast(Component.literal("§2[随机事件] §f孢子雨降临！快找遮蔽物！"));
            }
            default -> {
            }
        }
        lastRandomEventTick = currentTick;
    }

    // ==================== 孢潮推进 ====================

    private static void triggerSporeSurge() {
        broadcast(Component.literal("§c[孢潮推进] §f感染精英正在涌入战场，注意脚下！"));

        WorldBorder border = level.getWorldBorder();
        if (border.getSize() <= 0.0D) return;

        boolean sporeLoaded = ModList.get().isLoaded("spore");
        if (!sporeLoaded) {
            broadcast(Component.literal("§e[孢潮推进] §f未检测到 Fungal Infection: Spore，改用备用精英生物。"));
        }

        int spawnCount = SPORE_SURGE_ELITE_MIN + RANDOM.nextInt(SPORE_SURGE_ELITE_RANDOM);
        for (int i = 0; i < spawnCount; i++) {
            spawnElite(border, sporeLoaded);
        }
    }

    private static void spawnElite(WorldBorder border, boolean sporeLoaded) {
        if (level == null) return;

        double halfSize = border.getSize() / 2.0D;
        double x = border.getCenterX() + (RANDOM.nextDouble() * 2.0D - 1.0D) * halfSize;
        double z = border.getCenterZ() + (RANDOM.nextDouble() * 2.0D - 1.0D) * halfSize;
        BlockPos surface = level.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            new BlockPos((int) x, 0, (int) z));
        double y = surface.getY();

        Entity entity = null;
        if (sporeLoaded) {
            String mobId = ELITE_MOB_IDS[RANDOM.nextInt(ELITE_MOB_IDS.length)];
            entity = createEntity(mobId, level);
        }
        if (entity == null) {
            EntityType<?> fallback = fallbackEntityType();
            if (fallback == null) return;
            entity = fallback.create(level);
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, true));
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1, false, true));
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, true));
            }
        }
        if (entity == null) return;

        entity.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(entity);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, x, y + 1.0D, z, 30, 1.0D, 1.0D, 1.0D, 0.1D);
        level.playSound(null, x, y, z, SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.HOSTILE, 1.0F, 0.5F);
    }

    private static Entity createEntity(String mobId, ServerLevel targetLevel) {
        try {
            ResourceLocation id = ResourceLocation.parse(mobId);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            return type == null ? null : type.create(targetLevel);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static EntityType<?> fallbackEntityType() {
        EntityType<?>[] types = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
            EntityType.ENDERMAN, EntityType.WITCH, EntityType.CREEPER
        };
        return types[RANDOM.nextInt(types.length)];
    }

    // ==================== 清道夫悬赏 ====================

    private static void triggerBountyHunt() {
        List<ServerPlayer> players = getSurvivalPlayers();
        if (players.isEmpty()) return;

        ServerPlayer target = null;
        double bestRatio = -1.0D;
        for (ServerPlayer player : players) {
            double ratio = stats(player).killDeathRatio();
            if (ratio > bestRatio) {
                bestRatio = ratio;
                target = player;
            }
        }
        if (target == null) return;

        releaseBountyGlow();
        bountyTarget = target.getUUID();
        bountyKillReward = (int) target.getMaxHealth() + 2;

        broadcast(Component.literal("§e[清道夫悬赏] §fK/D 最高者 §c" + target.getName().getString()
            + " §f已被全图标记！击杀奖励: §a+" + bountyKillReward + " §f生命上限"));
        target.sendSystemMessage(Component.literal("§c[清道夫悬赏] §f你已被标记为悬赏目标，小心了。"));
        tickBountyGlow();
    }

    /** 让悬赏目标持续发光，实现“全图标记”。 */
    private static void tickBountyGlow() {
        if (bountyTarget == null || level == null) return;
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(bountyTarget);
        if (target == null) return;

        MobEffectInstance current = target.getEffect(MobEffects.GLOWING);
        if (current != null && current.getDuration() > BOUNTY_GLOW_REFRESH_MARGIN_TICKS) return;
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, BOUNTY_GLOW_DURATION_TICKS, 0, false, false));
    }

    private static void releaseBountyGlow() {
        if (bountyTarget == null || level == null) return;
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(bountyTarget);
        if (target != null) {
            target.removeEffect(MobEffects.GLOWING);
        }
    }

    private static void clearBountySilently() {
        bountyTarget = null;
        bountyKillReward = 0;
    }

    private static PlayerStats stats(ServerPlayer player) {
        return playerStats.computeIfAbsent(player.getUUID(), key -> new PlayerStats());
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!active || level == null || !(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!GameEventManager.isPlayerPartOfGame(victim.getUUID())) return;
        if (!isActiveGamePlayer(victim)) return;

        totalEliminations++;
        stats(victim).deaths++;

        if (event.getSource().getEntity() instanceof ServerPlayer killer && !killer.getUUID().equals(victim.getUUID())) {
            handleKill(killer, victim);
        }

        if (totalEliminations % ELIMINATION_THRESHOLD == 0) {
            triggerBountyHunt();
            syncToClients();
        }
    }

    private static void handleKill(ServerPlayer killer, ServerPlayer victim) {
        stats(killer).kills++;

        if (bountyTarget == null || !bountyTarget.equals(victim.getUUID())) return;

        AttributeInstance maxHealth = killer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "bounty_reward_" + UUID.randomUUID()),
                bountyKillReward,
                AttributeModifier.Operation.ADD_VALUE));
        }
        killer.sendSystemMessage(Component.literal("§a[清道夫悬赏] §f击杀悬赏目标！生命上限 §e+" + bountyKillReward));
        broadcast(Component.literal("§e[清道夫悬赏] §f" + killer.getName().getString()
            + " §f击杀了悬赏目标 §c" + victim.getName().getString() + "§f！"));
        releaseBountyGlow();
        clearBountySilently();
    }

    // ==================== 净化裂隙 ====================

    private static void triggerPurificationRift() {
        broadcast(Component.literal("§b[净化裂隙] §f安全气泡已在圈内生成，泡内可安心恢复！"));

        WorldBorder border = level.getWorldBorder();
        if (border.getSize() <= 0.0D) return;

        double halfSize = border.getSize() / 2.0D;
        String[] colorNames = {"§f白色", "§a绿色", "§b蓝色"};

        for (int i = 0; i < SAFE_BUBBLE_COUNT; i++) {
            double x = border.getCenterX() + (RANDOM.nextDouble() - 0.5D) * halfSize * 0.8D;
            double z = border.getCenterZ() + (RANDOM.nextDouble() - 0.5D) * halfSize * 0.8D;
            BlockPos surface = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int) x, 0, (int) z));
            double y = surface.getY();

            safeBubbles.add(new SafeBubble(new Vec3(x, y, z), SAFE_BUBBLE_RADIUS,
                SAFE_BUBBLE_DURATION_SECONDS * 20, i));

            broadcast(Component.literal("§b  气泡" + (i + 1) + ": §fX:" + (int) x + " Y:" + (int) y
                + " Z:" + (int) z + " §7(半径 " + (int) SAFE_BUBBLE_RADIUS + " 格, " + colorNames[i] + "§7)"));
            level.sendParticles(bubbleParticle(i), x, y + 1.0D, z, 50, 3.0D, 2.0D, 3.0D, 0.05D);
        }
        purificationRiftActive = true;
    }

    private static void tickSafeBubbles(long currentTick) {
        if (safeBubbles.isEmpty()) return;

        Iterator<SafeBubble> iterator = safeBubbles.iterator();
        while (iterator.hasNext()) {
            SafeBubble bubble = iterator.next();
            bubble.durationTicks--;
            if (bubble.durationTicks <= 0) {
                iterator.remove();
                continue;
            }

            // 泡内效果每 10 tick 补一次即可（效果持续 60 tick），不必每 tick 做实体查询
            if (currentTick % 10L == 0L) {
                AABB bounds = bubble.getBounds();
                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, bounds)) {
                    if (!isActiveGamePlayer(player)) continue;
                    refreshBubbleEffect(player, MobEffects.REGENERATION, 1);
                    refreshBubbleEffect(player, MobEffects.SATURATION, 2);
                }
            }

            if (currentTick % BUBBLE_OUTLINE_INTERVAL_TICKS == 0L) {
                drawBubbleOutline(bubble);
            }
        }

        if (safeBubbles.isEmpty()) {
            purificationRiftActive = false;
        }
    }

    /** 泡内玩家持续保持生命回复与饱和，仅在效果即将结束时续期，避免每刻刷包。 */
    private static void refreshBubbleEffect(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && current.getAmplifier() >= amplifier
                && current.getDuration() > BUBBLE_EFFECT_REFRESH_MARGIN_TICKS) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, BUBBLE_EFFECT_DURATION_TICKS, amplifier, false, true));
    }

    private static void drawBubbleOutline(SafeBubble bubble) {
        Vec3 center = bubble.getPosition();
        double radius = bubble.getRadius();
        ParticleOptions particle = bubbleParticle(bubble.getColorIndex());

        int points = 24;
        for (int i = 0; i < points; i++) {
            double angle = 2.0D * Math.PI * i / points;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            level.sendParticles(particle, x, center.y + 1.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(particle, x, center.y + 5.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (int i = 0; i <= 6; i++) {
            double y = center.y + i * 10.0D / 6.0D;
            for (double angle : new double[]{0.0D, Math.PI / 2.0D, Math.PI, 3.0D * Math.PI / 2.0D}) {
                level.sendParticles(particle,
                    center.x + radius * Math.cos(angle), y, center.z + radius * Math.sin(angle),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.HEART, center.x, center.y + 1.0D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static ParticleOptions bubbleParticle(int colorIndex) {
        ParticleOptions[] particles = {
            ParticleTypes.END_ROD,
            ParticleTypes.TOTEM_OF_UNDYING,
            ParticleTypes.SOUL_FIRE_FLAME
        };
        return particles[Math.floorMod(colorIndex, particles.length)];
    }

    // ==================== 终局过载 ====================

    private static void triggerEndgameOverload() {
        endgameOverloadActive = true;
        lastOverloadRefreshTick = level.getGameTime();
        broadcast(Component.literal("§4[终局过载] §f只剩 3 人！全体感染升至 2 级，缩圈伤害 +100%！"));
        applyEndgameInfection();
    }

    private static void applyEndgameInfection() {
        for (ServerPlayer player : getSurvivalPlayers()) {
            player.addEffect(new MobEffectInstance(Life_contract.SLOW_INFECTION,
                ENDGAME_INFECTION_DURATION_TICKS, ENDGAME_INFECTION_AMPLIFIER, false, true));
        }
    }

    /** 终局过载期间，世界边界造成的伤害翻倍。 */
    @SubscribeEvent
    public static void onBorderDamage(LivingIncomingDamageEvent event) {
        if (!active || !endgameOverloadActive) return;
        if (!event.getSource().is(DamageTypes.OUTSIDE_BORDER)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!GameEventManager.isPlayerPartOfGame(player.getUUID())) return;
        event.setAmount(event.getAmount() * (float) ENDGAME_BORDER_DAMAGE_MULTIPLIER);
    }

    // ==================== 孢子雨 ====================

    private static void startSporeRain(long currentTick) {
        sporeRainActive = true;
        sporeRainStartTick = currentTick;
        sporeRainExposureTicks.clear();
        broadcast(Component.literal("§2[孢子雨] §f孢子雨降临！暴露在天空下会持续被感染，快找遮蔽物！"));
    }

    private static void tickSporeRain(long currentTick) {
        // 暴露结算每 5 tick 做一次、一次补 5，与逐 tick 结算完全等价（效果刷新点仍是 100 的倍数），
        // 但省掉 4/5 的高度图查询：孢子雨期间每个在线玩家每 tick 都要查一次。
        if (currentTick % SPORE_RAIN_ACCOUNTING_INTERVAL_TICKS == 0L) {
            for (ServerPlayer player : level.getPlayers(p -> true)) {
                if (player.isCreative() || player.isSpectator()) continue;

                UUID playerId = player.getUUID();
                if (isExposedToRain(player)) {
                    int exposure = sporeRainExposureTicks.merge(playerId,
                            SPORE_RAIN_ACCOUNTING_INTERVAL_TICKS, Integer::sum);
                    int stage = Math.min(SPORE_RAIN_MAX_STAGE, exposure / SPORE_RAIN_EXPOSURE_TICKS_PER_STAGE);
                    if (stage > 0 && exposure % SPORE_RAIN_EFFECT_REFRESH_TICKS == 0) {
                        player.addEffect(new MobEffectInstance(Life_contract.SLOW_INFECTION,
                            SPORE_RAIN_EFFECT_REFRESH_TICKS + 40, stage - 1, false, true));
                    }
                    if (currentTick % 200L == 0L) {
                        player.sendSystemMessage(Component.literal("§2[孢子雨] §f你正暴露在孢子雨中，感染正在加深！"));
                    }
                } else {
                    int exposure = sporeRainExposureTicks.getOrDefault(playerId, 0);
                    if (exposure > 0) {
                        int reduced = Math.max(0,
                                exposure - (SPORE_RAIN_RECOVERY_TICKS / 4) * SPORE_RAIN_ACCOUNTING_INTERVAL_TICKS);
                        if (reduced == 0) {
                            sporeRainExposureTicks.remove(playerId);
                        } else {
                            sporeRainExposureTicks.put(playerId, reduced);
                        }
                    }
                }
            }
        }

        // 粒子每 10 tick 铺一次就够了，原来每 2 tick 会让每个在线玩家产生十几条粒子包
        if (currentTick % 10L == 0L) {
            spawnSporeRainParticles();
        }
    }

    private static boolean isExposedToRain(ServerPlayer player) {
        return level.canSeeSky(player.blockPosition());
    }

    private static void spawnSporeRainParticles() {
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            if (player.isCreative() || player.isSpectator()) continue;
            // 躲在遮蔽物下的玩家看不到头顶的雨粒子（粒子生成在玩家上方 8~13 格，会被屋顶挡住），
            // 而孢子雨的提示恰恰是让玩家进屋躲雨，所以这类玩家直接跳过，省下整包粒子。
            if (!isExposedToRain(player)) continue;
            for (int i = 0; i < 5; i++) {
                double x = player.getX() + (RANDOM.nextDouble() - 0.5D) * 20.0D;
                double z = player.getZ() + (RANDOM.nextDouble() - 0.5D) * 20.0D;
                double y = player.getY() + 8.0D + RANDOM.nextDouble() * 5.0D;

                level.sendParticles(ParticleTypes.CRIMSON_SPORE, x, y, z, 3, 0.5D, 0.0D, 0.5D, 0.02D);
                level.sendParticles(ParticleTypes.WARPED_SPORE, x + RANDOM.nextDouble(), y, z + RANDOM.nextDouble(),
                    3, 0.5D, 0.0D, 0.5D, 0.02D);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, x, y - 2.0D, z, 2, 0.3D, 0.0D, 0.3D, 0.01D);
            }
        }
    }

    // ==================== 工具方法 ====================

    private static List<ServerPlayer> getSurvivalPlayers() {
        if (level == null) return new ArrayList<>();
        return level.getPlayers(WorldEventManager::isActiveGamePlayer);
    }

    private static boolean isActiveGamePlayer(ServerPlayer player) {
        GameType gameType = player.gameMode.getGameModeForPlayer();
        return gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE;
    }

    private static void broadcast(Component message) {
        if (level == null) return;
        for (ServerPlayer player : level.getPlayers(p -> true)) {
            player.sendSystemMessage(message);
        }
    }

    private static void syncToClients() {
        if (level == null) return;

        List<EventSyncPayload.Bubble> bubbles = new ArrayList<>(safeBubbles.size());
        for (SafeBubble bubble : safeBubbles) {
            Vec3 pos = bubble.getPosition();
            bubbles.add(new EventSyncPayload.Bubble(
                (int) pos.x, (int) pos.y, (int) pos.z, (float) bubble.getRadius(), bubble.getColorIndex()));
        }

        // 只有可见状态发生变化时才发包，避免空转刷屏
        String signature = GameEventManager.isGameActive() + "|" + isSporeSurgeActive() + "|"
                + getSporeSurgeRemainingSeconds() + "|" + isPurificationRiftActive() + "|"
                + getSafeBubbleRemainingSeconds() + "|" + bubbles.size() + "|" + isBountyActive() + "|"
                + getBountyTargetName() + "|" + isEndgameOverloadActive() + "|" + isSporeRainActive() + "|"
                + getSporeRainRemainingSeconds();
        if (signature.equals(lastSyncSignature)) return;
        lastSyncSignature = signature;

        NetworkHandler.sendToAllPlayers(new EventSyncPayload(
            GameEventManager.isGameActive(),
            isSporeSurgeActive(),
            getSporeSurgeRemainingSeconds(),
            isPurificationRiftActive(),
            getSafeBubbleRemainingSeconds(),
            bubbles,
            isBountyActive(),
            getBountyTargetName(),
            isEndgameOverloadActive(),
            isSporeRainActive(),
            getSporeRainRemainingSeconds()));
    }
}
