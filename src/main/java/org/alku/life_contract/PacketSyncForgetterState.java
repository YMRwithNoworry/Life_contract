package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncForgetterState {
    private UUID playerUUID;
    private boolean isInvisible;
    private int remainingTicks;

    public PacketSyncForgetterState(UUID playerUUID, boolean isInvisible, int remainingTicks) {
        this.playerUUID = playerUUID;
        this.isInvisible = isInvisible;
        this.remainingTicks = remainingTicks;
    }

    public PacketSyncForgetterState(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.isInvisible = buffer.readBoolean();
        this.remainingTicks = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerUUID);
        buffer.writeBoolean(this.isInvisible);
        buffer.writeInt(this.remainingTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("syncForgetterState", UUID.class, boolean.class, int.class)
                            .invoke(null, playerUUID, isInvisible, remainingTicks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
