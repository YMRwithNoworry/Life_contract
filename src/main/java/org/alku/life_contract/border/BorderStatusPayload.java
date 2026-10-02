package org.alku.life_contract.border;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.alku.life_contract.NetworkHandler;

public record BorderStatusPayload(boolean active, boolean inside, String direction, int distance)
        implements CustomPacketPayload {
    public static final Type<BorderStatusPayload> TYPE = NetworkHandler.type("border_status");
    public static final StreamCodec<RegistryFriendlyByteBuf, BorderStatusPayload> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), BorderStatusPayload::new);

    public BorderStatusPayload(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean(), buffer.readUtf(8), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(active);
        buffer.writeBoolean(inside);
        buffer.writeUtf(direction, 8);
        buffer.writeVarInt(distance);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
