package org.alku.life_contract.airdrop.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.Life_contract;

/**
 * NeoForge 1.21.1 自定义数据包：服务端→客户端
 * 通知玩家当前追踪的空投目标位置。
 * 替代旧版 Forge SimpleChannel 中的 AirdropTrackPacket。
 */
public record AirdropPayload(boolean hasTarget, double targetX, double targetY, double targetZ)
        implements CustomPacketPayload {

    public static final Type<AirdropPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "track"));

    public static final StreamCodec<FriendlyByteBuf, AirdropPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AirdropPayload decode(FriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return new AirdropPayload(true, buf.readDouble(), buf.readDouble(), buf.readDouble());
            }
            return new AirdropPayload(false, 0.0, 0.0, 0.0);
        }

        @Override
        public void encode(FriendlyByteBuf buf, AirdropPayload payload) {
            buf.writeBoolean(payload.hasTarget);
            if (payload.hasTarget) {
                buf.writeDouble(payload.targetX);
                buf.writeDouble(payload.targetY);
                buf.writeDouble(payload.targetZ);
            }
        }
    };

    /** 清除追踪的便捷构造 */
    public static AirdropPayload clear() {
        return new AirdropPayload(false, 0.0, 0.0, 0.0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
