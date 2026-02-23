package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenTeamInventory {

    public PacketOpenTeamInventory() {
    }

    public PacketOpenTeamInventory(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                TeamInventory inventory = TeamInventory.getOrCreate(player);
                player.openMenu(new SimpleMenuProvider(
                        (windowId, inv, p) -> new TeamInventoryMenu(windowId, inv, inventory),
                        net.minecraft.network.chat.Component.translatable("container.life_contract.team_inventory")
                ));
            }
        });
        context.setPacketHandled(true);
    }
}
