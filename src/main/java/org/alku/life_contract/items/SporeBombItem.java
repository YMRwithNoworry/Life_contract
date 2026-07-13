package org.alku.life_contract.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;

import java.util.List;

public class SporeBombItem extends Item {
    
    public SporeBombItem() {
        super(new Properties()
            .stacksTo(16)
            .rarity(Rarity.RARE)
        );
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            Snowball snowball = new Snowball(level, player);
            snowball.setItem(new ItemStack(this));
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            snowball.getPersistentData().putBoolean("SporeBomb", true);
            level.addFreshEntity(snowball);
            
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        
        player.playSound(SoundEvents.SNOWBALL_THROW, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§a孢子炸弹"));
        tooltip.add(Component.literal("§7投掷后产生孢子云爆炸"));
        tooltip.add(Component.literal("§7命中者获得缓慢III和虚弱I"));
    }
    
    @Mod.EventBusSubscriber(modid = Life_contract.MODID)
    public static class SporeBombHandler {
        
        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getProjectile() instanceof Snowball snowball)) return;
            if (!snowball.getPersistentData().getBoolean("SporeBomb")) return;
            if (snowball.level().isClientSide) return;
            
            HitResult hitResult = event.getRayTraceResult();
            Vec3 hitPos = hitResult.getLocation();
            double x = hitPos.x;
            double y = hitPos.y;
            double z = hitPos.z;
            
            ServerLevel serverLevel = (ServerLevel) snowball.level();
            
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                x, y, z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                x, y, z, 150, 3, 3, 3, 0.2);
            serverLevel.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM,
                x, y + 1, z, 50, 2, 1, 2, 0);
            
            serverLevel.playSound(null, x, y, z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5f, 1.2f);
            serverLevel.playSound(null, x, y, z,
                SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.HOSTILE, 2.0f, 0.8f);
            
            AABB affectedArea = new AABB(x - 5, y - 2, z - 5, x + 5, y + 4, z + 5);
            
            for (Entity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, affectedArea)) {
                if (entity instanceof Player player) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
                    player.sendSystemMessage(Component.literal("§2[孢子炸弹] §f你受到了孢子云影响！"));
                }
            }
        }
    }
}
