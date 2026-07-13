package org.alku.life_contract;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import org.alku.life_contract.revive.PacketReviveTeammate;
import org.alku.life_contract.revive.PacketSkipRevive;
import org.alku.life_contract.revive.PacketSyncDeadTeammates;
import org.alku.life_contract.follower.PacketSyncFollower;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";

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
                PacketSyncFollower.class,
                PacketSyncFollower::encode,
                PacketSyncFollower::new,
                PacketSyncFollower::handle
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
        CHANNEL.registerMessage(id++, org.alku.life_contract.mutation.MutationPackets.Open.class,
                org.alku.life_contract.mutation.MutationPackets.Open::encode,
                org.alku.life_contract.mutation.MutationPackets.Open::new,
                org.alku.life_contract.mutation.MutationPackets.Open::handle);
        CHANNEL.registerMessage(id++, org.alku.life_contract.mutation.MutationPackets.Upgrade.class,
                org.alku.life_contract.mutation.MutationPackets.Upgrade::encode,
                org.alku.life_contract.mutation.MutationPackets.Upgrade::new,
                org.alku.life_contract.mutation.MutationPackets.Upgrade::handle);
    }

    public static void sendOpenTeamInventoryPacket() {
        CHANNEL.sendToServer(new PacketOpenTeamInventory());
    }

    public static void sendReviveTeammatePacket(java.util.UUID teammateUUID) {
        CHANNEL.sendToServer(new PacketReviveTeammate(teammateUUID));
    }

    public static void sendSkipRevivePacket() {
        CHANNEL.sendToServer(new PacketSkipRevive());
    }
}
