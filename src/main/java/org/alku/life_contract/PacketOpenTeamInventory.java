package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import org.alku.life_contract.client.TeamInventoryUIHolder;

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
            TeamInventory inventory = TeamInventory.getOrCreate(player);
            NetworkHandler.sendToPlayer(player, new PacketSyncTeamInventory(inventory.getTeamId(), inventory.getItems()));
            PlayerUIMenuType.openUI(player, TeamInventoryUIHolder.UI_ID);
        });
    }
}
