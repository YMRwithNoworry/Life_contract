package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.TeamInventory;

import java.util.List;

public final class TeamInventoryUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "team_inventory");

    private final Player owner;
    private final TeamInventory inventory;

    public TeamInventoryUIHolder(Player player) {
        owner = player;
        inventory = TeamInventory.getOrCreate(player);
    }

    @Override
    public ModularUI createUI(Player player) {
        UIElement root = new UIElement();
        root.getLayout().width(190).paddingAll(8).gapAll(5);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("container.life_contract.team_inventory"));
        Label smeltingHint = new Label().setValue(Component.translatable("gui.life_contract.team_inventory.smelting_hint"));

        ScrollerView teamSlots = new ScrollerView();
        teamSlots.getLayout().width(174).height(96);
        UIElement grid = new UIElement();
        grid.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(0);
        for (int rowIndex = 0; rowIndex < TeamInventory.ROWS; rowIndex++) {
            UIElement row = new UIElement();
            row.getLayout().flexDirection(FlexDirection.ROW).gapAll(0);
            for (int column = 0; column < TeamInventory.COLUMNS; column++) {
                ItemSlot slot = new ItemSlot();
                slot.bind(inventory, rowIndex * TeamInventory.COLUMNS + column);
                slot.getSlotStyle().acceptQuickMove(true).quickMovePriority(1);
                row.addChild(slot);
            }
            grid.addChild(row);
        }
        teamSlots.viewContainer(container -> container.addChild(grid));

        Label playerInventoryTitle = new Label()
                .setValue(Component.translatable("gui.life_contract.team_inventory.player_inventory"));
        InventorySlots playerSlots = new InventorySlots();
        playerSlots.apply(slot -> slot.getSlotStyle().acceptQuickMove(true));

        root.addChildren(title, smeltingHint, teamSlots, playerInventoryTitle, playerSlots);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                UiLayout.fitToScreen(190, 240)), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive() && inventory.stillValid(player);
    }
}
