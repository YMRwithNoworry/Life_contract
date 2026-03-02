package org.alku.life_contract.heavy_knight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.KeyBindings;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class HeavyKnightKeyHandler {

    private static boolean chargePressed = false;
    private static boolean shieldBashPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        Profession profession = ClientProfessionCache.getCurrentProfession();
        if (profession == null || !profession.isHeavyKnight()) return;

        if (KeyBindings.HEAVY_KNIGHT_CHARGE.isDown() && !chargePressed) {
            NetworkHandler.CHANNEL.sendToServer(new PacketHeavyKnightSkill(PacketHeavyKnightSkill.SKILL_CHARGE));
            chargePressed = true;
        } else if (!KeyBindings.HEAVY_KNIGHT_CHARGE.isDown()) {
            chargePressed = false;
        }

        if (KeyBindings.HEAVY_KNIGHT_SHIELD_BASH.isDown() && !shieldBashPressed) {
            NetworkHandler.CHANNEL.sendToServer(new PacketHeavyKnightSkill(PacketHeavyKnightSkill.SKILL_SHIELD_BASH));
            shieldBashPressed = true;
        } else if (!KeyBindings.HEAVY_KNIGHT_SHIELD_BASH.isDown()) {
            shieldBashPressed = false;
        }
    }
}
