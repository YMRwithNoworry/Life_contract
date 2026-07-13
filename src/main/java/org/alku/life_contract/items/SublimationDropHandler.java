package org.alku.life_contract.items;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public final class SublimationDropHandler {
    private SublimationDropHandler() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ItemEntity drop = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(Life_contract.SUBLIMATION.get())
        );
        event.getDrops().add(drop);
    }
}
