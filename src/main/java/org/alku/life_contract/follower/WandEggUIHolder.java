package org.alku.life_contract.follower;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class WandEggUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "follower_wand");

    private final Player player;
    private final WandEggStorage storage;

    public WandEggUIHolder(Player player) {
        this.player = player;
        this.storage = WandEggStorage.getOrCreate(player);
    }

    @Override
    public ModularUI createUI(Player player) {
        UIElement root = new UIElement();
        root.getLayout().width(178).paddingAll(6).gapAll(5);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.follower_wand.title"));
        UIElement grid = new UIElement();
        grid.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(2);
        for (int rowIndex = 0; rowIndex < 4; rowIndex++) {
            UIElement row = new UIElement();
            row.getLayout().flexDirection(FlexDirection.ROW).gapAll(2);
            for (int column = 0; column < 5; column++) {
                ItemSlot slot = new ItemSlot();
                slot.bind(storage, rowIndex * 5 + column);
                slot.getSlotStyle().acceptQuickMove(true).quickMovePriority(1);
                row.addChild(slot);
            }
            grid.addChild(row);
        }

        root.addChildren(title, grid,
                new Label().setValue(Component.translatable("gui.life_contract.follower_wand.player_inventory")),
                new InventorySlots().apply(slot -> slot.getSlotStyle().acceptQuickMove(true)));
        return new ModularUI(UI.of(root, List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC))), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == this.player && player.isAlive();
    }
}
