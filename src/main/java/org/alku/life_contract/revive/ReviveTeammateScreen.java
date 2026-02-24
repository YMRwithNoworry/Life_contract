package org.alku.life_contract.revive;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import org.alku.life_contract.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

public class ReviveTeammateScreen extends AbstractContainerScreen<ReviveTeammateMenu> {

    private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");
    
    private final List<Button> teammateButtons = new ArrayList<>();
    private Button skipButton;
    private int scrollOffset = 0;
    private static final int VISIBLE_ROWS = 4;

    public ReviveTeammateScreen(ReviveTeammateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 133;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        
        teammateButtons.clear();
        
        int startX = this.leftPos + 8;
        int startY = this.topPos + 18;
        
        List<ReviveTeammateSystem.DeadTeammateInfo> teammates = this.menu.getDeadTeammates();
        
        for (int i = 0; i < Math.min(VISIBLE_ROWS, teammates.size()); i++) {
            final int index = i + scrollOffset;
            if (index >= teammates.size()) break;
            
            ReviveTeammateSystem.DeadTeammateInfo teammate = teammates.get(index);
            
            Button button = Button.builder(
                Component.literal("§f" + teammate.getName()),
                btn -> selectTeammate(index)
            ).bounds(startX, startY + i * 22, 160, 20).build();
            
            teammateButtons.add(button);
            this.addRenderableWidget(button);
        }
        
        skipButton = Button.builder(
            Component.literal("§7放弃复活"),
            btn -> skipRevive()
        ).bounds(this.leftPos + 8, this.topPos + 110, 160, 20).build();
        
        this.addRenderableWidget(skipButton);
    }

    private void selectTeammate(int index) {
        List<ReviveTeammateSystem.DeadTeammateInfo> teammates = this.menu.getDeadTeammates();
        if (index >= 0 && index < teammates.size()) {
            ReviveTeammateSystem.DeadTeammateInfo teammate = teammates.get(index);
            NetworkHandler.sendReviveTeammatePacket(teammate.getUuid());
            this.onClose();
        }
    }

    private void skipRevive() {
        NetworkHandler.sendSkipRevivePacket();
        this.onClose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, 
            Component.literal("选择复活的队友"), 
            8, 6, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int startX = this.leftPos + 8;
        int startY = this.topPos + 18;
        
        List<ReviveTeammateSystem.DeadTeammateInfo> teammates = this.menu.getDeadTeammates();
        
        for (int i = 0; i < teammateButtons.size(); i++) {
            int index = i + scrollOffset;
            if (index >= teammates.size()) continue;
            
            ReviveTeammateSystem.DeadTeammateInfo teammate = teammates.get(index);
            
            if (mouseX >= startX && mouseX <= startX + 160 &&
                mouseY >= startY + i * 22 && mouseY <= startY + i * 22 + 20) {
                guiGraphics.renderTooltip(this.font, 
                    Component.literal("§e点击复活: §f" + teammate.getName()), 
                    mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_LOCATION, i, j, 0, 0, this.imageWidth, 36);
        guiGraphics.blit(BACKGROUND_LOCATION, i, j + 36, 0, 125, this.imageWidth, 97);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, this.menu.getDeadTeammates().size() - VISIBLE_ROWS);
        int newScroll = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta));
        if (newScroll != scrollOffset) {
            scrollOffset = newScroll;
            this.init();
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
