package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class SporeGlandEvents {
    private static final Random RANDOM = new Random();
    private static final float SPORE_CHANCE = 0.3f;
    private static final int INFECTION_DURATION = 100; // 5秒 = 100 ticks
    private static final String TAG_SLOW_INFECTION = "SlowInfection";
    private static final String TAG_SPORE_SPAWN_TIME = "SporeSpawnTime";
    
    private static final Set<String> ELITE_MOBS = Set.of(
        "spore:knight",
        "spore:griefer",
        "spore:braiomil",
        "spore:leaper",
        "spore:slasher",
        "spore:spitter",
        "spore:howler",
        "spore:stalker",
        "spore:brute",
        "spore:scavenger",
        "spore:bloater",
        "spore:volatile",
        "spore:mephitic",
        "spore:protector",
        "spore:gargoyle",
        "spore:conductor",
        "spore:chemist",
        "spore:inebriater",
        "spore:naiad",
        "spore:nuckelavee",
        "spore:inquisitor",
        "spore:brot",
        "spore:grober",
        "spore:wendigo",
        "spore:ogre",
        "spore:hevoker",
        "spore:hvindicator",
        "spore:jagd",
        "spore:specter",
        "spore:vanguard",
        "spore:reaper",
        "spore:leviathan",
        "spore:hivetumor",
        "spore:howitzer",
        "spore:sieger",
        "spore:hohlfresser"
    );

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide) return;
        
        Player player = event.getEntity();
        Entity target = event.getTarget();
        
        if (!(target instanceof LivingEntity livingTarget)) return;
        if (livingTarget instanceof Player) return;
        
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper()
                .getCuriosHandler(player).resolve();
        
        if (curiosHandler.isPresent()) {
            ICuriosItemHandler handler = curiosHandler.get();
            Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler("curio");
            
            if (stacksHandler.isPresent()) {
                IDynamicStackHandler stackHandler = stacksHandler.get().getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack.getItem() instanceof SporeGlandItem) {
                        if (RANDOM.nextFloat() < SPORE_CHANCE) {
                            applySlowInfection(livingTarget, player);
                            
                            if (player.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                        livingTarget.getX(), livingTarget.getY() + 1, livingTarget.getZ(),
                                        20, 0.5, 0.5, 0.5, 0.1);
                                
                                serverLevel.playSound(null, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(),
                                        SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                            }
                            
                            player.sendSystemMessage(Component.literal("§a[孢子腺体] §f孢子已传播！"));
                        }
                        return;
                    }
                }
            }
        }
    }

    private static void applySlowInfection(LivingEntity target, Player source) {
        CompoundTag data = target.getPersistentData();
        data.putBoolean(TAG_SLOW_INFECTION, true);
        data.putInt(TAG_SPORE_SPAWN_TIME, target.tickCount + INFECTION_DURATION);
        
        target.addEffect(new MobEffectInstance(MobEffects.POISON, INFECTION_DURATION, 0, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, INFECTION_DURATION, 1, false, true));
    }

    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;
        
        LivingEntity entity = event.getEntity();
        CompoundTag data = entity.getPersistentData();
        
        if (data.getBoolean(TAG_SLOW_INFECTION)) {
            int spawnTime = data.getInt(TAG_SPORE_SPAWN_TIME);
            
            if (entity.tickCount >= spawnTime) {
                spawnSporeMinion(entity);
                data.remove(TAG_SLOW_INFECTION);
                data.remove(TAG_SPORE_SPAWN_TIME);
            }
        }
    }

    private static void spawnSporeMinion(LivingEntity source) {
        if (!(source.level() instanceof ServerLevel serverLevel)) return;
        
        Zombie sporeMinion = new Zombie(EntityType.ZOMBIE, serverLevel);
        sporeMinion.moveTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());
        
        sporeMinion.setCustomName(Component.literal("§a小孢子怪"));
        sporeMinion.setCustomNameVisible(true);
        
        sporeMinion.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
        sporeMinion.setHealth(20.0f);
        sporeMinion.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
        sporeMinion.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0);
        
        sporeMinion.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0, false, true));
        
        serverLevel.addFreshEntity(sporeMinion);
        
        serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                source.getX(), source.getY() + 1, source.getZ(),
                30, 0.5, 0.5, 0.5, 0.2);
        
        serverLevel.playSound(null, source.getX(), source.getY(), source.getZ(),
                SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.HOSTILE, 1.5f, 0.5f);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        
        LivingEntity entity = event.getEntity();
        
        String entityId = EntityType.getKey(entity.getType()).toString();
        
        if (ELITE_MOBS.contains(entityId)) {
            if (event.getSource().getEntity() instanceof Player player) {
                if (RANDOM.nextFloat() < 0.3f) {
                    ItemStack sporeGland = new ItemStack(Life_contract.SPORE_GLAND.get());
                    
                    if (!player.addItem(sporeGland)) {
                        player.drop(sporeGland, false);
                    }
                    
                    player.sendSystemMessage(Component.literal("§6[孢子腺体] §f获得孢子腺体！"));
                }
            }
        }
    }
}
