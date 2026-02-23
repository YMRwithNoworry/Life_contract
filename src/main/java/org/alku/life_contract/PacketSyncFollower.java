package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncFollower {
    private final UUID entityUUID;
    private final UUID ownerUUID;
    private final boolean isRegister;

    public PacketSyncFollower(UUID entityUUID, UUID ownerUUID, boolean isRegister) {
        this.entityUUID = entityUUID;
        this.ownerUUID = ownerUUID;
        this.isRegister = isRegister;
    }

    public PacketSyncFollower(FriendlyByteBuf buffer) {
        this.entityUUID = buffer.readUUID();
        this.ownerUUID = buffer.readUUID();
        this.isRegister = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(entityUUID);
        buffer.writeUUID(ownerUUID);
        buffer.writeBoolean(isRegister);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    if (isRegister) {
                        proxyClass.getMethod("registerFollower", UUID.class, UUID.class)
                                .invoke(null, entityUUID, ownerUUID);
                    } else {
                        proxyClass.getMethod("unregisterFollower", UUID.class)
                                .invoke(null, entityUUID);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
