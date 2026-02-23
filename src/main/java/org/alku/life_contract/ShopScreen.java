package org.alku.life_contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");

    public ShopScreen(ShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
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
        guiGraphics.blit(CONTAINER_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        for (int i = 0; i < 54; i++) {
            ShopItem shopItem = ShopConfig.getShopItem(i);
            if (shopItem != null && shopItem.getPrice() > 0) {
                int row = i / 9;
                int col = i % 9;
                int x = 8 + col * 18 + 8;
                int y = 18 + row * 18 + 8;

                String priceText = shopItem.getPrice() + "";
                guiGraphics.drawString(this.font, priceText, x - this.font.width(priceText) + 16, y + 8, 0xFFFF00, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            int slotIndex = getSlotUnderMouse(mouseX, mouseY);
            if (slotIndex >= 0 && slotIndex < 54) {
                NetworkHandler.sendPurchasePacket(slotIndex);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getSlotUnderMouse(double mouseX, double mouseY) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;

        int x = (int) (mouseX - guiLeft);
        int y = (int) (mouseY - guiTop);

        if (x >= 7 && x < 169 && y >= 17 && y < 129) {
            int col = (x - 7) / 18;
            int row = (y - 17) / 18;
            return row * 9 + col;
        }
        return -1;
    }
}