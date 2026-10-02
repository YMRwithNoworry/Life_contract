package org.alku.life_contract.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.TriState;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.PacketSyncLifePoints;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
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
        event.setCanRender(TriState.TRUE);
    }

    private static int getSyncedLifePoints(Player player) {
        for (PacketSyncLifePoints.PlayerLifePoints data : ClientDataStorage.getPlayerLifePoints()) {
            if (player.getUUID().equals(data.uuid())) {
                return data.lifePoints();
            }
        }
        return -1;
    }
}
