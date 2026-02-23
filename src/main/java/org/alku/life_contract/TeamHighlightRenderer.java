package org.alku.life_contract;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class TeamHighlightRenderer {

    public static boolean isHighlightEnabled = true;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!isHighlightEnabled)
            return;

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        float partialTick = mc.getFrameTime();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player target && entity != player) {
                if (ContractEvents.isSameTeam(player, target)) {
                    renderTeamHighlight(poseStack, bufferSource, target, partialTick);
                }
            }
            if (entity instanceof IronGolem golem) {
                if (TeamIronGolemSystem.isTeamGolem(golem)) {
                    renderGolemHealth(poseStack, bufferSource, golem, partialTick);
                } else if (golem.getCustomName() != null && golem.getCustomName().getString().contains("队伍守卫")) {
                    renderGolemHealth(poseStack, bufferSource, golem, partialTick);
                }
            }
        }
    }

    private static void renderTeamHighlight(PoseStack poseStack, MultiBufferSource bufferSource, Player target, float partialTicks) {
        double x = target.xo + (target.getX() - target.xo) * partialTicks;
        double y = target.yo + (target.getY() - target.yo) * partialTicks;
        double z = target.zo + (target.getZ() - target.zo) * partialTicks;

        Minecraft mc = Minecraft.getInstance();
        double cameraX = mc.gameRenderer.getMainCamera().getPosition().x;
        double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;
        double cameraZ = mc.gameRenderer.getMainCamera().getPosition().z;

        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);

        AABB aabb = new AABB(x - 0.5, y, z - 0.5, x + 0.5, y + 2.0, z + 0.5);
        float r = 0.0f;
        float g = 1.0f;
        float b = 0.0f;
        float a = 0.4f;

        renderBox(poseStack, bufferSource, aabb, r, g, b, a);

        poseStack.popPose();
    }

    private static void renderGolemHealth(PoseStack poseStack, MultiBufferSource bufferSource, IronGolem golem, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        double x = golem.xo + (golem.getX() - golem.xo) * partialTicks;
        double y = golem.yo + (golem.getY() - golem.yo) * partialTicks;
        double z = golem.zo + (golem.getZ() - golem.zo) * partialTicks;

        double cameraX = mc.gameRenderer.getMainCamera().getPosition().x;
        double cameraY = mc.gameRenderer.getMainCamera().getPosition().y;
        double cameraZ = mc.gameRenderer.getMainCamera().getPosition().z;

        double distance = player.distanceTo(golem);
        if (distance > 20) return;

        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);
        poseStack.translate(x, y + golem.getBbHeight() + 0.8, z);

        float scale = (float) (0.025 * Mth.clamp(distance / 8, 1, 3));
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-scale, -scale, scale);

        Font font = mc.font;
        float health = golem.getHealth();
        float maxHealth = golem.getMaxHealth();
        
        Integer teamNumber = TeamIronGolemSystem.getGolemTeam(golem);
        String teamText = teamNumber != null ? "§b#" + teamNumber + " " : "";
        String healthText = String.format("%.1f / %.1f", health, maxHealth);
        String fullText = teamText + healthText;

        float textWidth = font.width(fullText);
        float xOffset = -textWidth / 2;

        int bgColor = (int) (255 * 0.25) << 24;
        font.drawInBatch(fullText, xOffset, 0, 0xFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, bgColor, 15728880);

        poseStack.popPose();
    }

    private static void renderBox(PoseStack poseStack, MultiBufferSource bufferSource, AABB aabb, float r, float g, float b, float a) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 1.0f, 0.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();

        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();
        consumer.vertex(pose.pose(), (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).color(r, g, b, a).normal(pose.normal(), 0.0f, 0.0f, 1.0f).endVertex();
    }
}