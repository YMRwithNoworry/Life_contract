package org.alku.life_contract.byte_chen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

public class ByteChenHUD implements IGuiOverlay {
    private static final ResourceLocation COMPUTE_BAR_TEXTURE = new ResourceLocation(Life_contract.MODID, "textures/gui/compute_bar.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        Profession profession = ClientProfessionCache.getCurrentProfession();
        if (profession == null || !profession.isByteChen()) return;

        int compute = ClientByteChenState.getCompute();
        int maxCompute = profession.getByteChenComputeMax();
        int nodeCount = ClientByteChenState.getNodeCount();
        int maxNodes = profession.getByteChenNodeMax();
        boolean exhausted = ClientByteChenState.isExhausted();
        boolean ultimateActive = ClientByteChenState.isUltimateActive();

        int x = screenWidth / 2 - 91;
        int y = screenHeight - 32 - 8;

        renderComputeBar(graphics, x, y, compute, maxCompute, exhausted);

        renderNodeCount(graphics, x, y + 12, nodeCount, maxNodes);

        renderCooldowns(graphics, x, y + 24, profession);

        if (exhausted) {
            renderExhaustWarning(graphics, screenWidth, screenHeight);
        }

        if (ultimateActive) {
            renderUltimateIndicator(graphics, screenWidth, screenHeight);
        }
    }

    private void renderComputeBar(GuiGraphics graphics, int x, int y, int compute, int maxCompute, boolean exhausted) {
        int barWidth = 182;
        int barHeight = 5;

        int color = exhausted ? 0xFF555555 : (compute < 20 ? 0xFFFF5555 : 0xFF00BFFF);
        
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF333333);
        
        int fillWidth = (int)((float)compute / maxCompute * barWidth);
        graphics.fill(x, y, x + fillWidth, y + barHeight, color);

        String text = String.format("算力: %d/%d", compute, maxCompute);
        graphics.drawString(Minecraft.getInstance().font, text, x, y - 10, 0xFFFFFFFF);
    }

    private void renderNodeCount(GuiGraphics graphics, int x, int y, int nodeCount, int maxNodes) {
        String text = String.format("节点: %d/%d", nodeCount, maxNodes);
        int color = nodeCount >= maxNodes ? 0xFFFF5555 : 0xFF00FF00;
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color);
    }

    private void renderCooldowns(GuiGraphics graphics, int x, int y, Profession profession) {
        Minecraft mc = Minecraft.getInstance();
        int spacing = 50;
        int currentX = x;

        int fullReadCd = ClientByteChenState.getFullReadCooldown();
        int dispatchCd = ClientByteChenState.getDataDispatchCooldown();
        int banCd = ClientByteChenState.getDataBanCooldown();
        int ultimateCd = ClientByteChenState.getUltimateCooldown();

        renderCooldownText(graphics, mc, currentX, y, "Q1", fullReadCd, profession.getByteChenFullReadCooldown());
        currentX += spacing;

        renderCooldownText(graphics, mc, currentX, y, "Q2", dispatchCd, profession.getByteChenDataDispatchCooldown());
        currentX += spacing;

        renderCooldownText(graphics, mc, currentX, y, "Q3", banCd, profession.getByteChenDataBanCooldown());
        currentX += spacing;

        renderCooldownText(graphics, mc, currentX, y, "R", ultimateCd, profession.getByteChenUltimateCooldown());
    }

    private void renderCooldownText(GuiGraphics graphics, Minecraft mc, int x, int y, String skillName, int currentCd, int maxCd) {
        int color = currentCd > 0 ? 0xFFFF5555 : 0xFF00FF00;
        String text;
        if (currentCd > 0) {
            float seconds = currentCd / 20.0f;
            text = String.format("%s: %.1fs", skillName, seconds);
        } else {
            text = skillName + ": 就绪";
        }
        graphics.drawString(mc.font, text, x, y, color);
    }

    private void renderExhaustWarning(GuiGraphics graphics, int screenWidth, int screenHeight) {
        String text = "§c[算力枯竭] 技能不可用";
        int textWidth = Minecraft.getInstance().font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 - 50;
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFF0000);
    }

    private void renderUltimateIndicator(GuiGraphics graphics, int screenWidth, int screenHeight) {
        int timer = ClientByteChenState.getUltimateTimer();
        float seconds = timer / 20.0f;
        String text = String.format("§d[全域字节重构] %.1fs", seconds);
        int textWidth = Minecraft.getInstance().font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 - 60;
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFF00FF);
    }
}
