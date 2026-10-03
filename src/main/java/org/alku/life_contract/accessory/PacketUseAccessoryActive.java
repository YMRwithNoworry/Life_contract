package org.alku.life_contract.accessory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

/** 客户端按 G 键 -> 服务端释放饰品主动技。 */
public class PacketUseAccessoryActive implements CustomPacketPayload {

    public static final PacketUseAccessoryActive INSTANCE = new PacketUseAccessoryActive();

    public static final Type<PacketUseAccessoryActive> TYPE = NetworkHandler.type("accessory_active");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUseAccessoryActive> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> {
            }, buffer -> INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketUseAccessoryActive packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AccessoryActiveSkills.use(player);
            }
        });
    }
}
