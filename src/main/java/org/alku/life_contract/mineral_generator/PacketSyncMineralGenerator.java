package org.alku.life_contract.mineral_generator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.ClientDataStorage;

import java.util.function.Supplier;

public class PacketSyncMineralGenerator {
    private final BlockPos pos;
    private final String mineralType;
    private final int interval;
    private final boolean enabled;
    private final long lastTick;
    private final long serverTick;

    public PacketSyncMineralGenerator(BlockPos pos, String mineralType, int interval, boolean enabled, long lastTick, long serverTick) {
        this.pos = pos;
        this.mineralType = mineralType;
        this.interval = interval;
        this.enabled = enabled;
        this.lastTick = lastTick;
        this.serverTick = serverTick;
    }

    public PacketSyncMineralGenerator(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.mineralType = buf.readUtf();
        this.interval = buf.readInt();
        this.enabled = buf.readBoolean();
        this.lastTick = buf.readLong();
        this.serverTick = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(mineralType);
        buf.writeInt(interval);
        buf.writeBoolean(enabled);
        buf.writeLong(lastTick);
        buf.writeLong(serverTick);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("syncMineralGeneratorData", BlockPos.class, String.class, 
                            int.class, boolean.class, long.class, long.class)
                            .invoke(null, pos, mineralType, interval, enabled, lastTick, serverTick);
                    proxyClass.getMethod("updateMineralGeneratorScreen", String.class, int.class, boolean.class)
                            .invoke(null, mineralType, interval, enabled);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
