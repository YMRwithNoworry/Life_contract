package org.alku.life_contract;

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu;
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-only LDLib2 layout for the existing server-authoritative team inventory menu.
 */
@OnlyIn(Dist.CLIENT)
public final class TeamInventoryUi {

    public static final int WIDTH = 208;
    public static final int HEIGHT = 238;

    private static final int TEAM_SLOT_COUNT = 54;
    private static final int GRID_LEFT = 23;
    private static final int TEAM_GRID_TOP = 25;
    private static final int PLAYER_GRID_TOP = 154;
    private static final int HOTBAR_TOP = 212;

    private static final IGuiTexture ROOT_BACKGROUND = GuiTextureGroup.of(
            new ColorRectTexture(0xF018252B),
            new ColorBorderTexture(1, 0xFF5C8D91)
    );
    private static final IGuiTexture SECTION_BACKGROUND = GuiTextureGroup.of(
            new ColorRectTexture(0x7010181D),
            new ColorBorderTexture(1, 0xFF365258)
    );
    private static final IGuiTexture ACCENT = new ColorRectTexture(0xFF65C6C2);
    private static final IGuiTexture SLOT_BACKGROUND = GuiTextureGroup.of(
            new ColorRectTexture(0xFF11191D),
            new ColorBorderTexture(1, 0xFF42636B)
    );
    private static final IGuiTexture SLOT_HOVER = new ColorRectTexture(0x5873D8D0);

    private TeamInventoryUi() {
    }

    public static ModularUI create(TeamInventoryMenu menu, Player player) {
        if (!(menu instanceof IModularUIHolderMenu holder)) {
            throw new IllegalStateException("LDLib2 did not apply its menu holder mixin to the team inventory.");
        }

        List<ItemSlot> itemSlots = new ArrayList<>(menu.slots.size());
        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(ROOT_BACKGROUND));

        addPanel(root, 15, 21, 178, 116);
        addPanel(root, 15, 151, 178, 82);
        addDecoration(root, 16, 19, 176, 1, ACCENT);

        root.addChild(label(
                Component.translatable("container.life_contract.team_inventory"),
                16, 6, 176, 12, 0xFFF1FAF9, 10.0F, Horizontal.CENTER
        ));
        root.addChild(label(
                Component.translatable("gui.life_contract.team_inventory.player_inventory"),
                GRID_LEFT, 141, 162, 10, 0xFFB9D2D0, 8.0F, Horizontal.LEFT
        ));

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = column + row * 9;
                addSlot(root, menu, itemSlots, slotIndex, GRID_LEFT + column * 18, TEAM_GRID_TOP + row * 18, false);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = TEAM_SLOT_COUNT + column + row * 9;
                addSlot(root, menu, itemSlots, slotIndex, GRID_LEFT + column * 18, PLAYER_GRID_TOP + row * 18, true);
            }
        }

        for (int column = 0; column < 9; column++) {
            int slotIndex = TEAM_SLOT_COUNT + 27 + column;
            addSlot(root, menu, itemSlots, slotIndex, GRID_LEFT + column * 18, HOTBAR_TOP, true);
        }

        ModularUI modularUI = ModularUI.of(UI.of(root), player);
        holder.setModularUI(modularUI);

        // The slots already exist in TeamInventoryMenu. Registering them here maps each vanilla
        // slot to its LDLib2 element without adding a duplicate container slot.
        itemSlots.forEach(holder::addSlot);
        return modularUI;
    }

    private static void addSlot(UIElement root, TeamInventoryMenu menu, List<ItemSlot> itemSlots,
            int slotIndex, int x, int y, boolean playerSlot) {
        if (slotIndex >= menu.slots.size()) {
            throw new IllegalStateException("Team inventory menu is missing slot " + slotIndex);
        }

        ItemSlot itemSlot = new ItemSlot(menu.slots.get(slotIndex));
        itemSlot.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE).left(x).top(y));
        itemSlot.style(style -> style.backgroundTexture(SLOT_BACKGROUND));
        itemSlot.slotStyle(style -> style
                .hoverOverlay(SLOT_HOVER)
                .isPlayerSlot(playerSlot));
        root.addChild(itemSlot);
        itemSlots.add(itemSlot);
    }

    private static void addPanel(UIElement root, int x, int y, int width, int height) {
        root.addChild(new UIElement()
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE).left(x).top(y).width(width).height(height))
                .style(style -> style.backgroundTexture(SECTION_BACKGROUND).zIndex(-1))
                .setAllowHitTest(false));
    }

    private static void addDecoration(UIElement root, int x, int y, int width, int height, IGuiTexture texture) {
        root.addChild(new UIElement()
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE).left(x).top(y).width(width).height(height))
                .style(style -> style.backgroundTexture(texture).zIndex(-1))
                .setAllowHitTest(false));
    }

    private static Label label(Component text, int x, int y, int width, int height, int color,
            float fontSize, Horizontal alignment) {
        Label label = new Label();
        label.setText(text);
        label.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE).left(x).top(y).width(width).height(height));
        label.textStyle(style -> style.textColor(color).fontSize(fontSize).textAlignHorizontal(alignment));
        label.setAllowHitTest(false);
        return label;
    }
}
