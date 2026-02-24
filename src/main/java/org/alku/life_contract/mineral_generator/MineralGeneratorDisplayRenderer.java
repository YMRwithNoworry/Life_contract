package org.alku.life_contract.mineral_generator;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ClientDataStorage;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class MineralGeneratorDisplayRenderer {

    private static final int MAX_RENDER_DISTANCE = 32;
    private static final float TEXT_SCALE_BASE = 0.025f;
    private static final float TEXT_Y_OFFSET = 3.5f;
    private static final int TEXT_COLOR_WHITE = 0xFFFFFF;
    private static final int TEXT_COLOR_YELLOW = 0xFFFF00;
    private static final int BG_COLOR = (int) (255 * 0.4) << 24;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
        Level level = mc.level;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        for (Map.Entry<BlockPos, ClientDataStorage.MineralGeneratorData> entry : ClientDataStorage.MINERAL_GENERATOR_CACHE.entrySet()) {
            BlockPos pos = entry.getKey();
            ClientDataStorage.MineralGeneratorData data = entry.getValue();

            double distance = mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distanceSqrt = Math.sqrt(distance);
            
            if (distanceSqrt > MAX_RENDER_DISTANCE) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof MineralGeneratorBlockEntity)) {
                continue;
            }

            renderMineralGeneratorDisplay(poseStack, bufferSource, mc, pos, data, cameraX, cameraY, cameraZ, distanceSqrt);
        }
    }

    private static void renderMineralGeneratorDisplay(PoseStack poseStack, MultiBufferSource bufferSource, 
            Minecraft mc, BlockPos pos, ClientDataStorage.MineralGeneratorData data,
            double cameraX, double cameraY, double cameraZ, double distance) {
        
        Font font = mc.font;
        
        double worldX = pos.getX() + 0.5;
        double worldY = pos.getY() + TEXT_Y_OFFSET;
        double worldZ = pos.getZ() + 0.5;

        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);
        poseStack.translate(worldX, worldY, worldZ);

        float scale = TEXT_SCALE_BASE * Mth.clamp((float) distance / 8, 1, 3);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-scale, -scale, scale);

        String mineralName = getMineralDisplayName(data.mineralType);
        String timeText = formatRemainingTime(data);

        float mineralNameWidth = font.width(mineralName);
        float mineralNameXOffset = -mineralNameWidth / 2;

        font.drawInBatch(mineralName, mineralNameXOffset, -10, TEXT_COLOR_WHITE, false, 
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, BG_COLOR, 15728880);

        int timeColor;
        if (!MineralGenerationConfig.isClientSideEnabled()) {
            timeColor = 0xFF5555;
        } else if (data.enabled) {
            timeColor = TEXT_COLOR_YELLOW;
        } else {
            timeColor = 0x888888;
        }
        float timeTextWidth = font.width(timeText);
        float timeTextXOffset = -timeTextWidth / 2;

        font.drawInBatch(timeText, timeTextXOffset, 2, timeColor, false, 
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, BG_COLOR, 15728880);

        poseStack.popPose();
    }

    private static String getMineralDisplayName(String mineralType) {
        try {
            MineralGeneratorBlockEntity.MineralType type = MineralGeneratorBlockEntity.MineralType.valueOf(mineralType.toUpperCase());
            return type.getDisplayName();
        } catch (IllegalArgumentException e) {
            return mineralType;
        }
    }

    private static String formatRemainingTime(ClientDataStorage.MineralGeneratorData data) {
        if (!MineralGenerationConfig.isClientSideEnabled()) {
            return "全局禁用";
        }
        
        if (!data.enabled) {
            return "已停止";
        }

        long currentClientTick = Minecraft.getInstance().level.getGameTime();
        long serverTickEstimate = data.serverTick + (currentClientTick - getLastKnownClientTick());
        long intervalTicks = data.interval * 20L;
        long ticksSinceLastGeneration = serverTickEstimate - data.lastTick;
        long remainingTicks = intervalTicks - ticksSinceLastGeneration;

        if (remainingTicks <= 0) {
            return "即将生成...";
        }

        long remainingSeconds = remainingTicks / 20;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;

        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    private static long lastKnownClientTick = 0;

    private static long getLastKnownClientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            lastKnownClientTick = mc.level.getGameTime();
        }
        return lastKnownClientTick;
    }
}
