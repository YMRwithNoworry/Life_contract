package org.alku.life_contract.airdrop.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * NeoForge 1.21.1 网络处理器
 * 替代旧版 Forge SimpleChannel，使用 CustomPacketPayload + PayloadRegistrar。
 */
public class NetworkHandler {

    /**
     * 在 RegisterPayloadHandlersEvent 中注册数据包。
     * 由 Airdrop 主类在 MOD 总线上调用。
     */
    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        AirdropPayload.TYPE,
                        AirdropPayload.STREAM_CODEC,
                        (payload, context) -> {
                            // 客户端处理：设置追踪目标
                            context.enqueueWork(() -> {
                                AirdropTrackClientHandler.setTarget(payload);
                            });
                        });
    }

    /**
     * 向指定玩家发送追踪数据包
     */
    public static void sendToPlayer(ServerPlayer player, AirdropPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
