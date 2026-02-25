package org.alku.life_contract;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import org.alku.life_contract.death_venger.PacketSyncMarkedTarget;
import org.alku.life_contract.follower.FollowerWandMenu;
import org.alku.life_contract.revive.PacketReviveTeammate;
import org.alku.life_contract.revive.PacketSkipRevive;
import org.alku.life_contract.revive.PacketSyncDeadTeammates;
import org.alku.life_contract.follower.PacketOpenFollowerWand;
import org.alku.life_contract.follower.PacketSyncFollower;
import org.alku.life_contract.follower.PacketSyncFollowerHunger;
import org.alku.life_contract.healer.PacketHealerActiveHeal;
import org.alku.life_contract.healer.PacketSyncHealerCooldown;
import org.alku.life_contract.mineral_generator.MineralGeneratorBlockEntity;
import org.alku.life_contract.mineral_generator.MineralGeneratorMenu;
import org.alku.life_contract.mineral_generator.PacketGetMineralGenerator;
import org.alku.life_contract.mineral_generator.PacketRemoveMineralGenerator;
import org.alku.life_contract.mineral_generator.PacketSetMineralGenerator;
import org.alku.life_contract.mineral_generator.PacketSyncMineralGenerationState;
import org.alku.life_contract.mineral_generator.PacketSyncMineralGenerator;
import org.alku.life_contract.mount.PacketMountCommand;
import org.alku.life_contract.mount.PacketMountMovement;
import org.alku.life_contract.mount.PacketSyncMount;
import org.alku.life_contract.profession.PacketOpenProfessionMenu;
import org.alku.life_contract.profession.PacketSelectProfession;
import org.alku.life_contract.profession.PacketSyncLockedProfessions;
import org.alku.life_contract.profession.PacketSyncProfessions;
import org.alku.life_contract.profession.PacketSyncUnlockedProfessions;
import org.alku.life_contract.profession.PacketUnlockProfession;
import org.alku.life_contract.profession.ProfessionConfig;
import org.alku.life_contract.jungle_ape_god.PacketJungleApeSkill;
import org.alku.life_contract.jungle_ape_god.PacketSyncJungleApeState;

import java.util.Set;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("deprecation")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Life_contract.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++,
                PacketSyncContract.class,
                PacketSyncContract::encode,
                PacketSyncContract::new,
                PacketSyncContract::handle
        );
        CHANNEL.registerMessage(id++,
                PacketOpenTeamInventory.class,
                PacketOpenTeamInventory::encode,
                PacketOpenTeamInventory::new,
                PacketOpenTeamInventory::handle
        );
        CHANNEL.registerMessage(id++,
                PacketOpenShop.class,
                PacketOpenShop::toBytes,
                PacketOpenShop::new,
                PacketOpenShop::handle
        );
        CHANNEL.registerMessage(id++,
                PacketPurchaseItem.class,
                PacketPurchaseItem::toBytes,
                PacketPurchaseItem::new,
                PacketPurchaseItem::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSaveTrade.class,
                PacketSaveTrade::toBytes,
                PacketSaveTrade::new,
                PacketSaveTrade::handle
        );
        CHANNEL.registerMessage(id++,
                PacketBuyTrade.class,
                PacketBuyTrade::toBytes,
                PacketBuyTrade::new,
                PacketBuyTrade::handle
        );
        CHANNEL.registerMessage(id++,
                PacketRemoveTrade.class,
                PacketRemoveTrade::toBytes,
                PacketRemoveTrade::new,
                PacketRemoveTrade::handle
        );
        CHANNEL.registerMessage(id++,
                PacketOpenTradeShop.class,
                PacketOpenTradeShop::toBytes,
                PacketOpenTradeShop::new,
                PacketOpenTradeShop::handle
        );
        CHANNEL.registerMessage(id++,
                PacketGetMineralGenerator.class,
                PacketGetMineralGenerator::toBytes,
                PacketGetMineralGenerator::new,
                PacketGetMineralGenerator::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSetMineralGenerator.class,
                PacketSetMineralGenerator::toBytes,
                PacketSetMineralGenerator::new,
                PacketSetMineralGenerator::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncMineralGenerator.class,
                PacketSyncMineralGenerator::toBytes,
                PacketSyncMineralGenerator::new,
                PacketSyncMineralGenerator::handle
        );
        CHANNEL.registerMessage(id++,
                PacketRemoveMineralGenerator.class,
                PacketRemoveMineralGenerator::toBytes,
                PacketRemoveMineralGenerator::decode,
                PacketRemoveMineralGenerator::handle
        );
        CHANNEL.registerMessage(id++,
                PacketOpenFollowerWand.class,
                PacketOpenFollowerWand::encode,
                PacketOpenFollowerWand::new,
                PacketOpenFollowerWand::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncFollower.class,
                PacketSyncFollower::encode,
                PacketSyncFollower::new,
                PacketSyncFollower::handle
        );
        CHANNEL.registerMessage(id++,
                PacketOpenProfessionMenu.class,
                PacketOpenProfessionMenu::encode,
                PacketOpenProfessionMenu::decode,
                PacketOpenProfessionMenu::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSelectProfession.class,
                PacketSelectProfession::encode,
                PacketSelectProfession::decode,
                PacketSelectProfession::handle
        );
        CHANNEL.registerMessage(id++,
                PacketUnlockProfession.class,
                PacketUnlockProfession::encode,
                PacketUnlockProfession::decode,
                PacketUnlockProfession::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncMineralGenerationState.class,
                PacketSyncMineralGenerationState::toBytes,
                PacketSyncMineralGenerationState::new,
                PacketSyncMineralGenerationState::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncMount.class,
                PacketSyncMount::encode,
                PacketSyncMount::decode,
                PacketSyncMount::handle
        );
        CHANNEL.registerMessage(id++,
                PacketMountCommand.class,
                PacketMountCommand::encode,
                PacketMountCommand::decode,
                PacketMountCommand::handle
        );
        CHANNEL.registerMessage(id++,
                PacketMountMovement.class,
                PacketMountMovement::encode,
                PacketMountMovement::decode,
                PacketMountMovement::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncForgetterState.class,
                PacketSyncForgetterState::encode,
                PacketSyncForgetterState::new,
                PacketSyncForgetterState::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncFollowerHunger.class,
                PacketSyncFollowerHunger::encode,
                PacketSyncFollowerHunger::new,
                PacketSyncFollowerHunger::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncImpostorState.class,
                PacketSyncImpostorState::encode,
                PacketSyncImpostorState::decode,
                PacketSyncImpostorState::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncMarkedTarget.class,
                PacketSyncMarkedTarget::encode,
                PacketSyncMarkedTarget::decode,
                PacketSyncMarkedTarget::handle
        );
        CHANNEL.registerMessage(id++,
                PacketHealerActiveHeal.class,
                PacketHealerActiveHeal::encode,
                PacketHealerActiveHeal::decode,
                PacketHealerActiveHeal::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncHealerCooldown.class,
                PacketSyncHealerCooldown::encode,
                PacketSyncHealerCooldown::decode,
                PacketSyncHealerCooldown::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncUnlockedProfessions.class,
                PacketSyncUnlockedProfessions::encode,
                PacketSyncUnlockedProfessions::decode,
                PacketSyncUnlockedProfessions::handle
        );
        CHANNEL.registerMessage(id++,
                PacketFoolStealProfession.class,
                PacketFoolStealProfession::encode,
                PacketFoolStealProfession::decode,
                PacketFoolStealProfession::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncProfessions.class,
                PacketSyncProfessions::encode,
                PacketSyncProfessions::decode,
                PacketSyncProfessions::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncLockedProfessions.class,
                PacketSyncLockedProfessions::encode,
                PacketSyncLockedProfessions::decode,
                PacketSyncLockedProfessions::handle
        );
        CHANNEL.registerMessage(id++,
                PacketReviveTeammate.class,
                PacketReviveTeammate::encode,
                PacketReviveTeammate::decode,
                PacketReviveTeammate::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSkipRevive.class,
                PacketSkipRevive::encode,
                PacketSkipRevive::decode,
                PacketSkipRevive::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncDeadTeammates.class,
                PacketSyncDeadTeammates::encode,
                PacketSyncDeadTeammates::decode,
                PacketSyncDeadTeammates::handle
        );
        CHANNEL.registerMessage(id++,
                PacketJungleApeSkill.class,
                PacketJungleApeSkill::encode,
                PacketJungleApeSkill::new,
                PacketJungleApeSkill::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncJungleApeState.class,
                PacketSyncJungleApeState::encode,
                PacketSyncJungleApeState::new,
                PacketSyncJungleApeState::handle
        );
    }

    public static void sendRemoveTradePacket(int slot) {
        CHANNEL.sendToServer(new PacketRemoveTrade(slot));
    }

    public static void sendOpenTradeShopPacket(boolean isRemoveMode) {
        CHANNEL.sendToServer(new PacketOpenTradeShop(isRemoveMode));
    }

    public static void openTradeShop(ServerPlayer player, boolean isRemoveMode) {
        if (isRemoveMode) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketOpenTradeShop(true));
        }
        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new TradeShopMenu(windowId, inv),
                Component.literal(isRemoveMode ? "删除交易" : "商店")
        ));
    }

    public static void sendGetMineralGeneratorPacket(net.minecraft.core.BlockPos pos) {
        CHANNEL.sendToServer(new PacketGetMineralGenerator(pos));
    }

    public static void sendSetMineralGeneratorPacket(net.minecraft.core.BlockPos pos, String mineralType, int interval, boolean enabled) {
        CHANNEL.sendToServer(new PacketSetMineralGenerator(pos, mineralType, interval, enabled));
    }
    
    public static void sendSyncMineralGenerator(net.minecraft.core.BlockPos pos, String mineralType, int interval, boolean enabled, long lastTick, long serverTick) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSyncMineralGenerator(pos, mineralType, interval, enabled, lastTick, serverTick));
    }

    public static void sendRemoveMineralGenerator(net.minecraft.core.BlockPos pos) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketRemoveMineralGenerator(pos));
    }

    public static void openMineralGenerator(ServerPlayer player, net.minecraft.core.BlockPos pos) {
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof MineralGeneratorBlockEntity generator) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new PacketSyncMineralGenerator(pos, generator.getMineralType().name(),
                            generator.getInterval(), generator.isEnabled(), generator.getLastTick(), player.level().getGameTime()));
        }
        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new MineralGeneratorMenu(windowId, inv, pos),
                Component.literal("矿物生成器")
        ));
    }

    public static void sendOpenTeamInventoryPacket() {
        CHANNEL.sendToServer(new PacketOpenTeamInventory());
    }

    public static void sendPurchasePacket(int slot) {
        CHANNEL.sendToServer(new PacketPurchaseItem(slot));
    }

    public static void sendSaveTradePacket(int expLevels) {
        CHANNEL.sendToServer(new PacketSaveTrade(expLevels));
    }

    public static void sendBuyTradePacket(int slot) {
        CHANNEL.sendToServer(new PacketBuyTrade(slot));
    }

    public static void openShop(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new ShopMenu(windowId, inv),
                Component.literal("商店")
        ));
    }

    public static void openTradeSetup(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new TradeSetupMenu(windowId, inv),
                Component.literal("添加交易")
        ));
    }

    public static void openTradeShop(ServerPlayer player) {
        openTradeShop(player, false);
    }

    public static void handlePurchase(ServerPlayer player, int slot) {
        ShopItem shopItem = ShopConfig.getShopItem(slot);
        if (shopItem == null || shopItem.getResult().isEmpty() || shopItem.getPrice() <= 0) {
            player.sendSystemMessage(Component.literal("§c这个物品无法购买！"));
            return;
        }

        ItemStack currency = shopItem.getCurrency();
        int price = shopItem.getPrice();
        int hasCurrency = countItems(player, currency);

        if (hasCurrency < price) {
            player.sendSystemMessage(Component.literal("§c金币不足！需要 " + price + " " + currency.getHoverName().getString()));
            return;
        }

        removeItems(player, currency, price);
        giveItem(player, shopItem.getResult());
        player.sendSystemMessage(Component.literal("§a购买成功！"));
    }

    private static int countItems(ServerPlayer player, ItemStack itemStack) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == itemStack.getItem()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItems(ServerPlayer player, ItemStack itemStack, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == itemStack.getItem()) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private static void giveItem(ServerPlayer player, ItemStack itemStack) {
        if (!player.addItem(itemStack.copy())) {
            player.drop(itemStack.copy(), false);
        }
    }

    public static void openFollowerWand(ServerPlayer player, InteractionHand hand) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new FollowerWandMenu(windowId, inv, hand),
                Component.literal("跟随之杖")
        ));
    }

    public static void openProfessionMenu(ServerPlayer player) {
        syncProfessions(player);
        syncUnlockedProfessions(player);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketOpenProfessionMenu());
    }

    public static void sendSelectProfessionPacket(String professionId) {
        CHANNEL.sendToServer(new PacketSelectProfession(professionId));
    }

    public static void sendUnlockProfessionPacket(String professionId, String password) {
        CHANNEL.sendToServer(new PacketUnlockProfession(professionId, password));
    }

    public static void sendOpenProfessionMenuPacket() {
        CHANNEL.sendToServer(new PacketOpenProfessionMenu());
    }

    public static void sendHealerActiveHealPacket() {
        CHANNEL.sendToServer(new PacketHealerActiveHeal());
    }

    public static void syncUnlockedProfessions(ServerPlayer player) {
        Set<String> unlocked = ProfessionConfig.getPlayerUnlockedProfessions(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncUnlockedProfessions(unlocked));
    }

    public static void syncLockedProfessions(ServerPlayer player) {
        Set<String> locked = ProfessionConfig.getLockedProfessions();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncLockedProfessions(locked));
    }

    public static void syncProfessions(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncProfessions(ProfessionConfig.getProfessions()));
    }

    public static void sendReviveTeammatePacket(java.util.UUID teammateUUID) {
        CHANNEL.sendToServer(new PacketReviveTeammate(teammateUUID));
    }

    public static void sendSkipRevivePacket() {
        CHANNEL.sendToServer(new PacketSkipRevive());
    }

    public static void sendJungleApeSkillPacket(int skillId) {
        CHANNEL.sendToServer(new PacketJungleApeSkill(skillId));
    }

    public static void sendJungleApeSkillPacket(int skillId, int targetId) {
        CHANNEL.sendToServer(new PacketJungleApeSkill(skillId, targetId));
    }
}