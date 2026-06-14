package org.alku.life_contract.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.events.PacketSyncEvents;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class LifePointNameplateRenderer {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        int lifePoints = getSyncedLifePoints(player);
        if (lifePoints < 0) {
            return;
        }

        event.setContent(Component.empty()
                .append(event.getContent())
                .append(Component.literal("  LP: " + lifePoints).withStyle(lifePoints <= 1 ? ChatFormatting.RED : ChatFormatting.AQUA)));
        event.setResult(Event.Result.ALLOW);
    }

    private static int getSyncedLifePoints(Player player) {
        for (PacketSyncEvents.PlayerPosData data : ClientDataStorage.getPlayerPositions()) {
            if (player.getUUID().equals(data.uuid)) {
                return data.lifePoints;
            }
        }
        return -1;
    }
}
