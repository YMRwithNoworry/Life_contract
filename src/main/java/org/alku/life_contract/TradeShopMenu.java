package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TradeShopMenu extends AbstractContainerMenu {

    private final Container container;
    private static final int CONTAINER_SIZE = 54;

    public TradeShopMenu(int windowId, Inventory playerInventory) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public TradeShopMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE));
    }

    public TradeShopMenu(int windowId, Inventory playerInventory, Container container) {
        super(Life_contract.TRADE_SHOP_MENU.get(), windowId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        container.startOpen(playerInventory.player);

        fillTradeItems();

        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new TradeSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
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

    private void fillTradeItems() {
        for (int i = 0; i < TradeConfig.getTradeCount() && i < CONTAINER_SIZE; i++) {
            TradeConfig.TradeItem trade = TradeConfig.getTrade(i);
            if (trade != null) {
                container.setItem(i, trade.getOutput().copy());
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

    private static class TradeSlot extends Slot {
        public TradeSlot(Container container, int slot, int x, int y) {
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
