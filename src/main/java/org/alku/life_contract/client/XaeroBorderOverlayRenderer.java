package org.alku.life_contract.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import org.joml.Matrix4f;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.render.module.ModuleRenderContext;

public final class XaeroBorderOverlayRenderer {
    private static final int COLOR = 0xFFFF3B30;

    private XaeroBorderOverlayRenderer() {}

    public static void render(MinimapSession session, ModuleRenderContext context, GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || context.w <= 0 || context.h <= 0) return;

        WorldBorder border = minecraft.level.getWorldBorder();
        double zoom = session.getProcessor().getMinimapZoom();
        double playerX = Mth.lerp(partialTick, minecraft.player.xOld, minecraft.player.getX());
        double playerZ = Mth.lerp(partialTick, minecraft.player.zOld, minecraft.player.getZ());
        boolean northLocked = MinimapConfigClientUtils.getEffectiveNorthLocked(context.w, session.getConfiguredWidth());
        double angle = northLocked ? Math.PI / 2.0 : Math.toRadians(minecraft.player.getViewYRot(partialTick) + 90.0F);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double centerX = context.x + context.w / 2.0;
        double centerY = context.y + context.h / 2.0;

        Point[] corners = {
            project(border.getMinX(), border.getMinZ(), playerX, playerZ, zoom, sin, cos, centerX, centerY),
            project(border.getMaxX(), border.getMinZ(), playerX, playerZ, zoom, sin, cos, centerX, centerY),
            project(border.getMaxX(), border.getMaxZ(), playerX, playerZ, zoom, sin, cos, centerX, centerY),
            project(border.getMinX(), border.getMaxZ(), playerX, playerZ, zoom, sin, cos, centerX, centerY)
        };

        graphics.enableScissor(context.x, context.y, context.x + context.w, context.y + context.h);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 4; i++) addClippedLine(buffer, matrix, corners[i], corners[(i + 1) % 4], context);
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
        graphics.disableScissor();
    }

    private static Point project(double x, double z, double playerX, double playerZ, double zoom,
                                 double sin, double cos, double centerX, double centerY) {
        double dx = (x - playerX) * zoom;
        double dz = (z - playerZ) * zoom;
        return new Point(centerX + sin * dx - cos * dz, centerY + cos * dx + sin * dz);
    }

    private static void addClippedLine(BufferBuilder buffer, Matrix4f matrix, Point a, Point b, ModuleRenderContext context) {
        Point[] clipped = clip(a, b, context.x, context.y, context.x + context.w, context.y + context.h);
        if (clipped == null) return;
        double dx = clipped[1].x - clipped[0].x;
        double dy = clipped[1].y - clipped[0].y;
        double length = Math.hypot(dx, dy);
        if (length < 0.01) return;
        double nx = -dy / length;
        double ny = dx / length;
        vertex(buffer, matrix, clipped[0].x + nx, clipped[0].y + ny);
        vertex(buffer, matrix, clipped[0].x - nx, clipped[0].y - ny);
        vertex(buffer, matrix, clipped[1].x - nx, clipped[1].y - ny);
        vertex(buffer, matrix, clipped[1].x + nx, clipped[1].y + ny);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y) {
        buffer.vertex(matrix, (float) x, (float) y, 200.0F).color(COLOR).endVertex();
    }

    private static Point[] clip(Point a, Point b, double left, double top, double right, double bottom) {
        double dx = b.x - a.x, dy = b.y - a.y;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {a.x - left, right - a.x, a.y - top, bottom - a.y};
        double t0 = 0.0, t1 = 1.0;
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0.0) { if (q[i] < 0.0) return null; continue; }
            double r = q[i] / p[i];
            if (p[i] < 0.0) t0 = Math.max(t0, r); else t1 = Math.min(t1, r);
            if (t0 > t1) return null;
        }
        return new Point[]{new Point(a.x + t0 * dx, a.y + t0 * dy), new Point(a.x + t1 * dx, a.y + t1 * dy)};
    }

    private record Point(double x, double y) {}
}
