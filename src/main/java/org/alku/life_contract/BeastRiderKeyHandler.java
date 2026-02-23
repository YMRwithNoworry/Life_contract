package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeastRiderKeyHandler {

    private static int behaviorIndex = 0;
    private static final BeastRiderMountSystem.MountBehavior[] BEHAVIORS = {
        BeastRiderMountSystem.MountBehavior.FOLLOW,
        BeastRiderMountSystem.MountBehavior.STAY,
        BeastRiderMountSystem.MountBehavior.DEFEND,
        BeastRiderMountSystem.MountBehavior.ATTACK
    };

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.level == null) return;
        
        if (!BeastRiderMountSystem.isBeastRider(player)) return;
        
        if (KeyBindings.MOUNT_NEAREST.consumeClick()) {
            PacketMountCommand.sendMountNearest();
        }
        
        if (KeyBindings.DISMOUNT.consumeClick()) {
            if (BeastRiderMountSystem.isMounted(player)) {
                PacketMountCommand.sendDismount();
            }
        }
        
        if (KeyBindings.MOUNT_BEHAVIOR_CYCLE.consumeClick()) {
            if (BeastRiderMountSystem.isMounted(player)) {
                cycleBehavior(player);
            }
        }
        
        if (KeyBindings.MOUNT_SPECIAL_ABILITY.consumeClick()) {
            if (BeastRiderMountSystem.isMounted(player)) {
                PacketMountCommand.sendSpecialAbility();
            }
        }
        
        if (BeastRiderMountSystem.isMounted(player)) {
            Mob mount = BeastRiderMountSystem.getMountEntity(player);
            if (mount == null || !mount.isAlive()) return;
            updateCameraPosition(player, mount);
        }
    }

    private static void cycleBehavior(LocalPlayer player) {
        behaviorIndex = (behaviorIndex + 1) % BEHAVIORS.length;
        BeastRiderMountSystem.MountBehavior newBehavior = BEHAVIORS[behaviorIndex];
        
        switch (newBehavior) {
            case FOLLOW -> PacketMountCommand.sendBehaviorFollow();
            case STAY -> PacketMountCommand.sendBehaviorStay();
            case DEFEND -> PacketMountCommand.sendBehaviorDefend();
            case ATTACK -> PacketMountCommand.sendBehaviorAttack();
        }
        
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("§b[驯兽师] §r切换行为: " + newBehavior.getName()),
            true
        );
    }

    private static void updateCameraPosition(LocalPlayer player, Mob mount) {
        float mountHeight = mount.getBbHeight();
        
        double cameraY = mount.getY() + mountHeight + 0.5;
        
        player.moveTo(
            player.getX(),
            cameraY,
            player.getZ(),
            mount.getYRot(),
            player.getXRot()
        );
    }
}
