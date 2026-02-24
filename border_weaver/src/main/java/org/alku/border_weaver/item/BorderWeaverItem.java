package org.alku.border_weaver.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.alku.border_weaver.Border_weaver;

import java.util.ArrayList;
import java.util.List;

public class BorderWeaverItem extends Item {
    public BorderWeaverItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();
            CompoundTag nbt = stack.getOrCreateTag();

            if (!nbt.contains("Pos1")) {
                nbt.putLong("Pos1", pos.asLong());
                context.getPlayer().sendSystemMessage(Component.literal("§b[Border Weaver]§r 第一个点已设置: " + pos.toShortString()));
            } else {
                long pos1Long = nbt.getLong("Pos1");
                BlockPos pos1 = BlockPos.of(pos1Long);
                BlockPos pos2 = pos;

                updateWorldBorder(level, pos1, pos2, context.getPlayer());
                nbt.remove("Pos1");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                WorldBorder border = level.getWorldBorder();
                border.setSize(6.0E7D);
                border.setCenter(0.5D, 0.5D);
                stack.getOrCreateTag().remove("Pos1");
                player.sendSystemMessage(Component.literal("§c[Border Weaver]§r 边界已重置为默认大小。"));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    private void updateWorldBorder(Level level, BlockPos pos1, BlockPos pos2, Player player) {
        // 计算包含两个点击点的最小矩形
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // 计算尺寸：坐标差的绝对值 + 1 (确保包含选中的方块)
        // 例如：点击 0 和 10，距离是 10，但包含 0-10 共 11 个方块
        double sizeX = (maxX - minX) + 1.0;
        double sizeZ = (maxZ - minZ) + 1.0;

        // 世界边界必须是正方形，取最大边
        double size = Math.max(sizeX, sizeZ);

        // 计算中心点：最小值 + 半径
        double centerX = minX + (sizeX / 2.0);
        double centerZ = minZ + (sizeZ / 2.0);

        WorldBorder border = level.getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(size);

        if (level instanceof ServerLevel serverLevel) {
            try {
                // 使用安全的列表收集方式清理实体
                List<Entity> toRemove = new ArrayList<>();
                int count = 0;

                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                        if (!border.isWithinBounds(entity.getX(), entity.getZ())) {
                            toRemove.add(entity);
                            count++;
                        }
                    }
                }

                toRemove.forEach(Entity::discard);
                player.sendSystemMessage(Component.literal("§a[Border Weaver]§r 边界已划定！中心: (" + String.format("%.1f", centerX) + ", " + String.format("%.1f", centerZ) + ") 大小: " + (int)size + "。清理: " + count));
            } catch (Exception e) {
                Border_weaver.LOGGER.error("工具划界清理实体失败: " + e.getMessage());
            }
        }
    }
}