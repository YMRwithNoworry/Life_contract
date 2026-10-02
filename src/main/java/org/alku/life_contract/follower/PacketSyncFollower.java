package org.alku.life_contract.follower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

import java.lang.reflect.Method;
import java.util.UUID;

public class PacketSyncFollower implements CustomPacketPayload {
    public static final Type<PacketSyncFollower> TYPE = NetworkHandler.type("sync_follower");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncFollower> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> packet.encode(buffer), PacketSyncFollower::new);

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncFollower packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (packet.isRegister) {
                    getRegisterFollowerMethod().invoke(null, packet.entityUUID, packet.entityId, packet.ownerUUID);
                } else {
                    getUnregisterFollowerMethod().invoke(null, packet.entityUUID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
