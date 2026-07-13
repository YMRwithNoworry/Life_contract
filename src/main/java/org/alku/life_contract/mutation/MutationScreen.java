package org.alku.life_contract.mutation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MutationScreen extends AbstractContainerScreen<MutationMenu> {
    private final Inventory playerInventory;
    private ModularUI modularUI;

    public MutationScreen(MutationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.playerInventory = inventory;
        this.imageWidth = MutationUi.WIDTH;
        this.imageHeight = MutationUi.HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        modularUI = MutationUi.create(menu, playerInventory.player);
        modularUI.setDrawTooltips(false);
        modularUI.setScreenAndInit(this);
        addRenderableWidget(modularUI.getWidget());
        setFocused(modularUI.getWidget());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {}
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}
}
