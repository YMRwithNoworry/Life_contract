package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import org.alku.life_contract.Life_contract;

import java.util.ArrayList;
import java.util.List;

public class ReviveTeammateMenu extends AbstractContainerMenu {

    private final List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates;

    public ReviveTeammateMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, ClientReviveData.getDeadTeammates());
    }

    public ReviveTeammateMenu(int windowId, Inventory playerInventory, List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates) {
        super(Life_contract.REVIVE_TEAMMATE_MENU.get(), windowId);
        this.deadTeammates = new ArrayList<>(deadTeammates);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public List<ReviveTeammateSystem.DeadTeammateInfo> getDeadTeammates() {
        return new ArrayList<>(deadTeammates);
    }
}
