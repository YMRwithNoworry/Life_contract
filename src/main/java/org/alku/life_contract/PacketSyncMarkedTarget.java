package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncMarkedTarget {

    private final UUID targetUUID;
    private final String targetName;
    private final int posX;
    private final int posY;
    private final int posZ;

    public PacketSyncMarkedTarget(UUID targetUUID, String targetName, int posX, int posY, int posZ) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static void encode(PacketSyncMarkedTarget msg, FriendlyByteBuf buffer) {
        if (msg.targetUUID != null) {
            buffer.writeBoolean(true);
            buffer.writeUUID(msg.targetUUID);
        } else {
            buffer.writeBoolean(false);
        }
        buffer.writeUtf(msg.targetName != null ? msg.targetName : "");
        buffer.writeInt(msg.posX);
        buffer.writeInt(msg.posY);
        buffer.writeInt(msg.posZ);
    }

    public static PacketSyncMarkedTarget decode(FriendlyByteBuf buffer) {
        boolean hasUUID = buffer.readBoolean();
        UUID targetUUID = hasUUID ? buffer.readUUID() : null;
        String targetName = buffer.readUtf();
        int posX = buffer.readInt();
        int posY = buffer.readInt();
        int posZ = buffer.readInt();
        return new PacketSyncMarkedTarget(targetUUID, targetName, posX, posY, posZ);
    }

    public static void handle(PacketSyncMarkedTarget msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientDataStorage.setMarkedTarget(msg.targetUUID, msg.targetName, msg.posX, msg.posY, msg.posZ);
        });
        context.setPacketHandled(true);
    }
}
