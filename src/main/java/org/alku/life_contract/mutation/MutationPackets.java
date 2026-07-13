package org.alku.life_contract.mutation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public final class MutationPackets {
    private MutationPackets() {
    }

    public static void open(ServerPlayer player) {
        MutationSavedData.TeamState state = MutationService.state(player);
        int availableMp = MutationService.availableMp(player);
        NetworkHooks.openScreen(player, new net.minecraft.world.MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("阵营异变树");
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                    int id,
                    net.minecraft.world.entity.player.Inventory inventory,
                    net.minecraft.world.entity.player.Player menuPlayer) {
                return new MutationMenu(id, inventory, state, availableMp);
            }
        }, buffer -> {
            buffer.writeVarInt(availableMp);
            buffer.writeVarInt(state.totalLevels());
            for (MutationNode node : MutationNode.values()) {
                buffer.writeVarInt(state.level(node));
            }
        });
    }

    public static final class Open {
        public Open() {
        }

        public Open(FriendlyByteBuf buffer) {
        }

        public void encode(FriendlyByteBuf buffer) {
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    open(context.getSender());
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static final class Upgrade {
        private final MutationNode node;

        public Upgrade(MutationNode node) {
            this.node = node;
        }

        public Upgrade(FriendlyByteBuf buffer) {
            node = MutationNode.values()[buffer.readVarInt()];
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeVarInt(node.ordinal());
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    player.sendSystemMessage(Component.literal("§6[异变] §f" + MutationService.upgrade(player, node)));
                    open(player);
                }
            });
            context.setPacketHandled(true);
        }
    }
}
