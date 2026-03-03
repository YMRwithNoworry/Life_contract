package org.alku.life_contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EggShopScreen extends AbstractContainerScreen<EggShopMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public EggShopScreen(EggShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
        updateMaxScroll();
    }

    private void updateMaxScroll() {
        int totalEntries = EggShopConfig.getEntryCount();
        int visibleRows = EggShopMenu.ROWS;
        int totalRows = (totalEntries + EggShopMenu.COLS - 1) / EggShopMenu.COLS;
        maxScroll = Math.max(0, totalRows - visibleRows);
    }

    @Override
    protected void init() {
        super.init();
        updateItems();
    }

    private void updateItems() {
        this.menu.updateItems(scrollOffset);
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

        for (int i = 0; i < EggShopMenu.CONTAINER_SIZE; i++) {
            int entryIndex = i + scrollOffset;
            EggShopConfig.EggShopEntry entry = EggShopConfig.getEntry(entryIndex);
            if (entry != null && entry.getPrice() > 0) {
                int row = i / EggShopMenu.COLS;
                int col = i % EggShopMenu.COLS;
                int x = 8 + col * 18 + 8;
                int y = 18 + row * 18 + 8;

                String priceText = entry.getPrice() + "";
                guiGraphics.drawString(this.font, priceText, x - this.font.width(priceText) + 16, y + 8, 0xFFFF00, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int slotIndex = getSlotUnderMouse(mouseX, mouseY);
            if (slotIndex >= 0 && slotIndex < EggShopMenu.CONTAINER_SIZE) {
                int entryIndex = slotIndex + scrollOffset;
                if (EggShopConfig.getEntry(entryIndex) != null) {
                    NetworkHandler.sendBuyEggPacket(entryIndex);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta != 0) {
            int newOffset = scrollOffset - (int) delta;
            if (newOffset < 0) {
                newOffset = 0;
            } else if (newOffset > maxScroll) {
                newOffset = maxScroll;
            }
            if (newOffset != scrollOffset) {
                scrollOffset = newOffset;
                updateItems();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int getSlotUnderMouse(double mouseX, double mouseY) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;

        int x = (int) (mouseX - guiLeft);
        int y = (int) (mouseY - guiTop);

        if (x >= 7 && x < 169 && y >= 17 && y < 125) {
            int col = (x - 7) / 18;
            int row = (y - 17) / 18;
            return row * EggShopMenu.COLS + col;
        }
        return -1;
    }
}
