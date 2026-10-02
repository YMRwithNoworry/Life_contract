package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 把本局的关键坐标发给客户端：末地传送门与本局边界中心，用于左侧 HUD 的坐标显示。
 */
public record PacketSyncWaypoints(boolean active,
                                  int portalX, int portalY, int portalZ, boolean portalActivated,
                                  int borderCenterX, int borderCenterZ)
        implements CustomPacketPayload {
    /** 没有可显示坐标时的空闲状态。 */
    public static final PacketSyncWaypoints INACTIVE =
            new PacketSyncWaypoints(false, 0, 0, 0, false, 0, 0);

    public static final Type<PacketSyncWaypoints> TYPE = NetworkHandler.type("sync_waypoints");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncWaypoints> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), PacketSyncWaypoints::new);

    public PacketSyncWaypoints(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readBoolean(),
                buffer.readInt(), buffer.readInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(active);
        buffer.writeInt(portalX);
        buffer.writeInt(portalY);
        buffer.writeInt(portalZ);
        buffer.writeBoolean(portalActivated);
        buffer.writeInt(borderCenterX);
        buffer.writeInt(borderCenterZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
