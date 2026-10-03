package org.alku.life_contract.accessory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import org.alku.life_contract.Life_contract;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Curios 兼容层：装了 Curios 时，佩戴在饰品栏里的饰品同样生效。
 * <p>
 * 全程反射调用，因此<b>不产生编译期依赖</b>：没装 Curios 时本类不会被使用，
 * 物品仍然可以放在背包里生效（见 {@link AccessoryEffects}）。
 */
public final class CuriosCompat {

    private static final String API_CLASS = "top.theillusivec4.curios.api.CuriosApi";
    private static final String HANDLER_INTERFACE =
            "top.theillusivec4.curios.api.type.capability.ICuriosItemHandler";

    private static boolean resolved;
    private static Method getCuriosInventory;
    private static Method getEquippedCurios;

    private CuriosCompat() {
    }

    public static boolean isAvailable() {
        return ModList.get().isLoaded("curios");
    }

    /** 玩家已佩戴的 Curios 物品；未装 Curios 或反射失败时返回空列表。 */
    public static List<ItemStack> equippedStacks(Player player) {
        if (!isAvailable() || !resolve()) {
            return List.of();
        }

        try {
            Object optional = getCuriosInventory.invoke(null, (LivingEntity) player);
            if (!(optional instanceof Optional<?> present) || present.isEmpty()) {
                return List.of();
            }

            Object equipped = getEquippedCurios.invoke(present.get());
            if (!(equipped instanceof IItemHandler handler)) {
                return List.of();
            }

            List<ItemStack> stacks = new ArrayList<>();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return stacks;
        } catch (Throwable throwable) {
            return List.of();
        }
    }

    private static boolean resolve() {
        if (resolved) {
            return getCuriosInventory != null && getEquippedCurios != null;
        }
        resolved = true;
        try {
            Class<?> api = Class.forName(API_CLASS);
            Class<?> handlerInterface = Class.forName(HANDLER_INTERFACE);
            getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
            getEquippedCurios = handlerInterface.getMethod("getEquippedCurios");
        } catch (Throwable throwable) {
            Life_contract.LOGGER.warn("[饰品] Curios 已加载但反射其 API 失败，本次只按背包判定", throwable);
            getCuriosInventory = null;
            getEquippedCurios = null;
        }
        return getCuriosInventory != null && getEquippedCurios != null;
    }
}