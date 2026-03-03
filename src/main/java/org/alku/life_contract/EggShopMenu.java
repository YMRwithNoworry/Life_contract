package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EggShopMenu extends AbstractContainerMenu {

    private final Container container;
    public static final int CONTAINER_SIZE = 54;
    public static final int ROWS = 6;
    public static final int COLS = 9;

    public EggShopMenu(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public EggShopMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public EggShopMenu(int windowId, Inventory playerInventory, Container container) {
        super(Life_contract.EGG_SHOP_MENU.get(), windowId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        container.startOpen(playerInventory.player);

        fillEggItems();

        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new EggShopSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    private void fillEggItems() {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            EggShopConfig.EggShopEntry entry = EggShopConfig.getEntry(i);
            if (entry != null) {
                ItemStack eggStack = entry.createEggStack();
                if (!eggStack.isEmpty()) {
                    container.setItem(i, eggStack);
                }
            }
        }
    }

    public void updateItems(int scrollOffset) {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            int entryIndex = i + scrollOffset;
            EggShopConfig.EggShopEntry entry = EggShopConfig.getEntry(entryIndex);
            if (entry != null) {
                ItemStack eggStack = entry.createEggStack();
                container.setItem(i, eggStack);
            } else {
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private static class EggShopSlot extends Slot {
        public EggShopSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
