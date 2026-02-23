package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeastRiderMovementController {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.level == null) return;
        
        if (!BeastRiderMountSystem.isMounted(player)) return;
        
        Mob mount = BeastRiderMountSystem.getMountEntity(player);
        if (mount == null || !mount.isAlive()) return;
        
        handleMovementInput(player, mount);
    }

    private static void handleMovementInput(LocalPlayer player, Mob mount) {
        Minecraft mc = Minecraft.getInstance();
        
        Vec3 movementInput = Vec3.ZERO;
        
        if (mc.options.keyUp.isDown()) {
            movementInput = movementInput.add(0, 0, 1);
        }
        if (mc.options.keyDown.isDown()) {
            movementInput = movementInput.add(0, 0, -1);
        }
        if (mc.options.keyLeft.isDown()) {
            movementInput = movementInput.add(1, 0, 0);
        }
        if (mc.options.keyRight.isDown()) {
            movementInput = movementInput.add(-1, 0, 0);
        }
        
        if (mc.options.keyJump.isDown() && mount.onGround()) {
            double jumpStrength = 0.5;
            mount.setDeltaMovement(mount.getDeltaMovement().add(0, jumpStrength, 0));
        }
        
        if (!movementInput.equals(Vec3.ZERO)) {
            movementInput = movementInput.normalize();
            sendMovementToServer(movementInput);
        }
    }

    private static void sendMovementToServer(Vec3 movementInput) {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountMovement(movementInput));
    }
}
