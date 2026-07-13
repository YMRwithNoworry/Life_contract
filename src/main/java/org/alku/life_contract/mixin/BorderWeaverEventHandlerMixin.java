package org.alku.life_contract.mixin;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.alku.border_weaver.handler.BorderEventHandler", remap = false)
public abstract class BorderWeaverEventHandlerMixin {
    @Inject(
            method = "onPlayerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/alku/border_weaver/handler/CountdownHandler;isTaskActive()Z",
                    ordinal = 1
            ),
            cancellable = true,
            require = 1
    )
    private static void lifeContract$suppressOverworldCountdownNoticeInEnd(
            TickEvent.PlayerTickEvent event, CallbackInfo callback) {
        if (Level.END.equals(event.player.level().dimension())) {
            callback.cancel();
        }
    }
}
