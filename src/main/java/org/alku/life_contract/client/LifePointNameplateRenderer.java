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

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class LifePointNameplateRenderer {

    /** 名牌每帧都会重建后缀，按命数缓存即可（取值集合很小）。 */
    private static final Map<Integer, Component> LP_SUFFIX_CACHE = new HashMap<>();

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
                .append(lpSuffix(lifePoints)));
        event.setCanRender(TriState.TRUE);
    }

    private static Component lpSuffix(int lifePoints) {
        return LP_SUFFIX_CACHE.computeIfAbsent(lifePoints, points -> Component.literal("  LP: " + points)
                .withStyle(points <= 1 ? ChatFormatting.RED : ChatFormatting.AQUA));
    }

    private static int getSyncedLifePoints(Player player) {
        // 每帧、每个可见玩家都会调用，走 ClientDataStorage 里的 UUID 索引
        return ClientDataStorage.getLifePointsFor(player.getUUID());
    }
}
