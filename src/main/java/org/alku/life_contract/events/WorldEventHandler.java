package org.alku.life_contract.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class WorldEventHandler {
    
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            GameEventManager.loadGameData(level);
        }
    }
    
    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel level) {
            GameEventManager.saveGameData();
        }
    }
}
