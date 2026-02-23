package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncFollowerHunger {
    private UUID playerUUID;
    private int followerCount;
    private float hungerMultiplier;

    public PacketSyncFollowerHunger(UUID playerUUID, int followerCount, float hungerMultiplier) {
        this.playerUUID = playerUUID;
        this.followerCount = followerCount;
        this.hungerMultiplier = hungerMultiplier;
    }

    public PacketSyncFollowerHunger(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.followerCount = buffer.readInt();
        this.hungerMultiplier = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerUUID);
        buffer.writeInt(this.followerCount);
        buffer.writeFloat(this.hungerMultiplier);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("syncFollowerHunger", UUID.class, int.class, float.class)
                            .invoke(null, playerUUID, followerCount, hungerMultiplier);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
