package org.alku.life_contract.follower;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import org.alku.life_contract.CreatureEggItem;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;

import java.util.List;
import java.util.UUID;

public class FollowerWandItem extends Item {

    public static final String TAG_CAPTURED_MOBS = "CapturedMobs";
    public static final String TAG_OWNER_UUID = "OwnerUUID";
    public static final String TAG_ENTITY_TYPE = "EntityType";
    public static final String TAG_ENTITY_DATA = "EntityData";
    public static final String TAG_CUSTOM_NAME = "CustomName";
    public static final int CONTAINER_SIZE = 54;
    private static final int CAPTURE_HUNGER_COST = 3;

    public FollowerWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("\u00a7d[\u8ddf\u968f\u4e4b\u6756]").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal("\u00a7e\u53f3\u952e\u751f\u7269 \u00a77- \u6536\u670d\u751f\u7269"));
        components.add(Component.literal("\u00a7eShift+\u53f3\u952e \u00a77- \u6253\u5f00\u5b58\u50a8\u754c\u9762"));
        components.add(Component.literal("\u00a77\u5df2\u6536\u670d: \u00a7f" + getCapturedCount(stack) + "/" + CONTAINER_SIZE));
        components.add(Component.literal("\u00a77\u6bcf\u6b21\u6536\u670d\u6d88\u8017 \u00a7e" + CAPTURE_HUNGER_COST + " \u00a77\u70b9\u9965\u997f\u503c"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("\u8ddf\u968f\u4e4b\u6756").withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            openWandGui(player, hand);
            return InteractionResult.SUCCESS;
        }

        if (target instanceof Player) {
            player.sendSystemMessage(Component.literal("\u00a7c\u65e0\u6cd5\u6536\u670d\u73a9\u5bb6\uff01"));
            return InteractionResult.FAIL;
        }

        if (!(target instanceof Mob mob)) {
            player.sendSystemMessage(Component.literal("\u00a7c\u53ea\u80fd\u6536\u670d\u751f\u7269\uff01"));
            return InteractionResult.FAIL;
        }

        if (getCapturedCount(stack) >= CONTAINER_SIZE) {
            player.sendSystemMessage(Component.literal("\u00a7c\u8ddf\u968f\u4e4b\u6756\u5df2\u6ee1\uff01\u8bf7\u5148\u91ca\u653e\u4e00\u4e9b\u751f\u7269\u3002"));
            return InteractionResult.FAIL;
        }

        if (!hasEnoughCaptureHunger(player)) {
            player.sendSystemMessage(Component.literal("\u00a7c\u9965\u997f\u503c\u4e0d\u8db3\uff0c\u6536\u670d\u751f\u7269\u9700\u8981\u81f3\u5c11 " + CAPTURE_HUNGER_COST + " \u70b9\u9965\u997f\u503c\u3002"));
            return InteractionResult.FAIL;
        }

        CompoundTag captureTag = createCaptureTag(mob, player.getUUID());
        ItemStack wandStack = player.getItemInHand(hand);
        if (addToWand(wandStack, captureTag)) {
            String mobName = mob.hasCustomName() ? mob.getCustomName().getString() : mob.getName().getString();
            consumeCaptureHunger(player);
            mob.discard();
            player.sendSystemMessage(Component.literal("\u00a7a\u6210\u529f\u6536\u670d: \u00a7f" + mobName));
            return InteractionResult.SUCCESS;
        }

        player.sendSystemMessage(Component.literal("\u00a7c\u8ddf\u968f\u4e4b\u6756\u5b58\u50a8\u5931\u8d25\uff01"));
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && !level.isClientSide) {
            openWandGui(player, hand);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    private void openWandGui(Player player, InteractionHand hand) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkHandler.openFollowerWand(serverPlayer, hand);
        }
    }

    private static CompoundTag createCaptureTag(Mob mob, UUID ownerUUID) {
        CompoundTag captureTag = new CompoundTag();
        captureTag.putUUID(TAG_OWNER_UUID, ownerUUID);
        captureTag.putString(TAG_ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString());

        CompoundTag entityData = new CompoundTag();
        mob.saveWithoutId(entityData);
        sanitizeCapturedEntityData(entityData);
        captureTag.put(TAG_ENTITY_DATA, entityData);
        captureTag.putFloat("Health", mob.getHealth());
        captureTag.putFloat("MaxHealth", mob.getMaxHealth());

        if (mob.hasCustomName()) {
            captureTag.putString(TAG_CUSTOM_NAME, mob.getCustomName().getString());
        }

        return captureTag;
    }

    private static void sanitizeCapturedEntityData(CompoundTag entityData) {
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
    }

    public static int getCapturedCount(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_CAPTURED_MOBS)) {
            return tag.getList(TAG_CAPTURED_MOBS, 10).size();
        }
        return 0;
    }

    public static boolean addToWand(ItemStack wand, CompoundTag captureTag) {
        CompoundTag tag = wand.getOrCreateTag();
        ListTag list = tag.getList(TAG_CAPTURED_MOBS, 10);

        if (list.size() >= CONTAINER_SIZE) {
            return false;
        }

        list.add(captureTag);
        tag.put(TAG_CAPTURED_MOBS, list);
        return true;
    }

    private static boolean hasEnoughCaptureHunger(Player player) {
        return player.getAbilities().instabuild || player.getFoodData().getFoodLevel() >= CAPTURE_HUNGER_COST;
    }

    private static void consumeCaptureHunger(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }

        player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - CAPTURE_HUNGER_COST));
    }

    public static SimpleContainer getContainer(ItemStack wand) {
        SimpleContainer container = new SimpleContainer(CONTAINER_SIZE);
        CompoundTag tag = wand.getOrCreateTag();

        if (tag.contains(TAG_CAPTURED_MOBS)) {
            ListTag list = tag.getList(TAG_CAPTURED_MOBS, 10);
            for (int i = 0; i < list.size() && i < CONTAINER_SIZE; i++) {
                CompoundTag captureTag = list.getCompound(i);
                String entityTypeKey = captureTag.getString(TAG_ENTITY_TYPE);
                String customName = captureTag.contains(TAG_CUSTOM_NAME) ? captureTag.getString(TAG_CUSTOM_NAME) : null;

                ItemStack displayStack = createDisplayStack(entityTypeKey, customName, captureTag);
                container.setItem(i, displayStack);
            }
        }

        return container;
    }

    private static ItemStack createDisplayStack(String entityTypeKey, String customName, CompoundTag captureTag) {
        ItemStack stack = new ItemStack(Life_contract.CREATURE_EGG.get());

        CompoundTag itemTag = new CompoundTag();
        if (captureTag.hasUUID(TAG_OWNER_UUID)) {
            itemTag.putUUID(TAG_OWNER_UUID, captureTag.getUUID(TAG_OWNER_UUID));
        }
        itemTag.putString(CreatureEggItem.TAG_ENTITY_TYPE, entityTypeKey);
        if (captureTag.contains(TAG_ENTITY_DATA)) {
            itemTag.put(CreatureEggItem.TAG_ENTITY_DATA, captureTag.getCompound(TAG_ENTITY_DATA));
        }
        if (customName != null) {
            itemTag.putString(CreatureEggItem.TAG_CUSTOM_NAME, customName);
        }

        if (captureTag.contains("Health")) {
            itemTag.putFloat(CreatureEggItem.TAG_CAPTURED_HEALTH, captureTag.getFloat("Health"));
        }
        if (captureTag.contains("MaxHealth")) {
            itemTag.putFloat(CreatureEggItem.TAG_CAPTURED_MAX_HEALTH, captureTag.getFloat("MaxHealth"));
        } else {
            copyMaxHealthFromAttributes(captureTag, itemTag);
        }

        stack.setTag(itemTag);
        return stack;
    }

    private static void copyMaxHealthFromAttributes(CompoundTag captureTag, CompoundTag itemTag) {
        CompoundTag entityData = captureTag.getCompound(TAG_ENTITY_DATA);
        if (!entityData.contains("Attributes")) {
            return;
        }

        ListTag attributes = entityData.getList("Attributes", 10);
        for (int i = 0; i < attributes.size(); i++) {
            CompoundTag attr = attributes.getCompound(i);
            if ("minecraft:generic.max_health".equals(attr.getString("Name")) && attr.contains("Base")) {
                itemTag.putFloat(CreatureEggItem.TAG_CAPTURED_MAX_HEALTH, attr.getFloat("Base"));
                break;
            }
        }
    }

    public static void saveContainer(ItemStack wand, SimpleContainer container) {
        CompoundTag tag = wand.getOrCreateTag();
        ListTag list = new ListTag();

        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CreatureEggItem) {
                CompoundTag captureTag = itemStackToCaptureTag(stack);
                if (!captureTag.isEmpty()) {
                    list.add(captureTag);
                }
            }
        }

        tag.put(TAG_CAPTURED_MOBS, list);
    }

    private static CompoundTag itemStackToCaptureTag(ItemStack stack) {
        CompoundTag captureTag = new CompoundTag();
        if (!stack.hasTag() || stack.getTag() == null) {
            return captureTag;
        }

        CompoundTag itemTag = stack.getTag();
        if (itemTag.hasUUID(TAG_OWNER_UUID)) {
            captureTag.putUUID(TAG_OWNER_UUID, itemTag.getUUID(TAG_OWNER_UUID));
        }

        String entityType = CreatureEggItem.getEntityType(stack);
        if (entityType != null) {
            captureTag.putString(TAG_ENTITY_TYPE, entityType);
        }

        CompoundTag entityData = CreatureEggItem.getEntityData(stack);
        if (!entityData.isEmpty()) {
            captureTag.put(TAG_ENTITY_DATA, entityData);
        }

        String customName = CreatureEggItem.getCustomName(stack);
        if (customName != null) {
            captureTag.putString(TAG_CUSTOM_NAME, customName);
        }

        float health = CreatureEggItem.getCapturedHealth(stack);
        if (health > 0) {
            captureTag.putFloat("Health", health);
        }

        float maxHealth = CreatureEggItem.getCapturedMaxHealth(stack);
        if (maxHealth > 0) {
            captureTag.putFloat("MaxHealth", maxHealth);
        }

        return captureTag;
    }

    public static boolean isFollowerItem(ItemStack stack) {
        return stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_ENTITY_TYPE);
    }

    public static UUID getOwnerUUID(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_OWNER_UUID)) {
            return stack.getTag().getUUID(TAG_OWNER_UUID);
        }
        return null;
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
}
