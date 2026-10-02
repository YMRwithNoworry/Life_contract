package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketSyncDeadTeammates implements CustomPacketPayload {
    public static final Type<PacketSyncDeadTeammates> TYPE = NetworkHandler.type("sync_dead_teammates");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncDeadTeammates> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> encode(packet, buffer), PacketSyncDeadTeammates::decode);

    private final List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates;

    public PacketSyncDeadTeammates(List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates) {
        this.deadTeammates = deadTeammates;
    }

    public static void encode(PacketSyncDeadTeammates msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.deadTeammates.size());
        for (ReviveTeammateSystem.DeadTeammateInfo info : msg.deadTeammates) {
            buffer.writeUUID(info.getUuid());
            buffer.writeUtf(info.getName());
            buffer.writeLong(info.getDeathTime());
            buffer.writeDouble(info.getDeathX());
            buffer.writeDouble(info.getDeathY());
            buffer.writeDouble(info.getDeathZ());
            buffer.writeResourceLocation(info.getDimension());
        }
    }

    public static PacketSyncDeadTeammates decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UUID uuid = buffer.readUUID();
            String name = buffer.readUtf();
            long deathTime = buffer.readLong();
            double deathX = buffer.readDouble();
            double deathY = buffer.readDouble();
            double deathZ = buffer.readDouble();
            ResourceLocation dimension = buffer.readResourceLocation();
            deadTeammates.add(new ReviveTeammateSystem.DeadTeammateInfo(
                uuid, name, deathTime, deathX, deathY, deathZ, dimension
            ));
        }
        return new PacketSyncDeadTeammates(deadTeammates);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncDeadTeammates packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                java.lang.reflect.Method method = proxyClass.getMethod("openReviveScreen", java.util.List.class);
                method.invoke(null, packet.deadTeammates);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public List<ReviveTeammateSystem.DeadTeammateInfo> getDeadTeammates() {
        return deadTeammates;
    }
}
