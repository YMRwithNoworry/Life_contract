package org.alku.life_contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TeamInventoryScreen extends AbstractContainerScreen<TeamInventoryMenu> {
    public TeamInventoryScreen(TeamInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 208;
        imageHeight = 238;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF018252B);
        graphics.fill(x, y, x + imageWidth, y + 1, 0xFF5C8D91);
        graphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF5C8D91);
        graphics.fill(x, y, x + 1, y + imageHeight, 0xFF5C8D91);
        graphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF5C8D91);

        graphics.fill(x + 15, y + 21, x + 193, y + 137, 0x7010181D);
        graphics.fill(x + 15, y + 151, x + 193, y + 233, 0x7010181D);
        graphics.fill(x + 16, y + 19, x + 192, y + 20, 0xFF65C6C2);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component title = Component.translatable("container.life_contract.team_inventory");
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0xFFF1FAF9, false);
        graphics.drawString(font,
                Component.translatable("gui.life_contract.team_inventory.player_inventory"),
                23, 141, 0xFFB9D2D0, false);
    }
}
