package org.alku.life_contract;

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

import java.util.List;
import java.util.UUID;

public class FollowerWandItem extends Item {

    public static final String TAG_CAPTURED_MOBS = "CapturedMobs";
    public static final String TAG_OWNER_UUID = "OwnerUUID";
    public static final String TAG_ENTITY_TYPE = "EntityType";
    public static final String TAG_ENTITY_DATA = "EntityData";
    public static final String TAG_CUSTOM_NAME = "CustomName";
    public static final int CONTAINER_SIZE = 9;

    public FollowerWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§d[跟随之杖]").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal("§e右键生物 §7- 收服生物"));
        components.add(Component.literal("§eShift+右键 §7- 打开存储界面"));
        components.add(Component.literal("§7已收服: §f" + getCapturedCount(stack) + "/9"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("跟随之杖").withStyle(ChatFormatting.LIGHT_PURPLE);
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
            player.sendSystemMessage(Component.literal("§c无法收服玩家！"));
            return InteractionResult.FAIL;
        }

        if (!(target instanceof Mob)) {
            player.sendSystemMessage(Component.literal("§c只能收服生物！"));
            return InteractionResult.FAIL;
        }

        if (getCapturedCount(stack) >= CONTAINER_SIZE) {
            player.sendSystemMessage(Component.literal("§c跟随之杖已满！请先释放一些生物。"));
            return InteractionResult.FAIL;
        }

        Mob mob = (Mob) target;
        
        CompoundTag captureTag = new CompoundTag();
        captureTag.putUUID(TAG_OWNER_UUID, player.getUUID());
        captureTag.putString(TAG_ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString());
        
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
        captureTag.put(TAG_ENTITY_DATA, entityData);
        
        captureTag.putFloat("Health", mob.getHealth());
        
        if (mob.hasCustomName()) {
            captureTag.putString(TAG_CUSTOM_NAME, mob.getCustomName().getString());
        }

        ItemStack wandStack = player.getItemInHand(hand);
        if (addToWand(wandStack, captureTag)) {
            String mobName = mob.hasCustomName() ? mob.getCustomName().getString() : mob.getName().getString();
            mob.discard();
            player.sendSystemMessage(Component.literal("§a成功收服: §f" + mobName));
            return InteractionResult.SUCCESS;
        } else {
            player.sendSystemMessage(Component.literal("§c跟随之杖存储失败！"));
            return InteractionResult.FAIL;
        }
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
        if (captureTag.contains("Attributes")) {
            net.minecraft.nbt.ListTag attributes = captureTag.getList("Attributes", 10);
            for (int i = 0; i < attributes.size(); i++) {
                CompoundTag attr = attributes.getCompound(i);
                if (attr.getString("Name").equals("minecraft:generic.max_health")) {
                    if (attr.contains("Base")) {
                        itemTag.putFloat(CreatureEggItem.TAG_CAPTURED_MAX_HEALTH, attr.getFloat("Base"));
                    }
                    break;
                }
            }
        }
        
        stack.setTag(itemTag);
        
        return stack;
    }

    public static void saveContainer(ItemStack wand, SimpleContainer container) {
        CompoundTag tag = wand.getOrCreateTag();
        ListTag list = new ListTag();
        
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CreatureEggItem) {
                CompoundTag captureTag = new CompoundTag();
                
                if (stack.hasTag() && stack.getTag() != null) {
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
                }
                
                if (!captureTag.isEmpty()) {
                    list.add(captureTag);
                }
            }
        }
        
        tag.put(TAG_CAPTURED_MOBS, list);
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
