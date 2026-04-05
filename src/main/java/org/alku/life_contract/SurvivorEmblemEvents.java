package org.alku.life_contract;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        Optional<ItemStack> emblem = CuriosIntegration.getEquippedItem(killer, SurvivorEmblemItem.class);
        
        if (emblem.isPresent()) {
            ItemStack stack = emblem.get();
            SurvivorEmblemItem.addKill(stack);
            SurvivorEmblemItem emblemItem = (SurvivorEmblemItem) stack.getItem();
            emblemItem.applyAttributes(killer, stack);
            
            int kills = SurvivorEmblemItem.getKills(stack);
            killer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6[幸存者徽记] §f击杀数: §e" + kills + " §7(+" + 
                    (kills * SurvivorEmblemItem.ATTACK_DAMAGE_PER_KILL) + " 攻击, +" +
                    (kills * SurvivorEmblemItem.MAX_HEALTH_PER_KILL) + " 生命, +" +
                    (kills * SurvivorEmblemItem.ARMOR_PER_KILL) + " 护甲)"));
        } else {
            ItemStack newEmblem = new ItemStack(Life_contract.SURVIVOR_EMBLEM.get());
            SurvivorEmblemItem.setKills(newEmblem, 1);
            
            boolean equipped = CuriosIntegration.equipItem(killer, newEmblem, "curio");
            
            if (!equipped) {
                if (!killer.addItem(newEmblem)) {
                    killer.drop(newEmblem, false);
                }
            }
            
            killer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6[幸存者徽记] §f获得幸存者徽记！击杀数: §e1"));
        }
    }

    private static void dropEmblemOnDeath(ServerPlayer victim) {
        Optional<ItemStack> emblem = CuriosIntegration.getEquippedItem(victim, SurvivorEmblemItem.class);
        
        if (emblem.isPresent()) {
            ItemStack emblemDrop = emblem.get().copy();
            emblem.get().setCount(0);
            
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
