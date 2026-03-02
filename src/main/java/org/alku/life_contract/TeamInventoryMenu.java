package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class TeamInventoryMenu extends AbstractContainerMenu {

    private final Container container;
    private final TeamInventory teamInventory;
    private static final int CONTAINER_SIZE = 54;
    private final UUID teamId;

    public TeamInventoryMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, new SimpleContainer(CONTAINER_SIZE), null);
    }

    public TeamInventoryMenu(int windowId, Inventory playerInventory, Container container) {
        this(windowId, playerInventory, container, null);
    }

    public TeamInventoryMenu(int windowId, Inventory playerInventory, Container container, UUID teamId) {
        super(Life_contract.TEAM_INVENTORY_MENU.get(), windowId);
        this.container = container;
        this.teamId = teamId;
        
        if (container instanceof TeamInventory teamInv) {
            this.teamInventory = teamInv;
        } else {
            this.teamInventory = null;
        }
        
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
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

    public TeamInventory getTeamInventory() {
        return teamInventory;
    }

    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public boolean stillValid(Player player) {
        if (teamInventory != null) {
            return teamInventory.stillValid(player);
        }
        return this.container.stillValid(player);
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
            } else if (!this.moveItemStackTo(slotStack, 0, CONTAINER_SIZE, false)) {
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
        this.container.stopOpen(player);
    }

    public static TeamInventoryMenu createForTeam(int windowId, Inventory playerInventory, UUID teamId) {
        TeamInventory teamInv = TeamInventory.getByTeamId(teamId);
        if (teamInv == null) {
            teamInv = TeamInventory.getOrCreate(playerInventory.player);
        }
        return new TeamInventoryMenu(windowId, playerInventory, teamInv, teamId);
    }
}
