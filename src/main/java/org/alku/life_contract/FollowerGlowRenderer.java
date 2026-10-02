package org.alku.life_contract;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import org.alku.life_contract.follower.FollowerClientCache;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class FollowerGlowRenderer {

    private static final float OUTLINE_R = 0.0f;
    private static final float OUTLINE_G = 0.8f;
    private static final float OUTLINE_B = 1.0f;
    private static final float OUTLINE_A = 0.9f;
    private static final float RENDER_DISTANCE = 48.0f;
    private static final float BOX_EXPANSION = 0.05f;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        double cameraX = mc.gameRenderer.getMainCamera().getPosition().x;
        double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;
        double cameraZ = mc.gameRenderer.getMainCamera().getPosition().z;

        for (int entityId : FollowerClientCache.getFollowerEntityIds(player.getUUID())) {
            if (!(mc.level.getEntity(entityId) instanceof Mob mob)
                    || !FollowerClientCache.isFollowerOf(mob.getUUID(), player.getUUID())
                    || player.distanceToSqr(mob) > RENDER_DISTANCE * RENDER_DISTANCE) {
                continue;
            }

            renderFollowerOutline(poseStack, bufferSource, mob, partialTick, cameraX, cameraY, cameraZ);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LocalPlayer) {
            FollowerClientCache.clear();
        }
    }

    private static void renderFollowerOutline(PoseStack poseStack, MultiBufferSource bufferSource, 
                                               Mob mob, float partialTicks,
                                               double cameraX, double cameraY, double cameraZ) {
        double x = mob.xo + (mob.getX() - mob.xo) * partialTicks;
        double y = mob.yo + (mob.getY() - mob.yo) * partialTicks;
        double z = mob.zo + (mob.getZ() - mob.zo) * partialTicks;

        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);

        AABB aabb = mob.getBoundingBox().move(-mob.getX(), -mob.getY(), -mob.getZ())
                .move(x, y, z)
                .inflate(BOX_EXPANSION);

        renderOutlineBox(poseStack, bufferSource, aabb, OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A);

        poseStack.popPose();
    }

    private static void renderOutlineBox(PoseStack poseStack, MultiBufferSource bufferSource, 
                                          AABB aabb, float r, float g, float b, float a) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();

        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        float[][] vertices = {
                {minX, minY, minZ}, {maxX, minY, minZ}, {minX, maxY, minZ}, {maxX, maxY, minZ},
                {minX, minY, maxZ}, {maxX, minY, maxZ}, {minX, maxY, maxZ}, {maxX, maxY, maxZ},
                {minX, minY, minZ}, {minX, maxY, minZ}, {maxX, minY, minZ}, {maxX, maxY, minZ},
                {minX, minY, maxZ}, {minX, maxY, maxZ}, {maxX, minY, maxZ}, {maxX, maxY, maxZ},
                {minX, minY, minZ}, {minX, minY, maxZ}, {maxX, minY, minZ}, {maxX, minY, maxZ},
                {minX, maxY, minZ}, {minX, maxY, maxZ}, {maxX, maxY, minZ}, {maxX, maxY, maxZ}
        };
        for (float[] vertex : vertices) {
            consumer.addVertex(pose.pose(), vertex[0], vertex[1], vertex[2])
                    .setColor(r, g, b, a)
                    .setNormal(pose, 1.0f, 0.0f, 0.0f);
        }
    }
}
