package org.alku.life_contract;
import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class TeamHighlightRenderer {
    private static final double GOLEM_RENDER_DISTANCE = 20.0D;
    /** 守卫血条是每帧渲染的，但“附近有哪些守卫”不必每帧查询一次。 */
    private static final int GOLEM_QUERY_INTERVAL_TICKS = 10;

    private static java.util.List<IronGolem> cachedTeamGolems = java.util.List.of();
    private static int lastGolemQueryTick = -1000;

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
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (Player target : mc.level.players()) {
            if (target != player && ContractEvents.isSameTeam(player, target)) {
                renderTeamHighlight(poseStack, bufferSource, target, partialTick);
            }
        }

        if (player.tickCount - lastGolemQueryTick >= GOLEM_QUERY_INTERVAL_TICKS) {
            lastGolemQueryTick = player.tickCount;
            AABB nearby = player.getBoundingBox().inflate(GOLEM_RENDER_DISTANCE);
            cachedTeamGolems = mc.level.getEntitiesOfClass(IronGolem.class, nearby);
        }
        for (IronGolem golem : cachedTeamGolems) {
            if (!golem.isAlive()) {
                continue;
            }
            if (TeamIronGolemSystem.isTeamGolem(golem)
                    || golem.getCustomName() != null && golem.getCustomName().getString().contains("队伍守卫")) {
                renderGolemHealth(poseStack, bufferSource, golem, partialTick);
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

        double distanceSqr = player.distanceToSqr(golem);
        if (distanceSqr > GOLEM_RENDER_DISTANCE * GOLEM_RENDER_DISTANCE) return;
        double distance = Math.sqrt(distanceSqr);

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

        float x1 = (float) aabb.minX, y1 = (float) aabb.minY, z1 = (float) aabb.minZ;
        float x2 = (float) aabb.maxX, y2 = (float) aabb.maxY, z2 = (float) aabb.maxZ;
        float[][] vertices = {
                {x1, y1, z1}, {x2, y1, z1}, {x1, y2, z1}, {x2, y2, z1},
                {x1, y1, z2}, {x2, y1, z2}, {x1, y2, z2}, {x2, y2, z2},
                {x1, y1, z1}, {x1, y2, z1}, {x2, y1, z1}, {x2, y2, z1},
                {x1, y1, z2}, {x1, y2, z2}, {x2, y1, z2}, {x2, y2, z2},
                {x1, y1, z1}, {x1, y1, z2}, {x2, y1, z1}, {x2, y1, z2},
                {x1, y2, z1}, {x1, y2, z2}, {x2, y2, z1}, {x2, y2, z2}
        };
        for (float[] vertex : vertices) {
            consumer.addVertex(pose.pose(), vertex[0], vertex[1], vertex[2])
                    .setColor(r, g, b, a)
                    .setNormal(pose, 1.0f, 0.0f, 0.0f);
        }
    }
}
