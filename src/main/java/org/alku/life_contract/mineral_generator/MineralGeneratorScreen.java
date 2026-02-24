package org.alku.life_contract.mineral_generator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.NetworkHandler;

public class MineralGeneratorScreen extends AbstractContainerScreen<MineralGeneratorMenu> {

    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/generic_54.png");

    private Button ironButton, goldButton, diamondButton, emeraldButton;
    private Button enableButton, disableButton;
    private EditBox intervalInput;

    private String currentMineralType = "IRON";
    private int currentInterval = 60;
    private boolean currentEnabled = false;
    private boolean dataLoaded = false;

    public MineralGeneratorScreen(MineralGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;

        ClientDataStorage.MineralGeneratorData cachedData = ClientDataStorage.getMineralGeneratorData(menu.getBlockPos());
        if (cachedData != null) {
            this.currentMineralType = cachedData.mineralType;
            this.currentInterval = cachedData.interval;
            this.currentEnabled = cachedData.enabled;
            this.dataLoaded = true;
        }

        this.ironButton = this.addRenderableWidget(Button.builder(Component.literal("铁锭"), button -> {
            selectMineral("IRON");
        }).bounds(guiLeft + 8, guiTop + 20, 40, 20).build());

        this.goldButton = this.addRenderableWidget(Button.builder(Component.literal("金锭"), button -> {
            selectMineral("GOLD");
        }).bounds(guiLeft + 52, guiTop + 20, 40, 20).build());

        this.diamondButton = this.addRenderableWidget(Button.builder(Component.literal("钻石"), button -> {
            selectMineral("DIAMOND");
        }).bounds(guiLeft + 96, guiTop + 20, 40, 20).build());

        this.emeraldButton = this.addRenderableWidget(Button.builder(Component.literal("绿宝石"), button -> {
            selectMineral("EMERALD");
        }).bounds(guiLeft + 140, guiTop + 20, 50, 20).build());

        this.intervalInput = new EditBox(this.font, guiLeft + 40, guiTop + 45, 60, 20, Component.literal("间隔(秒)"));
        this.intervalInput.setValue(String.valueOf(currentInterval));
        this.intervalInput.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.intervalInput);
        this.setFocused(this.intervalInput);
        this.intervalInput.setFocused(true);

        this.addRenderableWidget(Button.builder(Component.literal("设置间隔"), button -> {
            try {
                int interval = Integer.parseInt(intervalInput.getValue());
                if (interval > 0) {
                    currentInterval = interval;
                    saveConfig();
                }
            } catch (NumberFormatException e) {
            }
        }).bounds(guiLeft + 105, guiTop + 45, 70, 20).build());

        this.enableButton = this.addRenderableWidget(Button.builder(Component.literal("§a开启"), button -> {
            currentEnabled = true;
            saveConfig();
            updateButtonStates();
        }).bounds(guiLeft + 30, guiTop + 70, 60, 20).build());

        this.disableButton = this.addRenderableWidget(Button.builder(Component.literal("§c关闭"), button -> {
            currentEnabled = false;
            saveConfig();
            updateButtonStates();
        }).bounds(guiLeft + 110, guiTop + 70, 60, 20).build());

        if (!dataLoaded) {
            NetworkHandler.sendGetMineralGeneratorPacket(menu.getBlockPos());
        }

        updateButtonStates();
    }

    private void selectMineral(String type) {
        currentMineralType = type;
        saveConfig();
        updateButtonStates();
    }

    private void saveConfig() {
        NetworkHandler.sendSetMineralGeneratorPacket(
                menu.getBlockPos(),
                currentMineralType,
                currentInterval,
                currentEnabled
        );
    }

    public void updateDataFromServer(String mineralType, int interval, boolean enabled) {
        this.currentMineralType = mineralType;
        this.currentInterval = interval;
        this.currentEnabled = enabled;
        this.dataLoaded = true;
        this.intervalInput.setValue(String.valueOf(interval));
        updateButtonStates();
    }

    private void updateButtonStates() {
        this.ironButton.active = !currentMineralType.equals("IRON");
        this.goldButton.active = !currentMineralType.equals("GOLD");
        this.diamondButton.active = !currentMineralType.equals("DIAMOND");
        this.emeraldButton.active = !currentMineralType.equals("EMERALD");

        this.enableButton.active = !currentEnabled;
        this.disableButton.active = currentEnabled;
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
        guiGraphics.blit(CONTAINER_LOCATION, i, j + 72, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, "选择矿物:", 8, 8, 0x404040, false);
        guiGraphics.drawString(this.font, "时间间隔(秒):", 8, 50, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.intervalInput.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.intervalInput.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.intervalInput.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
