package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FollowerWandMenu extends AbstractContainerMenu {

    private final SimpleContainer container;
    public static final int CONTAINER_SIZE = 9;
    private final InteractionHand hand;
    private final Player player;

    public FollowerWandMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, InteractionHand.MAIN_HAND);
    }

    public FollowerWandMenu(int windowId, Inventory playerInventory, InteractionHand hand) {
        super(Life_contract.FOLLOWER_WAND_MENU.get(), windowId);
        this.hand = hand;
        this.player = playerInventory.player;
        
        ItemStack wand = player.getItemInHand(hand);
        this.container = FollowerWandItem.getContainer(wand);
        container.startOpen(playerInventory.player);

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new OutputOnlySlot(container, col, 8 + col * 18, 18));
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 50 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 108));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();

            if (index < CONTAINER_SIZE) {
                if (!this.moveItemStackTo(slotStack, CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            ItemStack wand = player.getItemInHand(hand);
            if (wand.getItem() instanceof FollowerWandItem) {
                FollowerWandItem.saveContainer(wand, container);
            }
        }
        container.stopOpen(player);
    }

    private static class OutputOnlySlot extends Slot {

        public OutputOnlySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
