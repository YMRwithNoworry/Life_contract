package org.alku.life_contract.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.alku.life_contract.client.XaeroBorderOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.render.module.ModuleRenderContext;

@Mixin(value = xaero.hud.minimap.module.MinimapRenderer.class, remap = false)
public abstract class XaeroMinimapRendererMixin {
    @Inject(method = "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("TAIL"), remap = false)
    private void lifeContract$renderBorder(MinimapSession session, ModuleRenderContext context,
                                           GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        XaeroBorderOverlayRenderer.render(session, context, graphics, partialTick);
    }
}
