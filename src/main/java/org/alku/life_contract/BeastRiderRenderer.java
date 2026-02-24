package org.alku.life_contract;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.mount.BeastRiderMountSystem;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeastRiderRenderer {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        
        if (!BeastRiderMountSystem.isMounted(player)) {
            return;
        }
        
        Mob mount = BeastRiderMountSystem.getMountEntity(player);
        if (mount == null || !mount.isAlive()) {
            return;
        }
        
        event.setCanceled(true);
        
        renderRidingPlayer(event, player, mount);
    }

    private static void renderRidingPlayer(RenderPlayerEvent.Pre event, Player player, Mob mount) {
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = event.getPartialTick();
        
        poseStack.pushPose();
        
        float mountHeight = mount.getBbHeight();
        float mountEyeHeight = mount.getEyeHeight();
        float mountWidth = mount.getBbWidth();
        
        double offsetY = mountHeight + 0.3;
        
        float mountYaw = mount.getViewYRot(partialTick);
        float mountPitch = mount.getViewXRot(partialTick);
        
        double offsetX = Math.sin(Math.toRadians(mountYaw)) * -0.2;
        double offsetZ = Math.cos(Math.toRadians(mountYaw)) * -0.2;
        
        Vec3 mountPos = mount.getPosition(partialTick);
        
        poseStack.translate(
            mountPos.x + offsetX - event.getEntity().getX(),
            mountPos.y + offsetY - event.getEntity().getY(),
            mountPos.z + offsetZ - event.getEntity().getZ()
        );
        
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-mountYaw));
        
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(mountPitch * 0.5f));
        
        float bodyPitch = (float) Math.sin(mount.walkAnimation.position(partialTick) * 0.5f) * mount.walkAnimation.speed(partialTick) * 3.0f;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(bodyPitch * 0.5f));
        
        if (player instanceof AbstractClientPlayer clientPlayer) {
            Object renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(clientPlayer);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.render(clientPlayer, 
                    player.getYRot(), 
                    partialTick, 
                    poseStack, 
                    bufferSource, 
                    packedLight
                );
            }
        }
        
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
    }
}
