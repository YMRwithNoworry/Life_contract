package org.alku.life_contract;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class PacketSyncTeamInventory implements CustomPacketPayload {
    public static final Type<PacketSyncTeamInventory> TYPE = NetworkHandler.type("sync_team_inventory");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncTeamInventory> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> packet.encode(buffer), PacketSyncTeamInventory::new);

    private final UUID teamId;
    private final NonNullList<ItemStack> items;

    public PacketSyncTeamInventory(UUID teamId, NonNullList<ItemStack> items) {
        this.teamId = teamId;
        this.items = items;
    }

    public PacketSyncTeamInventory(RegistryFriendlyByteBuf buffer) {
        this.teamId = buffer.readUUID();
        int size = buffer.readInt();
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            this.items.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
        }
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(teamId);
        buffer.writeInt(items.size());
        for (ItemStack stack : items) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncTeamInventory packet, IPayloadContext context) {
        context.enqueueWork(() -> TeamInventory.setClientInventory(packet.teamId, packet.items));
    }
}
