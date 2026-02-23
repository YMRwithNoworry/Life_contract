package org.alku.life_contract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TradeSetupScreen extends AbstractContainerScreen<TradeSetupMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");
    private Button confirmButton;
    private EditBox expLevelInput;
    private int expLevel = 1;

    public TradeSetupScreen(TradeSetupMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;

        this.expLevelInput = new EditBox(this.font, guiLeft + 60, guiTop + 40, 60, 20, Component.literal("经验等级"));
        this.expLevelInput.setValue("1");
        this.expLevelInput.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.expLevelInput);
        this.setFocused(this.expLevelInput);
        this.expLevelInput.setFocused(true);

        this.confirmButton = this.addRenderableWidget(Button.builder(Component.literal("确认添加"), button -> {
            try {
                expLevel = Integer.parseInt(expLevelInput.getValue());
                if (expLevel > 0) {
                    NetworkHandler.sendSaveTradePacket(expLevel);
                }
            } catch (NumberFormatException e) {
            }
        }).bounds(guiLeft + 38, guiTop + 70, 100, 20).build());
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
        guiGraphics.blit(CONTAINER_LOCATION, i, j + 125, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(this.font, "经验等级:", 8, 43, 0x404040, false);
        guiGraphics.drawString(this.font, "获得物品", 56, 103, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.expLevelInput.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.expLevelInput.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.expLevelInput.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
