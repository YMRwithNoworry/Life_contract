package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GamblerDiceItem extends Item {
    private static final String TAG_DICE_COOLDOWN = "GamblerDiceCooldown";
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    
    public GamblerDiceItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        
        String professionId = ContractEvents.getEffectiveProfessionId(serverPlayer);
        if (professionId == null || professionId.isEmpty()) {
            serverPlayer.displayClientMessage(
                Component.literal("§c[赌徒骰子] §r你不是赌徒，无法使用这个物品！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasDiceAbility()) {
            serverPlayer.displayClientMessage(
                Component.literal("§c[赌徒骰子] §r只有赌徒职业才能使用这个物品！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }
        
        int cooldownTicks = serverPlayer.getPersistentData().getInt(TAG_DICE_COOLDOWN);
        if (cooldownTicks > 0) {
            int remainingSeconds = (cooldownTicks + 19) / 20;
            serverPlayer.displayClientMessage(
                Component.literal("§e[赌徒骰子] §r冷却中，还需等待 " + remainingSeconds + " 秒！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }
        
        List<String> skillPool = profession.getDiceSkillPool();
        if (skillPool.isEmpty()) {
            serverPlayer.displayClientMessage(
                Component.literal("§c[赌徒骰子] §r技能池为空！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }
        
        String selectedSkill = skillPool.get(serverPlayer.getRandom().nextInt(skillPool.size()));
        executeRandomSkill(serverPlayer, selectedSkill);
        
        int cooldownSeconds = profession.getDiceCooldown();
        serverPlayer.getPersistentData().putInt(TAG_DICE_COOLDOWN, cooldownSeconds * 20);
        
        playDiceRollEffects(serverPlayer);
        
        return InteractionResultHolder.success(stack);
    }

    private void executeRandomSkill(ServerPlayer player, String skillId) {
        switch (skillId) {
            case "poisoner_attack" -> executePoisonerAttack(player);
            case "turtle_defense" -> executeTurtleDefense(player);
            case "jungle_poison" -> executeJunglePoison(player);
            case "ender_teleport" -> executeEnderTeleport(player);
            case "blaze_fire" -> executeBlazeFire(player);
            case "gacha_summon" -> executeGachaSummon(player);
            case "beast_mount" -> executeBeastMount(player);
            default -> player.sendSystemMessage(Component.literal("§c[赌徒骰子] §r未知技能: " + skillId));
        }
    }

    private void executePoisonerAttack(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, true));
        player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §c邪毒者之击§f！获得攻击提升效果！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    private void executeTurtleDefense(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0, false, false));
        
        var armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null) {
            AttributeModifier modifier = new AttributeModifier(
                ARMOR_MODIFIER_UUID,
                "GamblerDiceTurtleArmor",
                10.0,
                AttributeModifier.Operation.ADDITION
            );
            armorAttribute.addPermanentModifier(modifier);
        }
        
        player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §b玄龟之盾§f！获得防御提升效果！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
            );
        }
        
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
                if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
                if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                    var attr = player.getAttribute(Attributes.ARMOR);
                    if (attr != null) {
                        attr.removeModifier(ARMOR_MODIFIER_UUID);
                    }
                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        });
    }

    private void executeJunglePoison(ServerPlayer player) {
        double radius = 8.0;
        List<net.minecraft.world.entity.LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
            net.minecraft.world.entity.LivingEntity.class,
            player.getBoundingBox().inflate(radius),
            entity -> entity != player && !(entity instanceof Player)
        );
        
        int poisonedCount = 0;
        for (net.minecraft.world.entity.LivingEntity entity : nearbyEntities) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, true));
            poisonedCount++;
        }
        
        player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §2丛林诅咒§f！周围 " + poisonedCount + " 个敌人中毒！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SNEEZE,
                player.getX(), player.getY() + 1, player.getZ(),
                50, radius, 1, radius, 0.05
            );
        }
    }

    private void executeEnderTeleport(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        
        player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL, 3));
        
        player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §d末影传送§f！获得隐身效果和末影珍珠！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 0.5, 0.5, 0.5
            );
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.3, 0.3, 0.3, 0.3
            );
        }
        
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void executeBlazeFire(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, true));
        
        double radius = 5.0;
        List<net.minecraft.world.entity.LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
            net.minecraft.world.entity.LivingEntity.class,
            player.getBoundingBox().inflate(radius),
            entity -> entity != player && !(entity instanceof Player)
        );
        
        int burnedCount = 0;
        for (net.minecraft.world.entity.LivingEntity entity : nearbyEntities) {
            entity.setSecondsOnFire(5);
            burnedCount++;
        }
        
        player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §6烈焰风暴§f！获得火焰免疫，点燃周围 " + burnedCount + " 个敌人！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                100, radius, 1, radius, 0.1
            );
            serverLevel.sendParticles(
                ParticleTypes.LAVA,
                player.getX(), player.getY() + 0.5, player.getZ(),
                20, radius, 0.5, radius, 0.0
            );
        }
        
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void executeGachaSummon(ServerPlayer player) {
        List<String> summonPool = new ArrayList<>();
        summonPool.add("minecraft:wolf");
        summonPool.add("minecraft:iron_golem");
        summonPool.add("minecraft:snow_golem");
        summonPool.add("minecraft:bee");
        summonPool.add("minecraft:fox");
        summonPool.add("minecraft:panda");
        summonPool.add("minecraft:dolphin");
        
        String entityId = summonPool.get(player.getRandom().nextInt(summonPool.size()));
        
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(
            new net.minecraft.resources.ResourceLocation(entityId)
        );
        
        if (entityType != null) {
            Entity entity = entityType.create(player.level());
            if (entity != null) {
                entity.moveTo(player.getX() + player.getRandom().nextDouble() * 4 - 2, 
                             player.getY(), 
                             player.getZ() + player.getRandom().nextDouble() * 4 - 2);
                
                if (entity instanceof Mob mob) {
                    mob.setTarget(null);
                }
                
                player.level().addFreshEntity(entity);
                
                String entityName = entity.getType().getDescription().getString();
                player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §d扭蛋召唤§f！召唤了一只 §e" + entityName + "§f！"));
            }
        }
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                40, 1, 1, 1, 0.1
            );
            serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.5, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.05
            );
        }
        
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private void executeBeastMount(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 1, false, true));
        
        player.sendSystemMessage(Component.literal("§a[赌徒骰子] §f掷出了 §e驯兽之魂§f！获得速度和跳跃提升！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
            );
        }
        
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.HORSE_SADDLE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void playDiceRollEffects(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.5, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.3
            );
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.05
            );
        }
        
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.5F);
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }
    
    @Override
    public net.minecraft.world.entity.Entity createEntity(Level level, net.minecraft.world.entity.Entity location, ItemStack itemstack) {
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§d赌徒专属物品"));
        tooltip.add(Component.literal("§b★ 永久绑定 - 死亡不掉落"));
        tooltip.add(Component.literal("§7右键使用，随机触发一个职业技能"));
        tooltip.add(Component.literal("§e技能池:"));
        tooltip.add(Component.literal("§7- §c邪毒者之击 §8(攻击提升)"));
        tooltip.add(Component.literal("§7- §b玄龟之盾 §8(防御提升)"));
        tooltip.add(Component.literal("§7- §2丛林诅咒 §8(范围中毒)"));
        tooltip.add(Component.literal("§7- §d末影传送 §8(隐身+珍珠)"));
        tooltip.add(Component.literal("§7- §6烈焰风暴 §8(火焰免疫+点燃)"));
        tooltip.add(Component.literal("§7- §d扭蛋召唤 §8(随机召唤)"));
        tooltip.add(Component.literal("§7- §e驯兽之魂 §8(速度提升)"));
    }
}
