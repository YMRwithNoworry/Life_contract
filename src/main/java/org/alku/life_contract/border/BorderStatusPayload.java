package org.alku.life_contract.border;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.alku.life_contract.NetworkHandler;

public record BorderStatusPayload(boolean active, boolean inside, String direction, int distance,
                                  float dirX, float dirZ)
        implements CustomPacketPayload {
    /** 边界提示未生效时的空闲状态。 */
    public static final BorderStatusPayload INACTIVE = new BorderStatusPayload(false, true, "", 0, 0.0F, 0.0F);

    public static final Type<BorderStatusPayload> TYPE = NetworkHandler.type("border_status");
    public static final StreamCodec<RegistryFriendlyByteBuf, BorderStatusPayload> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), BorderStatusPayload::new);

    public BorderStatusPayload(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean(), buffer.readUtf(8), buffer.readVarInt(),
                buffer.readFloat(), buffer.readFloat());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(active);
        buffer.writeBoolean(inside);
        buffer.writeUtf(direction, 8);
        buffer.writeVarInt(distance);
        buffer.writeFloat(dirX);
        buffer.writeFloat(dirZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
