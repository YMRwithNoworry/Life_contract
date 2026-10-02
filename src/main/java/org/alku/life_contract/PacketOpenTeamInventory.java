package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.UUID;

public class PacketOpenTeamInventory implements CustomPacketPayload {
    public static final Type<PacketOpenTeamInventory> TYPE = NetworkHandler.type("open_team_inventory");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenTeamInventory> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> packet.encode(buffer), PacketOpenTeamInventory::new);

    public PacketOpenTeamInventory() {
    }

    public PacketOpenTeamInventory(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketOpenTeamInventory packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player != null) {
                TeamInventory inventory = TeamInventory.getOrCreate(player);
                UUID teamId = inventory.getTeamId();
                
                player.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("container.life_contract.team_inventory");
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                        return new TeamInventoryMenu(windowId, playerInventory, inventory, teamId);
                    }
                });
            }
        });
    }
}
