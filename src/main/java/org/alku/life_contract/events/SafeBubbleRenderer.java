package org.alku.life_contract.events;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class SafeBubbleRenderer {

    private static final float[][] BUBBLE_COLORS = {
        {0.75f, 0.95f, 1.00f},
        {0.45f, 1.00f, 0.55f},
        {0.45f, 0.70f, 1.00f},
        {1.00f, 0.55f, 0.95f},
        {1.00f, 0.90f, 0.45f}
    };

    private static final float SURFACE_ALPHA = 0.22f;
    private static final float INNER_ALPHA = 0.10f;
    private static final int LATITUDE_SEGMENTS = 10;
    private static final int LONGITUDE_SEGMENTS = 20;
    private static final double RENDER_DISTANCE = 384.0;
    private static final SphereVertex[] UNIT_SPHERE_VERTICES = buildUnitSphereVertices();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || !ClientDataStorage.isPurificationRiftActive()) {
            return;
        }

        List<int[]> bubbles = ClientDataStorage.getBubblePositions();
        if (bubbles == null || bubbles.isEmpty()) {
            return;
        }

        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (int i = 0; i < bubbles.size(); i++) {
            int[] bubble = bubbles.get(i);
            if (bubble.length < 4) {
                continue;
            }

            double x = bubble[0] + 0.5;
            double y = bubble[1] + 1.0;
            double z = bubble[2] + 0.5;
            double radius = bubble[3];
            int colorIndex = bubble.length >= 5 ? bubble[4] : i;

            if (player.distanceToSqr(x, y, z) > RENDER_DISTANCE * RENDER_DISTANCE) {
                continue;
            }

            float[] color = BUBBLE_COLORS[Math.floorMod(colorIndex, BUBBLE_COLORS.length)];
            renderSphere(poseStack, x, y, z, radius, color[0], color[1], color[2]);
        }

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderSphere(PoseStack poseStack, double centerX, double centerY, double centerZ,
                                     double radius, float red, float green, float blue) {
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (SphereVertex vertex : UNIT_SPHERE_VERTICES) {
            buffer.vertex(matrix,
                    (float) (centerX + vertex.x * radius),
                    (float) (centerY + vertex.y * radius),
                    (float) (centerZ + vertex.z * radius))
                .color(red, green, blue, vertex.alpha)
                .endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    private static SphereVertex[] buildUnitSphereVertices() {
        SphereVertex[] vertices = new SphereVertex[LATITUDE_SEGMENTS * LONGITUDE_SEGMENTS * 6];
        int index = 0;

        for (int lat = 0; lat < LATITUDE_SEGMENTS; lat++) {
            double phi1 = Math.PI * lat / LATITUDE_SEGMENTS;
            double phi2 = Math.PI * (lat + 1) / LATITUDE_SEGMENTS;
            float alpha = alphaForLatitude(lat);

            for (int lon = 0; lon < LONGITUDE_SEGMENTS; lon++) {
                double theta1 = 2 * Math.PI * lon / LONGITUDE_SEGMENTS;
                double theta2 = 2 * Math.PI * (lon + 1) / LONGITUDE_SEGMENTS;

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
        double normalized = Math.abs((lat + 0.5) / LATITUDE_SEGMENTS - 0.5) * 2.0;
        return (float) (INNER_ALPHA + (SURFACE_ALPHA - INNER_ALPHA) * normalized);
    }

    private static SphereVertex spherePoint(double phi, double theta, float alpha) {
        double sinPhi = Math.sin(phi);
        return new SphereVertex(
            (float) (sinPhi * Math.cos(theta)),
            (float) Math.cos(phi),
            (float) (sinPhi * Math.sin(theta)),
            alpha
        );
    }

    private static class SphereVertex {
        private final float x;
        private final float y;
        private final float z;
        private final float alpha;

        private SphereVertex(float x, float y, float z, float alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.alpha = alpha;
        }
    }
}
