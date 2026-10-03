package org.alku.life_contract.accessory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.alku.life_contract.Life_contract;

import java.util.EnumMap;
import java.util.Map;

/**
 * 玩家自带的饰品栏：每个佩戴类别一个槽位，存放在玩家持久化数据里。
 * <p>
 * <b>饰品必须装进饰品栏才会生效</b>：本槽位与 Curios 槽位都算（见 {@link AccessoryEffects}）。
 * 没有安装 Curios 时玩家可以用本饰品栏，装了 Curios 则两种槽位都能用。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AccessorySlots {

    private static final String TAG_SLOTS = "LifeContractAccessorySlots";

    private AccessorySlots() {
    }

    /** 读取某个类别的已装备饰品；空槽返回 {@link ItemStack#EMPTY}。 */
    public static ItemStack get(Player player, AccessoryCategory category) {
        CompoundTag slots = player.getPersistentData().getCompound(TAG_SLOTS);
        if (!slots.contains(category.id())) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parse(player.level().registryAccess(), slots.getCompound(category.id()))
                .orElse(ItemStack.EMPTY);
    }

    public static Map<AccessoryCategory, ItemStack> all(Player player) {
        Map<AccessoryCategory, ItemStack> result = new EnumMap<>(AccessoryCategory.class);
        for (AccessoryCategory category : AccessoryCategory.values()) {
            if (!category.isEquippable()) {
                continue;
            }
            ItemStack stack = get(player, category);
            if (!stack.isEmpty()) {
                result.put(category, stack);
            }
        }
        return result;
    }

    public static void set(Player player, AccessoryCategory category, ItemStack stack) {
        CompoundTag data = player.getPersistentData();
        CompoundTag slots = data.getCompound(TAG_SLOTS).copy();
        if (stack == null || stack.isEmpty()) {
            slots.remove(category.id());
        } else {
            slots.put(category.id(), stack.copyWithCount(1).save(player.level().registryAccess()));
        }
        data.put(TAG_SLOTS, slots);
    }

    /**
     * 把一件饰品装进对应类别的槽位。
     *
     * @return 被替换下来的旧饰品（可能为空），调用方负责归还给玩家
     */
    public static ItemStack equip(Player player, ItemStack stack) {
        AccessoryDefinition definition = AccessoryCatalog.of(stack);
        if (definition == null || !definition.isEquippable()) {
            return ItemStack.EMPTY;
        }
        ItemStack previous = get(player, definition.category());
        set(player, definition.category(), stack);
        return previous;
    }

    /** 卸下某个类别的饰品并尝试放回背包；背包满则掉落在脚下。 */
    public static ItemStack unequip(Player player, AccessoryCategory category) {
        ItemStack equipped = get(player, category);
        if (equipped.isEmpty()) {
            return ItemStack.EMPTY;
        }
        set(player, category, ItemStack.EMPTY);
        if (!player.getInventory().add(equipped)) {
            player.drop(equipped, false);
        }
        player.getInventory().setChanged();
        return equipped;
    }

    /** 把饰品栏内容写进聊天栏文本（指令与界面共用）。 */
    public static String describe(Player player, AccessoryCategory category) {
        ItemStack equipped = get(player, category);
        return equipped.isEmpty() ? "-" : equipped.getHoverName().getString();
    }

    /** 死亡重生后把饰品栏一起带过去。 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        CompoundTag original = event.getOriginal().getPersistentData();
        if (original.contains(TAG_SLOTS)) {
            event.getEntity().getPersistentData().put(TAG_SLOTS, original.getCompound(TAG_SLOTS).copy());
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Life_contract.LOGGER.debug("[饰品] 已继承饰品栏：{}", serverPlayer.getName().getString());
        }
    }
}