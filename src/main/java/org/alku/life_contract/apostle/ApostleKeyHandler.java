package org.alku.life_contract.apostle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.KeyBindings;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class ApostleKeyHandler {

    private static boolean teleportPressed = false;
    private static boolean fireballPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        String professionId = getProfessionId(player);
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ClientProfessionCache.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;

        CompoundTag data = player.getPersistentData();
        
        boolean inFire = data.getBoolean("ApostleInFireClient");
        int reduction = inFire ? 2 : 1;
        
        int teleportCooldown = data.getInt("ApostleTeleportCooldownClient");
        if (teleportCooldown > 0) {
            data.putInt("ApostleTeleportCooldownClient", Math.max(0, teleportCooldown - reduction));
        }
        
        int fireballCooldown = data.getInt("ApostleFireballCooldownClient");
        if (fireballCooldown > 0) {
            data.putInt("ApostleFireballCooldownClient", Math.max(0, fireballCooldown - reduction));
        }

        if (KeyBindings.APOSTLE_TELEPORT.isDown() && !teleportPressed) {
            NetworkHandler.sendApostleSkillPacket(PacketApostleSkill.SKILL_TELEPORT);
            teleportPressed = true;
        } else if (!KeyBindings.APOSTLE_TELEPORT.isDown()) {
            teleportPressed = false;
        }

        if (KeyBindings.APOSTLE_FIREBALL.isDown() && !fireballPressed) {
            NetworkHandler.sendApostleSkillPacket(PacketApostleSkill.SKILL_FIREBALL);
            fireballPressed = true;
        } else if (!KeyBindings.APOSTLE_FIREBALL.isDown()) {
            fireballPressed = false;
        }
    }
    
    private static String getProfessionId(LocalPlayer player) {
        ClientDataStorage.PlayerData data = ClientDataStorage.get(player.getUUID());
        if (data != null && data.profession != null && !data.profession.isEmpty()) {
            return data.profession;
        }
        
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains("LifeContractProfession")) {
            return persistentData.getString("LifeContractProfession");
        }
        
        return "";
    }
}
