package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TradeSetupMenu extends AbstractContainerMenu {

    private final Container container;
    private static final int CONTAINER_SIZE = 1;

    public TradeSetupMenu(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public TradeSetupMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public TradeSetupMenu(int windowId, Inventory playerInventory, Container container) {
        super(Life_contract.TRADE_SETUP_MENU.get(), windowId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        container.startOpen(playerInventory.player);

        this.addSlot(new OutputSlot(container, 0, 80, 120));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 152 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 210));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < CONTAINER_SIZE) {
                if (!this.moveItemStackTo(itemstack1, CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, CONTAINER_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public Container getContainer() {
        return container;
    }

    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }
    }
}
