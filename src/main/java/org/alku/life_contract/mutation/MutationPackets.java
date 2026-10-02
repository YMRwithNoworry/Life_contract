package org.alku.life_contract.mutation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.client.UpgradeHubUIHolder;
import org.alku.life_contract.client.MutationUIHolder;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;

public final class MutationPackets {
    private MutationPackets() {
    }

    public static void open(ServerPlayer player) {
        PlayerUIMenuType.openUI(player, MutationUIHolder.UI_ID);
    }

    public static final class Open implements CustomPacketPayload {
        public static final Type<Open> TYPE = NetworkHandler.type("mutation_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> STREAM_CODEC =
                NetworkHandler.codec((buffer, packet) -> packet.encode(buffer), Open::new);

        public Open() {
        }

        public Open(FriendlyByteBuf buffer) {
        }

        public void encode(FriendlyByteBuf buffer) {
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(Open packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    PlayerUIMenuType.openUI(player, UpgradeHubUIHolder.UI_ID);
                }
            });
        }
    }

    public static final class Upgrade implements CustomPacketPayload {
        public static final Type<Upgrade> TYPE = NetworkHandler.type("mutation_upgrade");
        public static final StreamCodec<RegistryFriendlyByteBuf, Upgrade> STREAM_CODEC =
                NetworkHandler.codec((buffer, packet) -> packet.encode(buffer), Upgrade::new);

        private final MutationNode node;

        public Upgrade(MutationNode node) {
            this.node = node;
        }

        public Upgrade(FriendlyByteBuf buffer) {
            int ordinal = buffer.readVarInt();
            node = ordinal >= 0 && ordinal < MutationNode.values().length
                    ? MutationNode.values()[ordinal]
                    : MutationNode.BLADE;
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeVarInt(node.ordinal());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(Upgrade packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.literal("§6[异变] §f" + MutationService.upgrade(player, packet.node)));
                }
            });
        }
    }
}
