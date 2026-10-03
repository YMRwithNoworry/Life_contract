package org.alku.life_contract.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 地表定位：把"找地表"这件事从"信任高度图"改成"用真实方块校验"。
 *
 * <p>为什么不直接用 {@code level.getHeightmapPos(...)}：
 * <ul>
 *   <li><b>区块没加载时结果会退化。</b>{@code Level#getHeight} 在
 *       {@code hasChunk} 为假时直接返回 {@code getMinBuildHeight()}，
 *       平台/气泡就会被丢到世界底部。</li>
 *   <li><b>高度图的原始数据是相对 {@code minBuildHeight} 存的。</b>
 *       {@code Heightmap#setHeight} 写入 {@code y - minBuildHeight}，
 *       {@code getFirstAvailable} 再读回 {@code data + minBuildHeight}。
 *       本模组早期用 Mixin 把主世界从 {@code minY=-64 / height=384} 扩成
 *       {@code minY=-128 / height=448}，之后又移除了该扩展。两种高度算出来的位宽
 *       恰好都是 9 bit、数组长度都是 36，{@code Heightmap#setRawData} 的长度校验
 *       因此会通过 —— 旧存档里按 {@code -128} 存的高度图会被按 {@code -64}
 *       原样读回来，整列偏高 64 格，表现就是平台与安全气泡"生成在天上"。</li>
 * </ul>
 * 因此这里只把高度图当作<b>搜索起点</b>，最终结果一定用真实方块确认：
 * 既能在高度图偏高时往下落回真实地表，也能在高度图偏低时从世界顶部重扫。
 */
public final class SurfaceFinder {

    private SurfaceFinder() {
    }

    /**
     * 求某一列"地面之上第一格空气"的 Y。
     * <p>
     * 语义与 {@code Level#getHeight} 一致（返回的是可站立的那一格，而不是地面方块本身），
     * 但保证区块已加载、且结果经过真实方块校验。
     */
    public static int findSurfaceY(ServerLevel level, int x, int z) {
        // 区块必须先加载：否则高度图和方块都读不到真实值
        level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        int lowest = minY + 1;
        int highest = maxY - 2;

        int hint = Mth.clamp(
                level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z),
                lowest, highest);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        // 从高度图给的起点往下找：起点偏高（旧存档基准变了）时会落回真实地表
        for (int y = hint; y >= lowest; y--) {
            cursor.set(x, y, z);
            if (isSurfaceAir(level, cursor)) {
                return y;
            }
        }
        // 起点偏低（区块刚生成、高度图还没算出来）时，从世界顶部重扫一遍
        for (int y = highest; y > hint; y--) {
            cursor.set(x, y, z);
            if (isSurfaceAir(level, cursor)) {
                return y;
            }
        }
        return lowest;
    }

    /** 便捷方法：直接给出可站立的那一格坐标。 */
    public static BlockPos findSurfacePos(ServerLevel level, int x, int z) {
        return new BlockPos(x, findSurfaceY(level, x, z), z);
    }

    /** 这一格是"地表空气"：自己是空的，且脚下是地面（树叶不算地面，和原版高度图口径一致）。 */
    private static boolean isSurfaceAir(ServerLevel level, BlockPos pos) {
        if (!isOpen(level, pos)) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (ground.getBlock() instanceof LeavesBlock) {
            return false;
        }
        if (!ground.getCollisionShape(level, below).isEmpty()) {
            return true;
        }
        // 水面也算地表（与原版 MOTION_BLOCKING_NO_LEAVES 口径一致）
        return !ground.getFluidState().isEmpty();
    }

    /** 该格既没有碰撞箱也不是流体，才算"能站人的空气"。 */
    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }
}
