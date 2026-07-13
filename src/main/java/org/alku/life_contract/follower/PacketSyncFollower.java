package org.alku.life_contract.follower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncFollower {
    private static final UUID NO_OWNER = new UUID(0L, 0L);
    private static Method registerFollowerMethod;
    private static Method unregisterFollowerMethod;
    private final UUID entityUUID;
    private final int entityId;
    private final UUID ownerUUID;
    private final boolean isRegister;

    public PacketSyncFollower(UUID entityUUID, int entityId, UUID ownerUUID, boolean isRegister) {
        this.entityUUID = entityUUID;
        this.entityId = entityId;
        this.ownerUUID = ownerUUID != null ? ownerUUID : NO_OWNER;
        this.isRegister = isRegister;
    }

    public PacketSyncFollower(FriendlyByteBuf buffer) {
        this.entityUUID = buffer.readUUID();
        this.entityId = buffer.readVarInt();
        this.ownerUUID = buffer.readUUID();
        this.isRegister = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(entityUUID);
        buffer.writeVarInt(entityId);
        buffer.writeUUID(ownerUUID);
        buffer.writeBoolean(isRegister);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    if (isRegister) {
                        getRegisterFollowerMethod().invoke(null, entityUUID, entityId, ownerUUID);
                    } else {
                        getUnregisterFollowerMethod().invoke(null, entityUUID);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }

    private static Method getRegisterFollowerMethod() throws ReflectiveOperationException {
        if (registerFollowerMethod == null) {
            Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
            registerFollowerMethod = proxyClass.getMethod(
                    "registerFollower", UUID.class, int.class, UUID.class);
        }
        return registerFollowerMethod;
    }

    private static Method getUnregisterFollowerMethod() throws ReflectiveOperationException {
        if (unregisterFollowerMethod == null) {
            Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
            unregisterFollowerMethod = proxyClass.getMethod("unregisterFollower", UUID.class);
        }
        return unregisterFollowerMethod;
    }
}
