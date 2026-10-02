package org.alku.life_contract.items;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.alku.life_contract.Life_contract;

/**
 * 所有生物死亡时有 50% 几率额外掉落 1 个火药（玩家死亡不触发）。
 */
@EventBusSubscriber(modid = Life_contract.MODID)
public final class GunpowderDropHandler {

    /** 掉落几率。 */
    public static final float GUNPOWDER_DROP_CHANCE = 0.5F;
    /** 单次掉落数量。 */
    public static final int GUNPOWDER_AMOUNT = 1;

    private GunpowderDropHandler() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;
        // 玩家死亡走背包掉落逻辑，不算“生物掉落”
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().getRandom().nextFloat() >= GUNPOWDER_DROP_CHANCE) return;

        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(Items.GUNPOWDER, GUNPOWDER_AMOUNT)
        ));
    }
}
