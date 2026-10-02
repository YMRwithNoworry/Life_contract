package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

public class PacketSkipRevive implements CustomPacketPayload {
    public static final Type<PacketSkipRevive> TYPE = NetworkHandler.type("skip_revive");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSkipRevive> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> encode(packet, buffer), PacketSkipRevive::decode);

    public PacketSkipRevive() {
    }

    public static void encode(PacketSkipRevive msg, FriendlyByteBuf buffer) {
    }

    public static PacketSkipRevive decode(FriendlyByteBuf buffer) {
        return new PacketSkipRevive();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSkipRevive packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ReviveTeammateSystem.skipRevive(player);
            }
        });
    }
}
