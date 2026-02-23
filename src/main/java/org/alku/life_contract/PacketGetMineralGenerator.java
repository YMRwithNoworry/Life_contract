package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketGetMineralGenerator {
    private final BlockPos pos;

    public PacketGetMineralGenerator(BlockPos pos) {
        this.pos = pos;
    }

    public PacketGetMineralGenerator(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level() instanceof ServerLevel level) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof MineralGeneratorBlockEntity generator) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new PacketSyncMineralGenerator(pos, generator.getMineralType().name(),
                                    generator.getInterval(), generator.isEnabled(), generator.getLastTick(), level.getGameTime()));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
