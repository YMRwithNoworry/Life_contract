package org.alku.life_contract;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import org.alku.life_contract.follower.FollowerWandMenu;
import org.alku.life_contract.revive.PacketReviveTeammate;
import org.alku.life_contract.revive.PacketSkipRevive;
import org.alku.life_contract.revive.PacketSyncDeadTeammates;
import org.alku.life_contract.follower.PacketOpenFollowerWand;
import org.alku.life_contract.follower.PacketSyncFollower;
import org.alku.life_contract.follower.PacketSyncFollowerHunger;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("deprecation")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "main"),
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
                PacketSyncFollowerHunger.class,
                PacketSyncFollowerHunger::encode,
                PacketSyncFollowerHunger::new,
                PacketSyncFollowerHunger::handle
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
                PacketSyncTeamInventory.class,
                PacketSyncTeamInventory::encode,
                PacketSyncTeamInventory::new,
                PacketSyncTeamInventory::handle
        );
        CHANNEL.registerMessage(id++,
                PacketSyncLifePoints.class,
                PacketSyncLifePoints::encode,
                PacketSyncLifePoints::new,
                PacketSyncLifePoints::handle
        );
    }

    public static void sendOpenTeamInventoryPacket() {
        CHANNEL.sendToServer(new PacketOpenTeamInventory());
    }

    public static void openFollowerWand(ServerPlayer player, InteractionHand hand) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new FollowerWandMenu(windowId, inv, hand),
                Component.literal("跟随之杖")
        ));
    }

    public static void sendReviveTeammatePacket(java.util.UUID teammateUUID) {
        CHANNEL.sendToServer(new PacketReviveTeammate(teammateUUID));
    }

    public static void sendSkipRevivePacket() {
        CHANNEL.sendToServer(new PacketSkipRevive());
    }
}
