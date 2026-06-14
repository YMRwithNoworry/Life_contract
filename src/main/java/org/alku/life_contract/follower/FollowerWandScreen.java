package org.alku.life_contract.follower;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FollowerWandScreen extends AbstractContainerScreen<FollowerWandMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int CONTAINER_ROWS = FollowerWandMenu.CONTAINER_SIZE / 9;

    public FollowerWandScreen(FollowerWandMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 114 + CONTAINER_ROWS * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int inventoryStartY = CONTAINER_ROWS * 18 + 17;
        guiGraphics.blit(CONTAINER_LOCATION, i, j, 0, 0, this.imageWidth, inventoryStartY);
        guiGraphics.blit(CONTAINER_LOCATION, i, j + inventoryStartY, 0, 126, this.imageWidth, 96);
    }
}
