package org.alku.life_contract.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.border.BorderStatusPayload;
import org.joml.Matrix4f;

/**
 * 预计缩圈提示 HUD：位于圈外时绘制一个始终指向“要走的屏幕方向”的箭头，
 * 箭头随玩家视角旋转，文字方向仅作为附加说明。
 */
@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BorderStatusHUD {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "border_status");

    private static final int PANEL_BG = 0xB3101418;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ARROW_OUTLINE = 0xFF05070A;
    private static final int ARROW_FAR = 0xFFFFC93C;
    private static final int ARROW_NEAR = 0xFFFF3B30;

    private static final int PANEL_HEIGHT = 20;
    private static final float ARROW_SIZE = 6.6F;
    private static final float BADGE_SLOT = 24.0F;
    /** 距离大于该值时箭头为“远方”配色。 */
    private static final float FAR_DISTANCE = 160.0F;
    /** 距离小于该值时箭头为最紧急配色。 */
    private static final float NEAR_DISTANCE = 24.0F;

    private static boolean active;
    private static boolean inside;
    private static String direction = "";
    private static int distance;
    private static float dirX;
    private static float dirZ;

    private BorderStatusHUD() {
    }

    @SubscribeEvent
    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, BorderStatusHUD::render);
    }

    public static void update(BorderStatusPayload payload) {
        active = payload.active();
        inside = payload.inside();
        direction = payload.direction();
        distance = payload.distance();
        dirX = payload.dirX();
        dirZ = payload.dirZ();
    }

    public static boolean isBorderStatusActive() {
        return active;
    }

    private static void render(GuiGraphics graphics, DeltaTracker partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active || minecraft.player == null || minecraft.options.hideGui) return;

        int centerX = graphics.guiWidth() / 2;
        int panelTop = graphics.guiHeight() - 68;

        if (inside) {
            Component message = Component.literal("§a✓ 你在预计缩圈范围内");
            int width = minecraft.font.width(message);
            graphics.fill(centerX - width / 2 - 6, panelTop, centerX + width / 2 + 6,
                    panelTop + PANEL_HEIGHT, PANEL_BG);
            graphics.drawCenteredString(minecraft.font, message, centerX, panelTop + 6, TEXT_COLOR);
            return;
        }

        float partial = partialTick.getGameTimeDeltaPartialTick(false);
        float urgency = Mth.clamp((FAR_DISTANCE - distance) / (FAR_DISTANCE - NEAR_DISTANCE), 0.0F, 1.0F);
        int accent = lerpColor(ARROW_FAR, ARROW_NEAR, urgency);
        // 越接近边界闪烁越明显，形成“快点进圈”的紧迫感
        float pulse = 0.5F + 0.5F * Mth.sin((System.currentTimeMillis() % 1200L) / 1200.0F * (float) (Math.PI * 2.0));
        float arrowScale = 1.0F + 0.1F * urgency * pulse;

        Component message = Component.literal(
                "§c✗ 圈外 §7· §f距边界 §e" + distance + " §f格 §7(向" + direction + ")");
        int panelWidth = (int) BADGE_SLOT + minecraft.font.width(message) + 12;
        int panelLeft = centerX - panelWidth / 2;

        graphics.fill(panelLeft - 1, panelTop - 1, panelLeft + panelWidth + 1, panelTop + PANEL_HEIGHT + 1,
                withAlpha(accent, 0x59));
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + PANEL_HEIGHT, PANEL_BG);
        graphics.fill(panelLeft, panelTop, panelLeft + 2, panelTop + PANEL_HEIGHT, withAlpha(accent, 0xE6));

        graphics.drawString(minecraft.font, message, panelLeft + (int) BADGE_SLOT,
                panelTop + (PANEL_HEIGHT - 8) / 2, TEXT_COLOR, true);
        // 箭头使用即时绘制，先刷新批次，确保它绘制在面板与文字之上
        graphics.flush();
        drawArrow(graphics, panelLeft + BADGE_SLOT / 2.0F + 1.0F, panelTop + PANEL_HEIGHT / 2.0F,
                ARROW_SIZE * arrowScale, arrowAngle(minecraft, partial), accent);
    }

    /** 计算箭头在屏幕上的旋转角度：0 表示正前方，正值表示目标在玩家右侧。 */
    private static float arrowAngle(Minecraft minecraft, float partial) {
        float forward = 0.0F;
        float right = 0.0F;
        if (dirX != 0.0F || dirZ != 0.0F) {
            double yaw = Math.toRadians(minecraft.player.getViewYRot(partial));
            float sin = (float) Math.sin(yaw);
            float cos = (float) Math.cos(yaw);
            // 面朝方向 (-sin, cos)，右手方向 (-cos, -sin)（世界 XZ 坐标）
            forward = dirX * -sin + dirZ * cos;
            right = dirX * -cos + dirZ * -sin;
        }
        return (float) Math.atan2(right, forward);
    }

    private static void drawArrow(GuiGraphics graphics, float centerX, float centerY, float size, float angle,
                                  int color) {
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        addArrow(buffer, matrix, centerX, centerY, size * 1.55F, angle, ARROW_OUTLINE);
        addArrow(buffer, matrix, centerX, centerY, size, angle, color);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static void addArrow(BufferBuilder buffer, Matrix4f matrix, float centerX, float centerY, float size,
                                 float angle, int color) {
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);
        // 箭头头部
        addTriangle(buffer, matrix, centerX, centerY, size, sin, cos, color,
                0.0F, -1.05F, -0.82F, 0.34F, 0.82F, 0.34F);
        // 箭杆（两个三角形拼成的矩形）
        addTriangle(buffer, matrix, centerX, centerY, size, sin, cos, color,
                -0.32F, 0.24F, 0.32F, 0.24F, 0.32F, 1.05F);
        addTriangle(buffer, matrix, centerX, centerY, size, sin, cos, color,
                -0.32F, 0.24F, 0.32F, 1.05F, -0.32F, 1.05F);
    }

    private static void addTriangle(BufferBuilder buffer, Matrix4f matrix, float centerX, float centerY, float size,
                                    float sin, float cos, int color,
                                    float x1, float y1, float x2, float y2, float x3, float y3) {
        // 正反两种绕序各提交一次，避免受剔除状态影响
        addVertex(buffer, matrix, centerX, centerY, size, sin, cos, color, x1, y1);
        addVertex(buffer, matrix, centerX, centerY, size, sin, cos, color, x2, y2);
        addVertex(buffer, matrix, centerX, centerY, size, sin, cos, color, x3, y3);
        addVertex(buffer, matrix, centerX, centerY, size, sin, cos, color, x3, y3);
        addVertex(buffer, matrix, centerX, centerY, size, sin, cos, color, x2, y2);
        addVertex(buffer, matrix, centerX, centerY, size, sin, cos, color, x1, y1);
    }

    private static void addVertex(BufferBuilder buffer, Matrix4f matrix, float centerX, float centerY, float size,
                                  float sin, float cos, int color, float localX, float localY) {
        float x = centerX + size * (localX * cos - localY * sin);
        float y = centerY + size * (localX * sin + localY * cos);
        buffer.addVertex(matrix, x, y, 0.0F).setColor(color);
    }

    private static int lerpColor(int from, int to, float delta) {
        int red = (int) Mth.lerp(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int green = (int) Mth.lerp(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int blue = (int) Mth.lerp(delta, from & 0xFF, to & 0xFF);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha & 0xFF) << 24 | (color & 0xFFFFFF);
    }
}
