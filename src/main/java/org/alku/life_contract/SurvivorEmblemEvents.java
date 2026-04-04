package org.alku.life_contract;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class SurvivorEmblemEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        
        if (event.getEntity() instanceof ServerPlayer victim) {
            if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                if (killer != victim) {
                    handlePlayerKill(killer, victim);
                }
            }
            
            dropEmblemOnDeath(victim);
        }
    }

    private static void handlePlayerKill(ServerPlayer killer, ServerPlayer victim) {
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper()
                .getCuriosHandler(killer).resolve();
        
        if (curiosHandler.isPresent()) {
            ICuriosItemHandler handler = curiosHandler.get();
            Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler("curio");
            
            if (stacksHandler.isPresent()) {
                IDynamicStackHandler stackHandler = stacksHandler.get().getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack.getItem() instanceof SurvivorEmblemItem) {
                        SurvivorEmblemItem.addKill(stack);
                        SurvivorEmblemItem emblem = (SurvivorEmblemItem) stack.getItem();
                        emblem.applyAttributes(killer, stack);
                        
                        int kills = SurvivorEmblemItem.getKills(stack);
                        killer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§6[幸存者徽记] §f击杀数: §e" + kills + " §7(+" + 
                                (kills * SurvivorEmblemItem.ATTACK_DAMAGE_PER_KILL) + " 攻击, +" +
                                (kills * SurvivorEmblemItem.MAX_HEALTH_PER_KILL) + " 生命, +" +
                                (kills * SurvivorEmblemItem.ARMOR_PER_KILL) + " 护甲)"));
                        return;
                    }
                }
            }
        }
        
        // 如果玩家没有徽记，创建一个新的
        ItemStack newEmblem = new ItemStack(Life_contract.SURVIVOR_EMBLEM.get());
        SurvivorEmblemItem.setKills(newEmblem, 1);
        
        boolean equipped = CuriosApi.getCuriosHelper().getCuriosHandler(killer)
                .resolve()
                .map(handler -> {
                    Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler("curio");
                    if (stacksHandler.isPresent()) {
                        IDynamicStackHandler stackHandler = stacksHandler.get().getStacks();
                        for (int i = 0; i < stackHandler.getSlots(); i++) {
                            if (stackHandler.getStackInSlot(i).isEmpty()) {
                                stackHandler.setStackInSlot(i, newEmblem);
                                return true;
                            }
                        }
                    }
                    return false;
                }).orElse(false);
        
        if (!equipped) {
            if (!killer.addItem(newEmblem)) {
                killer.drop(newEmblem, false);
            }
        }
        
        killer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6[幸存者徽记] §f获得幸存者徽记！击杀数: §e1"));
    }

    private static void dropEmblemOnDeath(ServerPlayer victim) {
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper()
                .getCuriosHandler(victim).resolve();
        
        if (curiosHandler.isPresent()) {
            ICuriosItemHandler handler = curiosHandler.get();
            Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler("curio");
            
            if (stacksHandler.isPresent()) {
                IDynamicStackHandler stackHandler = stacksHandler.get().getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack.getItem() instanceof SurvivorEmblemItem) {
                        ItemStack emblemDrop = stack.copy();
                        stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                        
                        net.minecraft.world.entity.item.ItemEntity itemEntity = 
                                new net.minecraft.world.entity.item.ItemEntity(
                                        victim.level(), 
                                        victim.getX(), 
                                        victim.getY(), 
                                        victim.getZ(), 
                                        emblemDrop);
                        victim.level().addFreshEntity(itemEntity);
                        
                        int kills = SurvivorEmblemItem.getKills(emblemDrop);
                        victim.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§c[幸存者徽记] §f你的徽记已掉落！击杀数: §e" + kills));
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerPickupItem(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity().level().isClientSide) return;
        
        Player player = event.getEntity();
        ItemStack stack = event.getStack();
        
        if (stack.getItem() instanceof SurvivorEmblemItem) {
            int kills = SurvivorEmblemItem.getKills(stack);
            if (kills > 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§6[幸存者徽记] §f拾取了一个徽记！击杀数: §e" + kills));
            }
        }
    }
}
