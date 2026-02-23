package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetMineralGenerator {
    private final BlockPos pos;
    private final String mineralType;
    private final int interval;
    private final boolean enabled;

    public PacketSetMineralGenerator(BlockPos pos, String mineralType, int interval, boolean enabled) {
        this.pos = pos;
        this.mineralType = mineralType;
        this.interval = interval;
        this.enabled = enabled;
    }

    public PacketSetMineralGenerator(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.mineralType = buf.readUtf();
        this.interval = buf.readInt();
        this.enabled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(mineralType);
        buf.writeInt(interval);
        buf.writeBoolean(enabled);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level() instanceof ServerLevel level) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof MineralGeneratorBlockEntity generator) {
                    try {
                        MineralGeneratorBlockEntity.MineralType type = MineralGeneratorBlockEntity.MineralType.valueOf(mineralType);
                        generator.setMineralType(type);
                    } catch (IllegalArgumentException e) {
                    }
                    if (interval > 0) {
                        generator.setInterval(interval);
                    }
                    generator.setEnabled(enabled);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
