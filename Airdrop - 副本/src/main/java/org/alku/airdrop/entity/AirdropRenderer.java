package org.alku.airdrop.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class AirdropRenderer extends EntityRenderer<AirdropEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public AirdropRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(AirdropEntity entity, float yaw, float partial, PoseStack poseStack, MultiBufferSource buffer,
            int light) {
        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        this.blockRenderer.renderSingleBlock(Blocks.RED_SHULKER_BOX.defaultBlockState(), poseStack, buffer, light,
                OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        poseStack.popPose();
        super.render(entity, yaw, partial, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(AirdropEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/red_shulker_box.png");
    }
}