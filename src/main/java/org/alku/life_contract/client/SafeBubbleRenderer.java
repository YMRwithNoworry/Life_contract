package org.alku.life_contract.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 在世界中绘制净化裂隙的安全气泡球体，让玩家能远远看到安全区。
 */
@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public final class SafeBubbleRenderer {

    private static final float[][] BUBBLE_COLORS = {
        {0.75F, 0.95F, 1.00F},
        {0.45F, 1.00F, 0.55F},
        {0.45F, 0.70F, 1.00F},
        {1.00F, 0.55F, 0.95F},
        {1.00F, 0.90F, 0.45F}
    };

    private static final float SURFACE_ALPHA = 0.22F;
    private static final float INNER_ALPHA = 0.10F;
    private static final int LATITUDE_SEGMENTS = 10;
    private static final int LONGITUDE_SEGMENTS = 20;
    private static final double RENDER_DISTANCE = 384.0D;
    private static final SphereVertex[] UNIT_SPHERE = buildUnitSphere();

    private SafeBubbleRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;
        if (!ClientDataStorage.isPurificationRiftActive()) return;

        List<int[]> bubbles = ClientDataStorage.getBubblePositions();
        if (bubbles == null || bubbles.isEmpty()) return;

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();

        // 先把真正要画的气泡挑出来。末地这类开阔维度里气泡经常全部超出视距，
        // 一个顶点都写不出来，此时如果照样提交批次，BufferBuilder.buildOrThrow()
        // 会因为 MeshData 为 null 抛 IllegalStateException("BufferBuilder was empty")，
        // 直接把客户端崩掉；同时也会白开一次 Tessellator 批次污染公共缓冲。
        List<int[]> visible = new ArrayList<>(bubbles.size());
        for (int i = 0; i < bubbles.size(); i++) {
            int[] bubble = bubbles.get(i);
            if (bubble.length < 4) continue;

            double x = bubble[0] + 0.5D;
            double y = bubble[1] + 1.0D;
            double z = bubble[2] + 0.5D;
            if (player.distanceToSqr(x, y, z) > RENDER_DISTANCE * RENDER_DISTANCE) continue;
            visible.add(bubble);
        }
        if (visible.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < visible.size(); i++) {
            int[] bubble = visible.get(i);
            double x = bubble[0] + 0.5D;
            double y = bubble[1] + 1.0D;
            double z = bubble[2] + 0.5D;
            double radius = bubble[3];
            int colorIndex = bubble.length >= 5 ? bubble[4] : i;

            float[] color = BUBBLE_COLORS[Math.floorMod(colorIndex, BUBBLE_COLORS.length)];
            appendSphere(buffer, matrix, x, y, z, radius, color[0], color[1], color[2]);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void appendSphere(BufferBuilder buffer, Matrix4f matrix, double centerX, double centerY,
                                     double centerZ, double radius, float red, float green, float blue) {
        for (SphereVertex vertex : UNIT_SPHERE) {
            buffer.addVertex(matrix,
                            (float) (centerX + vertex.x * radius),
                            (float) (centerY + vertex.y * radius),
                            (float) (centerZ + vertex.z * radius))
                    .setColor(red, green, blue, vertex.alpha);
        }
    }

    private static SphereVertex[] buildUnitSphere() {
        SphereVertex[] vertices = new SphereVertex[LATITUDE_SEGMENTS * LONGITUDE_SEGMENTS * 6];
        int index = 0;

        for (int lat = 0; lat < LATITUDE_SEGMENTS; lat++) {
            double phi1 = Math.PI * lat / LATITUDE_SEGMENTS;
            double phi2 = Math.PI * (lat + 1) / LATITUDE_SEGMENTS;
            float alpha = alphaForLatitude(lat);

            for (int lon = 0; lon < LONGITUDE_SEGMENTS; lon++) {
                double theta1 = 2.0D * Math.PI * lon / LONGITUDE_SEGMENTS;
                double theta2 = 2.0D * Math.PI * (lon + 1) / LONGITUDE_SEGMENTS;

                SphereVertex v1 = spherePoint(phi1, theta1, alpha);
                SphereVertex v2 = spherePoint(phi2, theta1, alpha);
                SphereVertex v3 = spherePoint(phi2, theta2, alpha);
                SphereVertex v4 = spherePoint(phi1, theta2, alpha);

                vertices[index++] = v1;
                vertices[index++] = v2;
                vertices[index++] = v3;
                vertices[index++] = v1;
                vertices[index++] = v3;
                vertices[index++] = v4;
            }
        }
        return vertices;
    }

    private static float alphaForLatitude(int lat) {
        double normalized = Math.abs((lat + 0.5D) / LATITUDE_SEGMENTS - 0.5D) * 2.0D;
        return (float) (INNER_ALPHA + (SURFACE_ALPHA - INNER_ALPHA) * normalized);
    }

    private static SphereVertex spherePoint(double phi, double theta, float alpha) {
        double sinPhi = Math.sin(phi);
        return new SphereVertex(
                (float) (sinPhi * Math.cos(theta)),
                (float) Math.cos(phi),
                (float) (sinPhi * Math.sin(theta)),
                alpha);
    }

    private record SphereVertex(float x, float y, float z, float alpha) {
    }
}
