package org.alku.life_contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeShopScreen extends AbstractContainerScreen<TradeShopMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");
    private static boolean isRemoveMode = false;

    public TradeShopScreen(TradeShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public static void setRemoveMode(boolean mode) {
        isRemoveMode = mode;
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
        if (isRemoveMode) {
            guiGraphics.drawString(this.font, "§c删除模式 - 右键删除交易", 8, 6, 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int slotIndex = getSlotUnderMouse(mouseX, mouseY);
        if (slotIndex >= 0 && slotIndex < 54) {
            TradeConfig.TradeItem trade = TradeConfig.getTrade(slotIndex);
            if (trade != null) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(trade.getOutput().getHoverName());
                if (isRemoveMode) {
                    tooltip.add(Component.literal("§6价格: " + trade.getExpLevels() + " 经验等级"));
                    tooltip.add(Component.literal("§c右键删除此交易"));
                } else {
                    tooltip.add(Component.literal("§6价格: " + trade.getExpLevels() + " 经验等级"));
                    tooltip.add(Component.literal("§7右键购买"));
                }
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            int slotIndex = getSlotUnderMouse(mouseX, mouseY);
            if (slotIndex >= 0 && slotIndex < 54) {
                if (isRemoveMode) {
                    NetworkHandler.sendRemoveTradePacket(slotIndex);
                } else {
                    NetworkHandler.sendBuyTradePacket(slotIndex);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        super.removed();
        isRemoveMode = false;
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
