package org.alku.life_contract.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DeepOreGenerationHandler {
    private static final int SOURCE_MIN_Y = -64;
    private static final int SOURCE_MAX_Y = -1;
    private static final int Y_OFFSET = -64;
    private static final int CHUNKS_PER_TICK = 4;
    private static final int MAX_READY_RETRIES = 200;

    private static final Queue<PendingChunk> PENDING_CHUNKS = new ConcurrentLinkedQueue<>();
    private static final Map<Block, Block> DEEPSLATE_VARIANTS = Map.ofEntries(
            Map.entry(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
            Map.entry(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
            Map.entry(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
            Map.entry(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE),
            Map.entry(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
            Map.entry(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
            Map.entry(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
            Map.entry(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE));

    private DeepOreGenerationHandler() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()
                || !(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || level.getMinBuildHeight() > -128) {
            return;
        }

        PENDING_CHUNKS.add(new PendingChunk(level, event.getChunk().getPos(), 0));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (int index = 0; index < CHUNKS_PER_TICK; index++) {
            PendingChunk pending = PENDING_CHUNKS.poll();
            if (pending == null) {
                return;
            }

            LevelChunk chunk = pending.level().getChunkSource().getChunkNow(pending.pos().x, pending.pos().z);
            if (chunk == null) {
                if (pending.readyRetries() < MAX_READY_RETRIES) {
                    PENDING_CHUNKS.add(pending.retry());
                }
                continue;
            }

            copyOreDistribution(pending.level(), chunk);
        }
    }

    private static void copyOreDistribution(ServerLevel level, LevelChunk chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos sourcePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = minX + localX;
                int z = minZ + localZ;
                for (int sourceY = SOURCE_MIN_Y; sourceY <= SOURCE_MAX_Y; sourceY++) {
                    sourcePos.set(x, sourceY, z);
                    BlockState sourceState = chunk.getBlockState(sourcePos);
                    if (!sourceState.is(Tags.Blocks.ORES)) {
                        continue;
                    }

                    targetPos.set(x, sourceY + Y_OFFSET, z);
                    BlockState targetState = chunk.getBlockState(targetPos);
                    if (!targetState.is(BlockTags.BASE_STONE_OVERWORLD)) {
                        continue;
                    }

                    Block deepslateVariant = DEEPSLATE_VARIANTS.get(sourceState.getBlock());
                    BlockState oreState = deepslateVariant == null
                            ? sourceState
                            : deepslateVariant.defaultBlockState();
                    level.setBlock(targetPos, oreState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PENDING_CHUNKS.clear();
    }

    private record PendingChunk(ServerLevel level, ChunkPos pos, int readyRetries) {
        private PendingChunk retry() {
            return new PendingChunk(level, pos, readyRetries + 1);
        }
    }
}
