package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketFoolStealProfession {

    private UUID targetUUID;

    public PacketFoolStealProfession(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public PacketFoolStealProfession() {
        this.targetUUID = null;
    }

    public static void encode(PacketFoolStealProfession msg, FriendlyByteBuf buffer) {
        if (msg.targetUUID != null) {
            buffer.writeBoolean(true);
            buffer.writeUUID(msg.targetUUID);
        } else {
            buffer.writeBoolean(false);
        }
    }

    public static PacketFoolStealProfession decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return new PacketFoolStealProfession(buffer.readUUID());
        }
        return new PacketFoolStealProfession();
    }

    public static void handle(PacketFoolStealProfession msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                FoolSystem.stealProfession(player, msg.targetUUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
