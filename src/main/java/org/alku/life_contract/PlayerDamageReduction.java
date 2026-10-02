package org.alku.life_contract;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * 玩家承伤规则：
 * <ul>
 *     <li>枪械子弹（TaCZ 等）：伤害降为原来的 20%</li>
 *     <li>其他非玩家来源的普通伤害：伤害降为原来的 40%</li>
 * </ul>
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerDamageReduction {

    /** 子弹对玩家造成的伤害倍率。 */
    public static final float BULLET_DAMAGE_MULTIPLIER = 0.2F;
    /** 其他非玩家来源伤害的倍率。 */
    public static final float NON_PLAYER_DAMAGE_MULTIPLIER = 0.4F;

    /** TaCZ（永恒枪械工坊：零）的模组 ID 与类包名前缀，仅按名字识别，不产生硬依赖。 */
    private static final String TACZ_MOD_ID = "tacz";
    private static final String TACZ_PACKAGE_PREFIX = "com.tacz.";

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        DamageSource source = event.getSource();

        if (isBulletDamage(source)) {
            event.setNewDamage(event.getNewDamage() * BULLET_DAMAGE_MULTIPLIER);
            return;
        }

        if (source.getEntity() instanceof Player) {
            return;
        }

        event.setNewDamage(event.getNewDamage() * NON_PLAYER_DAMAGE_MULTIPLIER);
    }

    /**
     * 判断是否为枪械子弹造成的伤害。
     * 优先看直接伤害来源（弹丸实体），其次看伤害类型，两条路径都不需要依赖 TaCZ 本体。
     */
    private static boolean isBulletDamage(DamageSource source) {
        Entity projectile = source.getDirectEntity();
        if (projectile != null) {
            String className = projectile.getClass().getName();
            if (className.startsWith(TACZ_PACKAGE_PREFIX) && className.contains("Bullet")) {
                return true;
            }
        }

        return source.typeHolder().unwrapKey()
                .map(ResourceKey<DamageType>::location)
                .filter(id -> TACZ_MOD_ID.equals(id.getNamespace()))
                .map(id -> id.getPath().contains("bullet"))
                .orElse(false);
    }
}
