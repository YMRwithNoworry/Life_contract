package org.alku.life_contract;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeamInventoryScreen extends AbstractContainerScreen<TeamInventoryMenu> {

    private final Inventory playerInventory;
    private ModularUI modularUI;

    public TeamInventoryScreen(TeamInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = TeamInventoryUi.WIDTH;
        this.imageHeight = TeamInventoryUi.HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();

        this.modularUI = TeamInventoryUi.create(this.menu, this.playerInventory.player);
        this.modularUI.setDrawTooltips(false);
        this.modularUI.setScreenAndInit(this);
        this.addRenderableWidget(this.modularUI.getWidget());
        this.setFocused(this.modularUI.getWidget());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // LDLib2 renders the complete inventory surface through its ModularUI widget.
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Labels belong to the LDLib2 element tree so they stay aligned with the custom layout.
    }
}
