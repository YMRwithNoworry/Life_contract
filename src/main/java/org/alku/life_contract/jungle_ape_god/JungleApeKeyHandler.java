package org.alku.life_contract.jungle_ape_god;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.KeyBindings;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class JungleApeKeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null) return;

        String professionId = ClientDataStorage.getProfessionId();
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        if (KeyBindings.JUNGLE_APE_Q1.consumeClick()) {
            NetworkHandler.sendJungleApeSkillPacket(PacketJungleApeSkill.SKILL_Q1);
        }
        
        if (KeyBindings.JUNGLE_APE_Q2.consumeClick()) {
            if (mc.crosshairPickEntity != null) {
                NetworkHandler.sendJungleApeSkillPacket(PacketJungleApeSkill.SKILL_Q2, mc.crosshairPickEntity.getId());
            } else {
                NetworkHandler.sendJungleApeSkillPacket(PacketJungleApeSkill.SKILL_Q2);
            }
        }
        
        if (KeyBindings.JUNGLE_APE_Q3.consumeClick()) {
            NetworkHandler.sendJungleApeSkillPacket(PacketJungleApeSkill.SKILL_Q3);
        }
        
        if (KeyBindings.JUNGLE_APE_R.consumeClick()) {
            NetworkHandler.sendJungleApeSkillPacket(PacketJungleApeSkill.SKILL_R);
        }
    }
}
