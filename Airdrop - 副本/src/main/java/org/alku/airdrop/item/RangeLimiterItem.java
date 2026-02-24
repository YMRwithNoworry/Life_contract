package org.alku.airdrop.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.alku.airdrop.data.AirdropSavedData;

public class RangeLimiterItem extends Item {
    public RangeLimiterItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide || player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            AirdropSavedData.get((ServerLevel) level).clearRange();
            player.getPersistentData().remove("AirdropRangeFirstPos");
            player.sendSystemMessage(Component.literal("§c[System] Range limit cleared."));
            return InteractionResult.SUCCESS;
        }

        if (!player.getPersistentData().contains("AirdropRangeFirstPos")) {
            player.getPersistentData().putLong("AirdropRangeFirstPos", pos.asLong());
            player.sendSystemMessage(Component.literal("§e[Setup] Point A set (" + pos.toShortString() + "). Click Point B."));
        } else {
            long firstPosLong = player.getPersistentData().getLong("AirdropRangeFirstPos");
            BlockPos firstPos = BlockPos.of(firstPosLong);
            AABB area = new AABB(firstPos, pos);
            AirdropSavedData.get((ServerLevel) level).setRange(area);
            player.getPersistentData().remove("AirdropRangeFirstPos");
            player.sendSystemMessage(Component.literal("§a[Setup] Range locked! (" +
                    (int)area.minX + "," + (int)area.minZ + " to " + (int)area.maxX + "," + (int)area.maxZ + ")"));
        }
        return InteractionResult.SUCCESS;
    }
}