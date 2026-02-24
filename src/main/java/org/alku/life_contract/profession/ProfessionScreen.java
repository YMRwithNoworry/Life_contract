package org.alku.life_contract.profession;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.alku.life_contract.NetworkHandler;

import java.util.List;

public class ProfessionScreen extends Screen {

    private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");
    
    private static final int IMAGE_WIDTH = 248;
    private static final int IMAGE_HEIGHT = 195;
    
    private Profession selectedProfession;
    private int selectedSlot = -1;
    private EditBox passwordBox;
    private Button confirmButton;
    private Button unlockButton;
    private Button cancelButton;
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;

    public ProfessionScreen() {
        super(Component.literal("职业选择"));
    }

    @Override
    protected void init() {
        super.init();
        
        int guiLeft = (this.width - IMAGE_WIDTH) / 2;
        int guiTop = (this.height - IMAGE_HEIGHT) / 2;
        
        int buttonY = guiTop + 150;
        
        confirmButton = Button.builder(Component.literal("确认选择"), button -> {
            if (selectedProfession != null && !selectedProfession.requiresPassword()) {
                NetworkHandler.sendSelectProfessionPacket(selectedProfession.getId());
                this.onClose();
            } else if (selectedProfession != null && selectedProfession.requiresPassword()) {
                if (ClientUnlockedProfessions.isUnlocked(selectedProfession.getId())) {
                    NetworkHandler.sendSelectProfessionPacket(selectedProfession.getId());
                    this.onClose();
                } else {
                    statusMessage = "该职业需要先解锁！";
                    statusColor = 0xFF5555;
                }
            }
        }).bounds(guiLeft + 8, buttonY, 70, 20).build();
        confirmButton.active = false;
        this.addRenderableWidget(confirmButton);
        
        passwordBox = new EditBox(this.font, guiLeft + 82, buttonY + 1, 86, 18, Component.literal("密码"));
        passwordBox.setMaxLength(32);
        this.addRenderableWidget(passwordBox);
        
        unlockButton = Button.builder(Component.literal("解锁职业"), button -> {
            if (selectedProfession != null && selectedProfession.requiresPassword()) {
                String password = passwordBox.getValue();
                NetworkHandler.sendUnlockProfessionPacket(selectedProfession.getId(), password);
            }
        }).bounds(guiLeft + 172, buttonY, 70, 20).build();
        unlockButton.active = false;
        this.addRenderableWidget(unlockButton);
        
        cancelButton = Button.builder(Component.literal("关闭"), button -> {
            this.onClose();
        }).bounds(guiLeft + 88, buttonY + 24, 70, 20).build();
        this.addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        
        int guiLeft = (this.width - IMAGE_WIDTH) / 2;
        int guiTop = (this.height - IMAGE_HEIGHT) / 2;
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND_LOCATION, guiLeft, guiTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        guiGraphics.drawString(this.font, "职业选择", guiLeft + IMAGE_WIDTH / 2 - this.font.width("职业选择") / 2, guiTop + 6, 0x404040, false);
        
        renderProfessionItems(guiGraphics, guiLeft, guiTop);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int infoY = guiTop + 100;
        
        if (selectedProfession != null) {
            boolean unlocked = ClientUnlockedProfessions.isUnlocked(selectedProfession.getId());
            
            String statusText;
            int statusCol;
            if (selectedProfession.requiresPassword() && !unlocked) {
                statusText = " [需密码解锁]";
                statusCol = 0xFF5555;
            } else if (selectedProfession.requiresPassword()) {
                statusText = " [已解锁]";
                statusCol = 0x55FF55;
            } else {
                statusText = "";
                statusCol = 0xFFFF55;
            }
            
            String displayText = "选中: " + selectedProfession.getName() + statusText;
            guiGraphics.drawString(this.font, displayText, 
                    guiLeft + 8, infoY, statusCol, false);
            
            String desc = selectedProfession.getDescription();
            if (!desc.isEmpty() && desc.length() > 30) {
                desc = desc.substring(0, 30) + "...";
            }
            if (!desc.isEmpty()) {
                guiGraphics.drawString(this.font, desc, 
                        guiLeft + 8, infoY + 10, 0xAAAAAA, false);
            }
        } else {
            guiGraphics.drawString(this.font, "请点击选择一个职业", 
                    guiLeft + 8, infoY, 0xAAAAAA, false);
        }
        
        if (!statusMessage.isEmpty()) {
            guiGraphics.drawString(this.font, statusMessage, 
                    guiLeft + 8, infoY + 22, statusColor, false);
        }
        
        renderTooltip(guiGraphics, mouseX, mouseY, guiLeft, guiTop);
    }

    private void renderProfessionItems(GuiGraphics guiGraphics, int guiLeft, int guiTop) {
        List<Profession> professions = ClientProfessionCache.getProfessions();
        
        int startX = guiLeft + 10;
        int startY = guiTop + 24;
        int cols = 9;
        int itemSize = 18;
        
        for (int i = 0; i < professions.size() && i < 36; i++) {
            Profession profession = professions.get(i);
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * itemSize;
            int y = startY + row * itemSize;
            
            boolean unlocked = ClientUnlockedProfessions.isUnlocked(profession.getId());
            
            boolean isLocked = ProfessionConfig.isProfessionLocked(profession.getId());
            
            if (isLocked) {
                net.minecraft.world.item.ItemStack barrierStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BARRIER);
                guiGraphics.renderFakeItem(barrierStack, x, y);
            } else if (!profession.requiresPassword() || unlocked) {
                String itemId = profession.getIconItem();
                net.minecraft.resources.ResourceLocation resourceLocation = new net.minecraft.resources.ResourceLocation(itemId);
                net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resourceLocation);
                
                if (item == null) {
                    item = net.minecraft.world.item.Items.PAPER;
                }
                
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
                guiGraphics.renderFakeItem(stack, x, y);
            } else {
                net.minecraft.world.item.ItemStack barrierStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BARRIER);
                guiGraphics.renderFakeItem(barrierStack, x, y);
            }
        }
    }

    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int guiLeft, int guiTop) {
        int slotIndex = getSlotUnderMouse(mouseX, mouseY, guiLeft, guiTop);
        if (slotIndex >= 0 && slotIndex < 36) {
            List<Profession> professions = ClientProfessionCache.getProfessions();
            if (slotIndex < professions.size()) {
                Profession profession = professions.get(slotIndex);
                boolean unlocked = ClientUnlockedProfessions.isUnlocked(profession.getId());
                
                boolean isLocked = ProfessionConfig.isProfessionLocked(profession.getId());
                
                java.util.List<Component> tooltip = new java.util.ArrayList<>();
                
                if (isLocked) {
                    tooltip.add(Component.literal("§c" + profession.getName() + " [已锁定]"));
                    tooltip.add(Component.literal("§7该职业已被管理员锁定"));
                    tooltip.add(Component.literal("§7暂时无法选择此职业"));
                } else if (!profession.requiresPassword() || unlocked) {
                    tooltip.add(Component.literal("§e" + profession.getName()));
                    if (!profession.getDescription().isEmpty()) {
                        tooltip.add(Component.literal("§7" + profession.getDescription()));
                    }
                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal("§a点击选择此职业"));
                } else {
                    tooltip.add(Component.literal("§c??? (未解锁)"));
                    tooltip.add(Component.literal("§7输入密码以解锁此职业"));
                }
                
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiLeft = (this.width - IMAGE_WIDTH) / 2;
        int guiTop = (this.height - IMAGE_HEIGHT) / 2;
        
        int slotIndex = getSlotUnderMouse(mouseX, mouseY, guiLeft, guiTop);
        
        if (slotIndex >= 0 && slotIndex < 36) {
            List<Profession> professions = ClientProfessionCache.getProfessions();
            if (slotIndex < professions.size()) {
                Profession profession = professions.get(slotIndex);
                selectedProfession = profession;
                selectedSlot = slotIndex;
                
                boolean unlocked = ClientUnlockedProfessions.isUnlocked(profession.getId());
                
                boolean isLocked = ProfessionConfig.isProfessionLocked(profession.getId());
                
                if (isLocked) {
                    confirmButton.active = false;
                    unlockButton.active = false;
                    statusMessage = "该职业已被管理员锁定！";
                    statusColor = 0xFF5555;
                } else if (!profession.requiresPassword() || unlocked) {
                    confirmButton.active = true;
                    unlockButton.active = false;
                    statusMessage = "";
                } else {
                    confirmButton.active = false;
                    unlockButton.active = true;
                    statusMessage = "";
                }
                
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getSlotUnderMouse(double mouseX, double mouseY, int guiLeft, int guiTop) {
        int startX = guiLeft + 10;
        int startY = guiTop + 24;
        int cols = 9;
        int itemSize = 18;
        
        int x = (int) (mouseX - startX);
        int y = (int) (mouseY - startY);

        if (x >= 0 && x < cols * itemSize && y >= 0 && y < 4 * itemSize) {
            int col = x / itemSize;
            int row = y / itemSize;
            return row * cols + col;
        }
        return -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setStatusMessage(String message, int color) {
        this.statusMessage = message;
        this.statusColor = color;
    }
}
