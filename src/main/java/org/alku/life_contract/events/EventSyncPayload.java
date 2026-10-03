package org.alku.life_contract.events;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.alku.life_contract.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 把游戏事件状态同步到客户端：事件 HUD 与安全气泡渲染都依赖它。
 */
public record EventSyncPayload(boolean gameActive,
                               boolean sporeSurgeActive, int sporeSurgeRemaining,
                               boolean purificationRiftActive, int safeBubbleRemaining, List<Bubble> bubbles,
                               boolean bountyActive, String bountyTargetName,
                               boolean endgameOverloadActive)
        implements CustomPacketPayload {

    /** 安全气泡的客户端数据：坐标、半径与配色索引。 */
    public record Bubble(int x, int y, int z, float radius, int colorIndex) {
    }

    public static final Type<EventSyncPayload> TYPE = NetworkHandler.type("event_status");
    public static final StreamCodec<RegistryFriendlyByteBuf, EventSyncPayload> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), EventSyncPayload::new);

    public EventSyncPayload(FriendlyByteBuf buffer) {
        this(
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                readBubbles(buffer),
                buffer.readBoolean(),
                buffer.readUtf(32),
                buffer.readBoolean());
    }

    private static List<Bubble> readBubbles(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<Bubble> bubbles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            bubbles.add(new Bubble(
                    buffer.readInt(), buffer.readInt(), buffer.readInt(),
                    buffer.readFloat(), buffer.readVarInt()));
        }
        return bubbles;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(gameActive);
        buffer.writeBoolean(sporeSurgeActive);
        buffer.writeVarInt(sporeSurgeRemaining);
        buffer.writeBoolean(purificationRiftActive);
        buffer.writeVarInt(safeBubbleRemaining);

        buffer.writeVarInt(bubbles.size());
        for (Bubble bubble : bubbles) {
            buffer.writeInt(bubble.x());
            buffer.writeInt(bubble.y());
            buffer.writeInt(bubble.z());
            buffer.writeFloat(bubble.radius());
            buffer.writeVarInt(bubble.colorIndex());
        }

        buffer.writeBoolean(bountyActive);
        buffer.writeUtf(bountyTargetName, 32);
        buffer.writeBoolean(endgameOverloadActive);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
