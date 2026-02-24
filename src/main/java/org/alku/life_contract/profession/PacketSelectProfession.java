package org.alku.life_contract.profession;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ContractEvents;

import java.util.function.Supplier;

public class PacketSelectProfession {
    private final String professionId;

    public PacketSelectProfession(String professionId) {
        this.professionId = professionId;
    }

    public static void encode(PacketSelectProfession msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.professionId);
    }

    public static PacketSelectProfession decode(FriendlyByteBuf buffer) {
        return new PacketSelectProfession(buffer.readUtf());
    }

    public static void handle(PacketSelectProfession msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Profession profession = ProfessionConfig.getProfession(msg.professionId);
                if (profession != null) {
                    if (ProfessionConfig.isProfessionLocked(msg.professionId)) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§c[生灵契约] 该职业已被管理员锁定，无法选择！"));
                        return;
                    }
                    boolean unlocked = ProfessionConfig.isProfessionUnlocked(player.getUUID(), msg.professionId);
                    if (!profession.requiresPassword() || unlocked) {
                        ProfessionConfig.setPlayerProfession(player.getUUID(), msg.professionId);
                        player.getPersistentData().putString("LifeContractProfession", msg.professionId);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§a[生灵契约] §f你选择了职业: §e" + profession.getName()));
                        
                        if (profession.hasDiceAbility()) {
                            giveGamblerDice(player);
                        }
                        
                        if (profession.hasDonkBowAbility()) {
                            giveDonkBow(player);
                        }
                        
                        ContractEvents.syncData(player);
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§c[生灵契约] 该职业尚未解锁！"));
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
    
    private static void giveGamblerDice(ServerPlayer player) {
        if (!hasGamblerDice(player)) {
            ItemStack diceStack = new ItemStack(Life_contract.GAMBLER_DICE.get());
            if (!player.getInventory().add(diceStack)) {
                player.drop(diceStack, false);
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§d[赌徒] §f你获得了专属的 §e赌徒骰子§f！"));
        }
    }
    
    private static void giveDonkBow(ServerPlayer player) {
        if (!hasDonkBow(player)) {
            ItemStack bowStack = new ItemStack(Life_contract.SEALED_BOW.get());
            if (!player.getInventory().add(bowStack)) {
                player.drop(bowStack, false);
            }
            
            ItemStack arrowStack = new ItemStack(net.minecraft.world.item.Items.ARROW, 8);
            if (!player.getInventory().add(arrowStack)) {
                player.drop(arrowStack, false);
            }
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§d[donk] §f你获得了专属的 §e尘封的弓 §f和 §e8根箭§f！"));
        }
    }
    
    public static boolean hasGamblerDice(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Life_contract.GAMBLER_DICE.get()) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasDonkBow(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Life_contract.SEALED_BOW.get()) {
                return true;
            }
        }
        return false;
    }
}
