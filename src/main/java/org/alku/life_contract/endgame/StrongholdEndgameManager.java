package org.alku.life_contract.endgame;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.alku.life_contract.world.SurfaceFinder;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.registries.BuiltInRegistries;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.PerfProfiler;
import org.alku.life_contract.events.GameEventManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class StrongholdEndgameManager {
    private static final int STRONGHOLD_SEARCH_RADIUS_CHUNKS = 256;
    private static final double PORTAL_ACTIVATION_BORDER_SIZE = 50.0D;
    private static final double MINIMUM_BORDER_SIZE = 10.0D;
    private static final double END_BORDER_SIZE = 500.0D;
    private static final double END_BOSS_MAX_Y = 105.0D;
    private static final double END_BOSS_RESET_Y = 103.0D;
    private static final ResourceLocation DISTORTED_ENDERMAN_ID =
            ResourceLocation.fromNamespaceAndPath("phayriosis", "distorted_enderman");
    /**
     * 末地终局 Boss：真菌感染：孢子 的朽翼魔（Verfalldrache）。
     * <p>
     * 注意注册名是 {@code spore:verfall}（游戏内 {@code /summon spore:verfall}），
     * 不是 {@code spore:verfalldrache}；两种写法都列上以兼容不同版本。
     */
    private static final List<ResourceLocation> END_BOSS_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath("spore", "verfall"),
            ResourceLocation.fromNamespaceAndPath("spore", "verfalldrache"));
    /** 未加载 spore 时退回的旧版终局 Boss，保证终局始终有可击杀的目标。 */
    private static final ResourceLocation LEGACY_END_BOSS_ID =
            ResourceLocation.fromNamespaceAndPath("phayriosis", "converted_dragon");
    private static final String END_ENCOUNTER_ENTITY_TAG = "LifeContractEndEncounterEntity";
    private static final String END_BOSS_TAG = "LifeContractEndBoss";

    private static final List<LocalPortalPos> FRAME_LOCAL_POSITIONS = List.of(
            new LocalPortalPos(4, 3, 8),
            new LocalPortalPos(5, 3, 8),
            new LocalPortalPos(6, 3, 8),
            new LocalPortalPos(4, 3, 12),
            new LocalPortalPos(5, 3, 12),
            new LocalPortalPos(6, 3, 12),
            new LocalPortalPos(3, 3, 9),
            new LocalPortalPos(3, 3, 10),
            new LocalPortalPos(3, 3, 11),
            new LocalPortalPos(7, 3, 9),
            new LocalPortalPos(7, 3, 10),
            new LocalPortalPos(7, 3, 11));

    private static ServerLevel activeLevel;
    private static BlockPos portalCenter;
    private static List<BlockPos> portalFrames = List.of();
    private static List<BlockPos> portalInterior = List.of();
    private static int missingFrameIndex = -1;
    private static boolean portalActivated;
    private static boolean endEncounterInitialized;
    private static UUID endBossUuid;
    /** 本次对局实际生成的 Boss 实体 ID，用于提示文案。 */
    private static ResourceLocation activeEndBossId;
    /** 终局 Boss 的血条（服务端 BossEvent，会显示在屏幕正上方）。 */
    private static ServerBossEvent endBossBar;

    private StrongholdEndgameManager() {
    }

    public static PreparationResult prepareForGame(ServerLevel level, BlockPos searchOrigin) {
        clearSession();

        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder.Reference<Structure> strongholdHolder = structures.getHolderOrThrow(BuiltinStructures.STRONGHOLD);
        Pair<BlockPos, Holder<Structure>> nearest = level.getChunkSource().getGenerator().findNearestMapStructure(
                level,
                HolderSet.direct(strongholdHolder),
                searchOrigin,
                STRONGHOLD_SEARCH_RADIUS_CHUNKS,
                false);
        if (nearest == null) {
            return PreparationResult.failure("无法找到末地要塞，请确认世界已启用结构生成");
        }

        ChunkPos startChunkPos = new ChunkPos(nearest.getFirst());
        LevelChunk startChunk = level.getChunk(startChunkPos.x, startChunkPos.z);
        StructureStart strongholdStart = startChunk.getStartForStructure(strongholdHolder.value());
        if (strongholdStart == null || !strongholdStart.isValid()) {
            return PreparationResult.failure("已定位末地要塞，但无法读取其结构起点");
        }

        StrongholdPieces.PortalRoom portalRoom = strongholdStart.getPieces().stream()
                .filter(StrongholdPieces.PortalRoom.class::isInstance)
                .map(StrongholdPieces.PortalRoom.class::cast)
                .findFirst()
                .orElse(null);
        if (portalRoom == null) {
            return PreparationResult.failure("末地要塞中未找到传送门房间");
        }

        forcePortalRoomChunks(level, portalRoom.getBoundingBox());
        configurePortal(level, portalRoom);

        return PreparationResult.success(
                portalCenter,
                "末地要塞传送门已锁定于 " + portalCenter.getX() + ", "
                        + portalCenter.getY() + ", " + portalCenter.getZ()
                        + "，并预填 11 颗末影之眼");
    }

    private static void forcePortalRoomChunks(ServerLevel level, BoundingBox bounds) {
        int minChunkX = bounds.minX() >> 4;
        int maxChunkX = bounds.maxX() >> 4;
        int minChunkZ = bounds.minZ() >> 4;
        int maxChunkZ = bounds.maxZ() >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    private static void configurePortal(ServerLevel level, StrongholdPieces.PortalRoom portalRoom) {
        BlockPos center = toWorldPosition(portalRoom, new LocalPortalPos(5, 3, 10));
        List<BlockPos> frames = new ArrayList<>(FRAME_LOCAL_POSITIONS.size());
        for (LocalPortalPos localPos : FRAME_LOCAL_POSITIONS) {
            frames.add(toWorldPosition(portalRoom, localPos));
        }

        List<BlockPos> interior = new ArrayList<>(9);
        for (int localX = 4; localX <= 6; localX++) {
            for (int localZ = 9; localZ <= 11; localZ++) {
                interior.add(toWorldPosition(portalRoom, new LocalPortalPos(localX, 3, localZ)));
            }
        }

        int emptyIndex = Math.floorMod(Long.hashCode(level.getSeed() ^ center.asLong()), frames.size());
        for (int index = 0; index < frames.size(); index++) {
            BlockPos framePos = frames.get(index);
            BlockState existing = level.getBlockState(framePos);
            Direction facing = existing.is(Blocks.END_PORTAL_FRAME)
                    ? existing.getValue(EndPortalFrameBlock.FACING)
                    : outwardFacing(center, framePos);
            BlockState frameState = Blocks.END_PORTAL_FRAME.defaultBlockState()
                    .setValue(EndPortalFrameBlock.FACING, facing)
                    .setValue(EndPortalFrameBlock.HAS_EYE, index != emptyIndex);
            level.setBlock(framePos, frameState, Block.UPDATE_ALL);
        }
        for (BlockPos interiorPos : interior) {
            if (level.getBlockState(interiorPos).is(Blocks.END_PORTAL)) {
                level.setBlock(interiorPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        activeLevel = level;
        portalCenter = center;
        portalFrames = List.copyOf(frames);
        portalInterior = List.copyOf(interior);
        missingFrameIndex = emptyIndex;
        portalActivated = false;
    }

    private static BlockPos toWorldPosition(StructurePiece piece, LocalPortalPos localPos) {
        BoundingBox bounds = piece.getBoundingBox();
        Direction orientation = piece.getOrientation();
        if (orientation == null) {
            return new BlockPos(localPos.x(), localPos.y(), localPos.z());
        }

        int worldX = switch (orientation) {
            case NORTH, SOUTH -> bounds.minX() + localPos.x();
            case WEST -> bounds.maxX() - localPos.z();
            case EAST -> bounds.minX() + localPos.z();
            default -> localPos.x();
        };
        int worldZ = switch (orientation) {
            case NORTH -> bounds.maxZ() - localPos.z();
            case SOUTH -> bounds.minZ() + localPos.z();
            case WEST, EAST -> bounds.minZ() + localPos.x();
            default -> localPos.z();
        };
        return new BlockPos(worldX, bounds.minY() + localPos.y(), worldZ);
    }

    private static Direction outwardFacing(BlockPos center, BlockPos frame) {
        int deltaX = frame.getX() - center.getX();
        int deltaZ = frame.getZ() - center.getZ();
        if (Math.abs(deltaX) > Math.abs(deltaZ)) {
            return deltaX < 0 ? Direction.WEST : Direction.EAST;
        }
        return deltaZ < 0 ? Direction.NORTH : Direction.SOUTH;
    }

    public static void tick(ServerLevel level, boolean allowActivation) {
        if (activeLevel != level || portalCenter == null) {
            return;
        }

        WorldBorder border = level.getWorldBorder();
        double centerX = portalCenter.getX() + 0.5D;
        double centerZ = portalCenter.getZ() + 0.5D;
        if (Math.abs(border.getCenterX() - centerX) > 0.001D
                || Math.abs(border.getCenterZ() - centerZ) > 0.001D) {
            border.setCenter(centerX, centerZ);
        }

        double borderSize = border.getSize();
        if (allowActivation && !portalActivated && borderSize < PORTAL_ACTIVATION_BORDER_SIZE) {
            activatePortal();
        }
        if (borderSize < MINIMUM_BORDER_SIZE) {
            border.setSize(MINIMUM_BORDER_SIZE);
        }
    }

    private static void activatePortal() {
        if (activeLevel == null || portalCenter == null || portalFrames.size() != 12) {
            return;
        }

        for (BlockPos framePos : portalFrames) {
            BlockState existing = activeLevel.getBlockState(framePos);
            Direction facing = existing.is(Blocks.END_PORTAL_FRAME)
                    ? existing.getValue(EndPortalFrameBlock.FACING)
                    : outwardFacing(portalCenter, framePos);
            activeLevel.setBlock(framePos, Blocks.END_PORTAL_FRAME.defaultBlockState()
                    .setValue(EndPortalFrameBlock.FACING, facing)
                    .setValue(EndPortalFrameBlock.HAS_EYE, true), Block.UPDATE_ALL);
        }
        for (BlockPos interiorPos : portalInterior) {
            activeLevel.setBlock(interiorPos, Blocks.END_PORTAL.defaultBlockState(), Block.UPDATE_ALL);
        }

        portalActivated = true;
        // 传送门状态变了，刷新 HUD 上的坐标显示
        org.alku.life_contract.WaypointSync.broadcast();
        activeLevel.playSound(null, portalCenter, SoundEvents.END_PORTAL_SPAWN,
                SoundSource.BLOCKS, 1.5F, 1.0F);
        activeLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§5[终局] §f边界已小于 §e50×50§f，最后一颗末影之眼已补齐，末地传送门现已开启！"),
                false);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !Level.END.equals(event.getTo())) {
            return;
        }
        ServerLevel endLevel = player.serverLevel();
        enforceEndBorder(endLevel);
        suppressVanillaDragonFight(endLevel);
        initializeEndEncounter(endLevel);
        if (endBossBar != null) {
            endBossBar.addPlayer(player);
        }
    }

    /**
     * 末地边界必须单独钉在原点。
     * <p>
     * 主世界边界是以开局玩家为中心的，而原版在建维度时给主世界边界挂了一个
     * {@code BorderChangeListener.DelegateBorderChangeListener}，会把主世界边界的
     * <b>中心与尺寸同步到其它维度</b>。于是主世界每次缩圈都会把末地边界重新拽回主世界中心，
     * 玩家一进末地（末地主岛在 0,0）就落在边界外。
     * <p>
     * 这里在末地每个 tick 检查一次，一旦被改回去就立刻纠正并重发边界包。
     */
    private static void enforceEndBorder(ServerLevel endLevel) {
        WorldBorder border = endLevel.getWorldBorder();
        boolean centered = Math.abs(border.getCenterX()) < 1.0E-4D
                && Math.abs(border.getCenterZ()) < 1.0E-4D;
        boolean sized = Math.abs(border.getSize() - END_BORDER_SIZE) < 1.0E-4D;
        if (centered && sized) {
            return;
        }

        border.setCenter(0.0D, 0.0D);
        border.setSize(END_BORDER_SIZE);
        for (ServerPlayer player : endLevel.players()) {
            player.connection.send(new ClientboundInitializeBorderPacket(border));
        }
    }

    private static void suppressVanillaDragonFight(ServerLevel endLevel) {
        EndDragonFight dragonFight = endLevel.getDragonFight();
        if (dragonFight == null) {
            return;
        }
        for (ServerPlayer player : endLevel.getServer().getPlayerList().getPlayers()) {
            dragonFight.removePlayer(player);
        }
        endLevel.setDragonFight(null);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entity instanceof LivingEntity
                && entityId != null
                && "phayriosis".equals(entityId.getNamespace())
                && !Level.END.equals(event.getLevel().dimension())) {
            event.setCanceled(true);
            return;
        }

        if (entity instanceof EnderDragon
                && Level.END.equals(event.getLevel().dimension())) {
            event.setCanceled(true);
            return;
        }

        if (isEndBossEntity(entity)
                && Level.END.equals(event.getLevel().dimension())
                && event.getLevel() instanceof ServerLevel) {
            endBossUuid = entity.getUUID();
            activeEndBossId = entityId;
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        long perfStart = PerfProfiler.begin();
        try {
            if (event.getLevel() instanceof ServerLevel level && Level.END.equals(level.dimension())) {
                enforceEndBorder(level);
            }
            tickEndBoss(event);
        } finally {
            PerfProfiler.end("EndBoss.levelTick", perfStart);
        }
    }

    private static void tickEndBoss(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel endLevel)
                || !Level.END.equals(endLevel.dimension())
                || endBossUuid == null) {
            return;
        }

        Entity entity = endLevel.getEntity(endBossUuid);
        if (!(entity instanceof LivingEntity dragon) || !isEndBossEntity(entity)) {
            endBossUuid = null;
            return;
        }
        capEndBossFlight(dragon);
        updateEndBossBar(dragon);
    }

    // ==================== 终局 Boss 血条 ====================

    /** 起一条紫色 Boss 血条；已经在末地的玩家直接加入。 */
    private static void startEndBossBar(LivingEntity boss, ResourceLocation bossId) {
        stopEndBossBar();

        endBossBar = new ServerBossEvent(
                Component.literal(bossDisplayName(bossId)),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS);
        endBossBar.setProgress(1.0F);
        endBossBar.setCreateWorldFog(false);

        if (boss.level().getServer() != null) {
            for (ServerPlayer player : boss.level().getServer().getPlayerList().getPlayers()) {
                if (Level.END.equals(player.level().dimension())) {
                    endBossBar.addPlayer(player);
                }
            }
        }
    }

    /** 血条进度跟随 Boss 血量；顺便把后来进末地的玩家补进来。 */
    private static void updateEndBossBar(LivingEntity boss) {
        if (endBossBar == null) {
            return;
        }
        float maxHealth = Math.max(1.0F, boss.getMaxHealth());
        endBossBar.setProgress(Mth.clamp(boss.getHealth() / maxHealth, 0.0F, 1.0F));

        if (boss.level().getServer() != null) {
            for (ServerPlayer player : boss.level().getServer().getPlayerList().getPlayers()) {
                if (Level.END.equals(player.level().dimension())) {
                    endBossBar.addPlayer(player);
                }
            }
        }
    }

    private static void stopEndBossBar() {
        if (endBossBar != null) {
            endBossBar.removeAllPlayers();
            endBossBar = null;
        }
    }

    /** 只按生成时打上的标记识别终局 Boss，spore 与旧版回退生物都适用。 */
    private static boolean isEndBossEntity(Entity entity) {
        return entity != null && entity.getPersistentData().getBoolean(END_BOSS_TAG);
    }

    private static void capEndBossFlight(LivingEntity dragon) {
        boolean exceededCeiling = dragon.getY() > END_BOSS_MAX_Y;
        if (exceededCeiling) {
            dragon.setPos(dragon.getX(), END_BOSS_RESET_Y, dragon.getZ());
        }
        Vec3 movement = dragon.getDeltaMovement();
        if ((exceededCeiling || dragon.getY() >= END_BOSS_MAX_Y) && movement.y > 0.0D) {
            dragon.setDeltaMovement(movement.x, 0.0D, movement.z);
        }
    }

    private static void initializeEndEncounter(ServerLevel endLevel) {
        if (endEncounterInitialized || !Level.END.equals(endLevel.dimension())) {
            return;
        }

        suppressVanillaDragonFight(endLevel);

        AABB islandArea = new AABB(
                -1024.0D, endLevel.getMinBuildHeight(), -1024.0D,
                1024.0D, endLevel.getMaxBuildHeight(), 1024.0D);
        endLevel.getEntitiesOfClass(EnderDragon.class, islandArea).forEach(Entity::discard);
        endLevel.getEntitiesOfClass(LivingEntity.class, islandArea,
                mob -> mob.getPersistentData().getBoolean(END_ENCOUNTER_ENTITY_TAG))
                .forEach(Entity::discard);

        ResourceLocation bossId = resolveEndBossId();
        if (bossId == null) {
            Life_contract.LOGGER.error(
                    "Unable to spawn any End boss: none of {} and {} is available",
                    END_BOSS_IDS, LEGACY_END_BOSS_ID);
            return;
        }

        BlockPos islandCenter = SurfaceFinder.findSurfacePos(endLevel, 0, 0).above();
        int dragonY = (int) Math.min(END_BOSS_MAX_Y, Math.max(80, islandCenter.getY() + 30));
        BlockPos dragonPos = new BlockPos(0, dragonY, 0);
        LivingEntity dragon = spawnEncounterMob(endLevel, bossId, dragonPos, true);
        if (dragon == null) {
            Life_contract.LOGGER.error("Unable to spawn the End boss {} in The End", bossId);
            return;
        }
        endBossUuid = dragon.getUUID();
        activeEndBossId = bossId;
        startEndBossBar(dragon, bossId);

        List<BlockPos> endermanColumns = List.of(
                new BlockPos(-5, 0, 0),
                new BlockPos(5, 0, 0),
                new BlockPos(0, 0, 5));
        int spawnedEndermen = 0;
        for (BlockPos column : endermanColumns) {
            BlockPos spawnPos = SurfaceFinder.findSurfacePos(endLevel, column.getX(), column.getZ()).above();
            if (spawnEncounterMob(endLevel, DISTORTED_ENDERMAN_ID, spawnPos, false) != null) {
                spawnedEndermen++;
            }
        }

        endEncounterInitialized = true;
        endLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§5[终局] §f原版末影龙已被 §d" + bossDisplayName(bossId)
                        + " §f取代，末地主岛中央还出现了 §d" + spawnedEndermen + " §f只诡异末影人！"
                        + "§f击杀它即可为你的队伍赢下这局比赛。"),
                false);
    }

    /** 优先使用 spore 的朽翼魔，缺失时退回旧版 Phayriosis 龙。 */
    private static ResourceLocation resolveEndBossId() {
        for (ResourceLocation candidate : END_BOSS_IDS) {
            if (hasEntityType(candidate)) {
                return candidate;
            }
        }
        Life_contract.LOGGER.warn("End boss {} is unavailable, falling back to {}",
                END_BOSS_IDS, LEGACY_END_BOSS_ID);
        return hasEntityType(LEGACY_END_BOSS_ID) ? LEGACY_END_BOSS_ID : null;
    }

    /**
     * 判断实体类型是否真的注册过。
     * <p>
     * 不能用 {@code BuiltInRegistries.ENTITY_TYPE.get(id) != null}：ENTITY_TYPE 是
     * {@code DefaultedRegistry}，默认值是 {@code minecraft:pig}，未注册的 id 会返回<b>猪</b>
     * 而不是 null。那样「不存在」会被误判成「存在」，既不会回退到备用 Boss，
     * 还会真的在末地生成一只猪当终局 Boss。
     */
    private static boolean hasEntityType(ResourceLocation entityId) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        return entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    private static String bossDisplayName(ResourceLocation entityId) {
        return entityId != null && "spore".equals(entityId.getNamespace())
                ? "朽翼魔 Verfalldrache"
                : "诡异末影龙";
    }

    /** 生成终局生物：spore 的 Boss 不一定是原版 Mob，这里只要求是 LivingEntity。 */
    private static LivingEntity spawnEncounterMob(ServerLevel level, ResourceLocation entityId,
                                                  BlockPos spawnPos, boolean boss) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (entityType == null || !entityId.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType))) {
            // 注意：ENTITY_TYPE 是 DefaultedRegistry，未注册的 id 会返回默认的猪，
            // 所以必须比对注册名，否则会"成功地"生成一只猪。
            Life_contract.LOGGER.error("Missing end encounter entity type {}", entityId);
            return null;
        }

        Entity created = entityType.create(level);
        if (!(created instanceof LivingEntity living)) {
            Life_contract.LOGGER.error("End encounter entity type {} did not create a living entity", entityId);
            return null;
        }

        living.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        if (living instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.EVENT, null);
            // 终局 Boss 不能被距离剔除掉，否则玩家一转头它就"没生成"了
            mob.setPersistenceRequired();
        }
        living.getPersistentData().putBoolean(END_ENCOUNTER_ENTITY_TAG, true);
        if (boss) {
            living.getPersistentData().putBoolean(END_BOSS_TAG, true);
        }
        if (!level.addFreshEntity(living)) {
            living.discard();
            return null;
        }
        return living;
    }

    @SubscribeEvent
    public static void onDragonDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()
                || !isEndBossEntity(event.getEntity())
                || !GameEventManager.isGameActive()) {
            return;
        }

        stopEndBossBar();

        // 击杀归属：最后一下的玩家，或最后一击来源的玩家（环境伤害不计）
        ServerPlayer killer = resolvePlayerKiller(event, event.getEntity());
        if (killer == null) {
            Life_contract.LOGGER.info("End boss died without a player kill credit; match continues");
            return;
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        GameEventManager.declareDragonWinner(killer,
                activeEndBossId != null ? activeEndBossId : entityId);
    }

    private static ServerPlayer resolvePlayerKiller(LivingDeathEvent event, LivingEntity victim) {
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        LivingEntity killCredit = victim.getKillCredit();
        return killCredit instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    public static BlockPos getPortalCenter() {
        return portalCenter;
    }

    public static int getMissingFrameIndex() {
        return missingFrameIndex;
    }

    public static boolean isPortalActivated() {
        return portalActivated;
    }

    public static void clearSession() {
        activeLevel = null;
        portalCenter = null;
        portalFrames = List.of();
        portalInterior = List.of();
        missingFrameIndex = -1;
        portalActivated = false;
        endEncounterInitialized = false;
        endBossUuid = null;
        activeEndBossId = null;
        stopEndBossBar();
    }

    public record PreparationResult(boolean success, BlockPos portalCenter, String message) {
        private static PreparationResult success(BlockPos portalCenter, String message) {
            return new PreparationResult(true, portalCenter, message);
        }

        private static PreparationResult failure(String message) {
            return new PreparationResult(false, null, message);
        }
    }

    private record LocalPortalPos(int x, int y, int z) {
    }
}
