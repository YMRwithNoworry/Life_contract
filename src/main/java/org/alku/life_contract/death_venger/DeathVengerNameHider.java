package org.alku.life_contract.death_venger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DeathVengerNameHider {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
                if (!(event.getEntity() instanceof Player player)) return;

                String professionId = ClientDataStorage.getSelfProfessionId();
                if (professionId == null || professionId.isEmpty()) return;

                Profession profession = ClientProfessionCache.getProfession(professionId);
                if (profession == null) return;

                if (profession.shouldHideNameTag()) {
                    event.setContent(null);
                }
            }
}
