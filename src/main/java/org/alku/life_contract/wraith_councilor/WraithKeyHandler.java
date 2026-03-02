package org.alku.life_contract.wraith_councilor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.KeyBindings;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.PacketWraithSkill;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class WraithKeyHandler {
    
    private static boolean wasBarragePressed = false;
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        
        while (KeyBindings.WRAITH_SUMMON.consumeClick()) {
            NetworkHandler.sendWraithSkillPacket(PacketWraithSkill.SKILL_SUMMON, Vec3.ZERO);
        }
        
        while (KeyBindings.WRAITH_DOMAIN.consumeClick()) {
            HitResult hitResult = mc.hitResult;
            Vec3 targetPos;
            if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                targetPos = hitResult.getLocation();
            } else {
                targetPos = player.position().add(player.getLookAngle().scale(8));
            }
            NetworkHandler.sendWraithSkillPacket(PacketWraithSkill.SKILL_DOMAIN, targetPos);
        }
        
        boolean isBarragePressed = KeyBindings.WRAITH_BARRAGE.isDown();
        if (isBarragePressed && !wasBarragePressed) {
            NetworkHandler.sendWraithSkillPacket(PacketWraithSkill.SKILL_BARRAGE_START, Vec3.ZERO);
        }
        if (!isBarragePressed && wasBarragePressed) {
            NetworkHandler.sendWraithSkillPacket(PacketWraithSkill.SKILL_BARRAGE_RELEASE, Vec3.ZERO);
        }
        wasBarragePressed = isBarragePressed;
        
        while (KeyBindings.WRAITH_ULTIMATE.consumeClick()) {
            NetworkHandler.sendWraithSkillPacket(PacketWraithSkill.SKILL_ULTIMATE, Vec3.ZERO);
        }
    }
    
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
    }
}
