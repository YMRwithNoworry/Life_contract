package org.alku.life_contract;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosIntegration {
    
    public static boolean hasEquippedItem(Player player, Class<?> itemClass) {
        return getEquippedItem(player, itemClass).isPresent();
    }
    
    public static Optional<ItemStack> getEquippedItem(Player player, Class<?> itemClass) {
        return CuriosApi.getCuriosHelper()
                .getCuriosHandler(player)
                .resolve()
                .flatMap(handler -> handler.getStacksHandler("curio"))
                .map(ICurioStacksHandler::getStacks)
                .flatMap(stackHandler -> {
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (itemClass.isInstance(stack.getItem())) {
                            return Optional.of(stack);
                        }
                    }
                    return Optional.empty();
                });
    }
    
    public static Optional<Integer> findFirstEmptySlot(Player player, String slot) {
        return CuriosApi.getCuriosHelper()
                .getCuriosHandler(player)
                .resolve()
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(ICurioStacksHandler::getStacks)
                .flatMap(stackHandler -> {
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        if (stackHandler.getStackInSlot(i).isEmpty()) {
                            return Optional.of(i);
                        }
                    }
                    return Optional.empty();
                });
    }
    
    public static boolean equipItem(Player player, ItemStack item, String slot) {
        Optional<ICuriosItemHandler> handlerOpt = CuriosApi.getCuriosHelper()
                .getCuriosHandler(player)
                .resolve();
        
        if (handlerOpt.isEmpty()) {
            return false;
        }
        
        ICuriosItemHandler handler = handlerOpt.get();
        Optional<ICurioStacksHandler> stacksHandlerOpt = handler.getStacksHandler(slot);
        
        if (stacksHandlerOpt.isEmpty()) {
            return false;
        }
        
        IDynamicStackHandler stackHandler = stacksHandlerOpt.get().getStacks();
        
        for (int i = 0; i < stackHandler.getSlots(); i++) {
            if (stackHandler.getStackInSlot(i).isEmpty()) {
                stackHandler.setStackInSlot(i, item);
                return true;
            }
        }
        
        return false;
    }
    
    public static void init() {
    }
}
