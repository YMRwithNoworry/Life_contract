package org.alku.life_contract;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncTeamInventory {

    private final UUID teamId;
    private final NonNullList<ItemStack> items;

    public PacketSyncTeamInventory(UUID teamId, NonNullList<ItemStack> items) {
        this.teamId = teamId;
        this.items = items;
    }

    public PacketSyncTeamInventory(FriendlyByteBuf buffer) {
        this.teamId = buffer.readUUID();
        int size = buffer.readInt();
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            this.items.set(i, buffer.readItem());
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(teamId);
        buffer.writeInt(items.size());
        for (ItemStack stack : items) {
            buffer.writeItem(stack);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                TeamInventory.setClientInventory(teamId, items);
            });
        });
        context.setPacketHandled(true);
    }
}
