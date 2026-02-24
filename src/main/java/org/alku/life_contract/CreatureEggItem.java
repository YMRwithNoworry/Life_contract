package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import org.alku.life_contract.follower.FollowerEvents;
import org.alku.life_contract.mount.BeastRiderBuffSystem;
import org.alku.life_contract.mount.BeastRiderMountSystem;

import java.util.List;

public class CreatureEggItem extends Item {

    public static final String TAG_ENTITY_TYPE = "EntityType";
    public static final String TAG_ENTITY_DATA = "EntityData";
    public static final String TAG_CUSTOM_NAME = "CustomName";
    public static final String TAG_CAPTURED_HEALTH = "CapturedHealth";
    public static final String TAG_CAPTURED_MAX_HEALTH = "CapturedMaxHealth";
    public static final String TAG_IS_WITHER_GACHA = "IsWitherGacha";

    public CreatureEggItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        if (hasEntity(stack)) {
            String entityTypeKey = getEntityType(stack);
            String customName = getCustomName(stack);
            
            if (customName != null) {
                components.add(Component.literal("§e" + customName).withStyle(ChatFormatting.YELLOW));
            }
            
            if (entityTypeKey != null) {
                net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation(entityTypeKey);
                String displayName = loc.getPath().replace("_", " ");
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                components.add(Component.literal("§7类型: §f" + displayName));
            }
            
            float health = getCapturedHealth(stack);
            float maxHealth = getCapturedMaxHealth(stack);
            if (maxHealth > 0) {
                components.add(Component.literal("§c生命值: §f" + (int)health + "/" + (int)maxHealth));
            }
            
            components.add(Component.literal("§a右键方块 §7- 生成生物"));
            components.add(Component.literal("§a右键空气 §7- 在指向位置生成"));
            components.add(Component.literal("§b最大距离: §f64格"));
        } else {
            components.add(Component.literal("§c空的生物蛋").withStyle(ChatFormatting.RED));
            components.add(Component.literal("§7Shift+右键生物 §7- 捕获生物"));
        }
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (hasEntity(stack)) {
            String customName = getCustomName(stack);
            if (customName != null) {
                return Component.literal("§d[生物蛋] §f" + customName);
            }
            String entityTypeKey = getEntityType(stack);
            if (entityTypeKey != null) {
                net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation(entityTypeKey);
                String displayName = loc.getPath().replace("_", " ");
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                return Component.literal("§d[生物蛋] §f" + displayName);
            }
        }
        return Component.literal("§d[生物蛋] §7(空)");
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (hasEntity(stack)) {
                player.sendSystemMessage(Component.literal("§c这个生物蛋已经有生物了！"));
                return InteractionResult.FAIL;
            }

            if (target instanceof Player) {
                player.sendSystemMessage(Component.literal("§c无法捕获玩家！"));
                return InteractionResult.FAIL;
            }

            if (!(target instanceof Mob)) {
                player.sendSystemMessage(Component.literal("§c只能捕获生物！"));
                return InteractionResult.FAIL;
            }

            Mob mob = (Mob) target;
            if (captureEntity(stack, mob)) {
                String mobName = mob.hasCustomName() ? mob.getCustomName().getString() : mob.getName().getString();
                mob.discard();
                
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5F, 1.5F);
                player.sendSystemMessage(Component.literal("§a成功捕获: §f" + mobName));
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.literal("§c捕获失败！"));
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!hasEntity(stack)) {
            if (player != null && player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (player != null) {
                player.sendSystemMessage(Component.literal("§c这个生物蛋是空的！Shift+右键生物来捕获。"));
            }
            return InteractionResult.FAIL;
        }

        if (spawnEntity(stack, (ServerLevel) level, player, pos)) {
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!hasEntity(stack)) {
            if (player.isShiftKeyDown()) {
                return InteractionResultHolder.pass(stack);
            }
            player.sendSystemMessage(Component.literal("§c这个生物蛋是空的！Shift+右键生物来捕获。"));
            return InteractionResultHolder.fail(stack);
        }

        BlockHitResult hitResult = rayTrace(player, level, 64.0);
        BlockPos spawnPos;
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            spawnPos = hitResult.getBlockPos().relative(hitResult.getDirection());
        } else if (hitResult.getType() == HitResult.Type.MISS) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            double distance = 5.0;
            Vec3 targetPos = eyePos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);
            spawnPos = new BlockPos((int)Math.floor(targetPos.x), (int)Math.floor(targetPos.y), (int)Math.floor(targetPos.z));
            
            if (!level.getWorldBorder().isWithinBounds(spawnPos)) {
                spawnPos = player.blockPosition();
            }
        } else {
            spawnPos = player.blockPosition();
        }

        if (!isValidSpawnPosition(level, spawnPos)) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos testPos = spawnPos.offset(dx, 0, dz);
                    if (isValidSpawnPosition(level, testPos)) {
                        spawnPos = testPos;
                        break;
                    }
                }
            }
            
            if (!isValidSpawnPosition(level, spawnPos)) {
                spawnPos = findSafeSpawnPosition(level, player.blockPosition());
            }
        }

        if (spawnEntity(stack, (ServerLevel) level, player, spawnPos)) {
            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    private static BlockHitResult rayTrace(Player player, Level level, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
        
        ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        return level.clip(context);
    }

    private static boolean isValidSpawnPosition(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        net.minecraft.world.level.block.state.BlockState stateAbove = level.getBlockState(pos.above());
        
        boolean isSolidGround = state.isSolid() || state.getBlock().isPossibleToRespawnInThis(state);
        boolean hasSpace = !stateAbove.isSolid() && !stateAbove.liquid();
        
        return isSolidGround && hasSpace;
    }

    private static BlockPos findSafeSpawnPosition(Level level, BlockPos center) {
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos testPos = center.offset(dx, dy, dz);
                    if (isValidSpawnPosition(level, testPos)) {
                        return testPos;
                    }
                }
            }
        }
        return center;
    }

    public static boolean hasEntity(ItemStack stack) {
        return stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_ENTITY_TYPE);
    }

    public static boolean captureEntity(ItemStack stack, Mob mob) {
        CompoundTag tag = stack.getOrCreateTag();
        
        String entityTypeKey = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
        tag.putString(TAG_ENTITY_TYPE, entityTypeKey);
        
        CompoundTag entityData = new CompoundTag();
        mob.saveWithoutId(entityData);
        entityData.remove("UUID");
        entityData.remove("Pos");
        entityData.remove("Dimension");
        entityData.remove("Rotation");
        entityData.remove("Motion");
        entityData.remove("DeathTime");
        entityData.remove("HurtTime");
        entityData.remove("HurtByTimestamp");
        entityData.remove("FallDistance");
        entityData.remove("Fire");
        entityData.remove("Air");
        entityData.remove("OnGround");
        entityData.remove("SpawningEntities");
        entityData.remove("DeathLootTable");
        entityData.remove("DeathLootTableSeed");
        entityData.remove("CanPickUpLoot");
        entityData.remove("Leash");
        entityData.remove("PersistenceRequired");
        tag.put(TAG_ENTITY_DATA, entityData);
        
        if (mob.hasCustomName()) {
            tag.putString(TAG_CUSTOM_NAME, mob.getCustomName().getString());
        }
        
        tag.putFloat(TAG_CAPTURED_HEALTH, mob.getHealth());
        tag.putFloat(TAG_CAPTURED_MAX_HEALTH, mob.getMaxHealth());
        
        return true;
    }

    public static boolean spawnEntity(ItemStack stack, ServerLevel level, @Nullable Player player, BlockPos pos) {
        String entityTypeKey = getEntityType(stack);
        if (entityTypeKey == null) return false;

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(
            new net.minecraft.resources.ResourceLocation(entityTypeKey)
        );
        if (entityType == null) {
            if (player != null) {
                player.sendSystemMessage(Component.literal("§c无法找到该生物类型！"));
            }
            return false;
        }

        Entity entity = entityType.spawn(level, stack, player, pos, net.minecraft.world.entity.MobSpawnType.SPAWN_EGG, true, true);

        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            
            CompoundTag entityData = getEntityData(stack);
            if (!entityData.isEmpty()) {
                entityData.remove("UUID");
                entityData.remove("Pos");
                entityData.remove("Dimension");
                entityData.remove("DeathTime");
                entityData.remove("HurtTime");
                entityData.remove("HurtByTimestamp");
                entityData.remove("FallDistance");
                entityData.remove("Fire");
                entityData.remove("Air");
                entityData.remove("OnGround");
                entityData.remove("SpawningEntities");
                entityData.remove("DeathLootTable");
                entityData.remove("DeathLootTableSeed");
                entityData.remove("Leash");
                
                mob.load(entityData);
                mob.setPersistenceRequired();
                mob.setHealth(mob.getHealth());
            }
            
            String customName = getCustomName(stack);
            if (customName != null) {
                mob.setCustomName(Component.literal(customName));
                mob.setCustomNameVisible(true);
            }
            
            float capturedHealth = getCapturedHealth(stack);
            float capturedMaxHealth = getCapturedMaxHealth(stack);
            if (capturedMaxHealth > 0) {
                mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(capturedMaxHealth);
                mob.setHealth(Math.min(capturedHealth, capturedMaxHealth));
            }
            
            if (stack.hasTag() && stack.getTag() != null && stack.getTag().getBoolean(TAG_IS_WITHER_GACHA)) {
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WEAKNESS,
                    Integer.MAX_VALUE,
                    1,
                    false,
                    false
                ));
            }
            
            mob.setInvulnerable(false);
            mob.setNoAi(false);
            mob.clearFire();
            mob.fallDistance = 0;
            mob.setTicksFrozen(0);
            mob.setDeltaMovement(Vec3.ZERO);
            
            if (player != null) {
                FollowerEvents.registerFollower(mob, player.getUUID());
            }
            
            if (player != null && BeastRiderMountSystem.isBeastRider(player)) {
                BeastRiderBuffSystem.applyBeastRiderBuff(mob, player.getUUID());
            }
            
            level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, 0.8F);
            level.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.5F, 1.0F);
            level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.3F, 1.2F);
            
            spawnParticles(level, pos);
            
            if (player != null) {
                String mobName = mob.hasCustomName() ? mob.getCustomName().getString() : mob.getName().getString();
                player.sendSystemMessage(Component.literal("§a成功释放: §f" + mobName));
            }
            
            return true;
        } else if (entity != null) {
            entity.discard();
        }

        return false;
    }

    private static void spawnParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.0;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.0;
            level.sendParticles(ParticleTypes.PORTAL, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0.1, 0, 0.05);
        }
        
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetY = level.random.nextDouble() * 1.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;
            level.sendParticles(ParticleTypes.END_ROD, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0.05, 0, 0.02);
        }
        
        for (int i = 0; i < 15; i++) {
            double angle = (i / 15.0) * Math.PI * 2;
            double radius = 1.0;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, y + 0.5, pz, 1, 0, 0.02, 0, 0.01);
        }
        
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 0.8;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.FLASH, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0);
        }
    }

    public static String getEntityType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_ENTITY_TYPE)) {
            return stack.getTag().getString(TAG_ENTITY_TYPE);
        }
        return null;
    }

    public static CompoundTag getEntityData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_ENTITY_DATA)) {
            return stack.getTag().getCompound(TAG_ENTITY_DATA);
        }
        return new CompoundTag();
    }

    public static String getCustomName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_CUSTOM_NAME)) {
            return stack.getTag().getString(TAG_CUSTOM_NAME);
        }
        return null;
    }

    public static float getCapturedHealth(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_CAPTURED_HEALTH)) {
            return stack.getTag().getFloat(TAG_CAPTURED_HEALTH);
        }
        return 0;
    }

    public static float getCapturedMaxHealth(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_CAPTURED_MAX_HEALTH)) {
            return stack.getTag().getFloat(TAG_CAPTURED_MAX_HEALTH);
        }
        return 0;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasEntity(stack);
    }
}
