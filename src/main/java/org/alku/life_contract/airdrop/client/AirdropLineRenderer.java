package org.alku.life_contract.airdrop.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.airdrop.network.AirdropTrackClientHandler;
import org.alku.life_contract.Life_contract;
import org.joml.Matrix4f;

/**
 * 客户端渲染器：在玩家与空投之间绘制一条直线
 */
@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class AirdropLineRenderer {

    // 线条颜色 (RGBA) - 金色
    private static final float RED = 1.0f;
    private static final float GREEN = 0.85f;
    private static final float BLUE = 0.0f;
    private static final float ALPHA = 0.8f;

    // 线条宽度（通过多线偏移模拟）
    private static final double LINE_HALF_WIDTH = 0.04;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Vec3 targetPos = AirdropTrackClientHandler.getTargetPos();
        if (targetPos == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        Vec3 playerPos = mc.player.position().add(0, mc.player.getEyeHeight() / 2.0, 0);

        // 摄像机偏移
        Vec3 cameraOffset = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // 平移到摄像机位置，使世界坐标与摄像机对齐
        poseStack.translate(-cameraOffset.x, -cameraOffset.y, -cameraOffset.z);

        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        // 绘制主线条
        drawLine(consumer, matrix, playerPos, targetPos, RED, GREEN, BLUE, ALPHA);

        // 绘制加粗线条（4个方向的偏移线）
        Vec3 direction = targetPos.subtract(playerPos).normalize();
        // 获取一个垂直于方向的向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up);
        if (right.lengthSqr() < 0.001) {
            right = direction.cross(new Vec3(1, 0, 0));
        }
        right = right.normalize();
        Vec3 upOffset = right.cross(direction).normalize();

        for (double offset : new double[]{LINE_HALF_WIDTH, -LINE_HALF_WIDTH}) {
            // 水平偏移
            Vec3 rOff = right.scale(offset);
            drawLine(consumer, matrix, playerPos.add(rOff), targetPos.add(rOff),
                    RED, GREEN, BLUE, ALPHA * 0.6f);
            // 垂直偏移
            Vec3 uOff = upOffset.scale(offset);
            drawLine(consumer, matrix, playerPos.add(uOff), targetPos.add(uOff),
                    RED, GREEN, BLUE, ALPHA * 0.6f);
        }

        mc.renderBuffers().bufferSource().endBatch(RenderType.LINES);

        poseStack.popPose();
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f matrix,
                                  Vec3 from, Vec3 to,
                                  float r, float g, float b, float a) {
        // 法线方向（从起点到终点）
        Vec3 normal = to.subtract(from).normalize();
        float nx = (float) normal.x;
        float ny = (float) normal.y;
        float nz = (float) normal.z;

        consumer.addVertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .setColor(r, g, b, a)
                .setNormal(nx, ny, nz);

        consumer.addVertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .setColor(r, g, b, a)
                .setNormal(nx, ny, nz);
    }
}
