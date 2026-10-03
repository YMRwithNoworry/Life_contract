package org.alku.life_contract;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.alku.life_contract.follower.PacketSyncFollower;
import org.alku.life_contract.mutation.MutationPackets;
import org.alku.life_contract.revive.PacketReviveTeammate;
import org.alku.life_contract.revive.PacketSkipRevive;
import org.alku.life_contract.revive.PacketSyncDeadTeammates;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    private NetworkHandler() {
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, path));
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> codec(
            BiConsumer<RegistryFriendlyByteBuf, T> encoder,
            Function<RegistryFriendlyByteBuf, T> decoder) {
        return StreamCodec.of((buffer, value) -> encoder.accept(buffer, value), buffer -> decoder.apply(buffer));
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar payloads = event.registrar(PROTOCOL_VERSION);
        payloads.playToClient(PacketSyncContract.TYPE, PacketSyncContract.STREAM_CODEC, PacketSyncContract::handle);
        payloads.playToServer(PacketOpenTeamInventory.TYPE, PacketOpenTeamInventory.STREAM_CODEC, PacketOpenTeamInventory::handle);
        payloads.playToClient(PacketSyncFollower.TYPE, PacketSyncFollower.STREAM_CODEC, PacketSyncFollower::handle);
        payloads.playToServer(PacketReviveTeammate.TYPE, PacketReviveTeammate.STREAM_CODEC, PacketReviveTeammate::handle);
        payloads.playToServer(PacketSkipRevive.TYPE, PacketSkipRevive.STREAM_CODEC, PacketSkipRevive::handle);
        payloads.playToClient(PacketSyncDeadTeammates.TYPE, PacketSyncDeadTeammates.STREAM_CODEC, PacketSyncDeadTeammates::handle);
        payloads.playToClient(PacketSyncTeamInventory.TYPE, PacketSyncTeamInventory.STREAM_CODEC, PacketSyncTeamInventory::handle);
        payloads.playToClient(PacketSyncLifePoints.TYPE, PacketSyncLifePoints.STREAM_CODEC, PacketSyncLifePoints::handle);
        payloads.playToClient(
                org.alku.life_contract.border.BorderStatusPayload.TYPE,
                org.alku.life_contract.border.BorderStatusPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> org.alku.life_contract.client.BorderStatusHUD.update(payload)));
        payloads.playToClient(
                PacketSyncWaypoints.TYPE,
                PacketSyncWaypoints.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> org.alku.life_contract.client.EventHUD.updateWaypoints(payload)));
        payloads.playToClient(
                org.alku.life_contract.events.EventSyncPayload.TYPE,
                org.alku.life_contract.events.EventSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> org.alku.life_contract.client.EventHUD.update(payload)));
        payloads.playToServer(
                org.alku.life_contract.accessory.PacketUseAccessoryActive.TYPE,
                org.alku.life_contract.accessory.PacketUseAccessoryActive.STREAM_CODEC,
                org.alku.life_contract.accessory.PacketUseAccessoryActive::handle);
        payloads.playToServer(MutationPackets.Open.TYPE, MutationPackets.Open.STREAM_CODEC, MutationPackets.Open::handle);
        payloads.playToServer(MutationPackets.Upgrade.TYPE, MutationPackets.Upgrade.STREAM_CODEC, MutationPackets.Upgrade::handle);
        payloads.playToClient(
                org.alku.life_contract.airdrop.network.AirdropPayload.TYPE,
                org.alku.life_contract.airdrop.network.AirdropPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> org.alku.life_contract.airdrop.network.AirdropTrackClientHandler.setTarget(payload)));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sendToTrackingEntity(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendOpenTeamInventoryPacket() {
        sendToServer(new PacketOpenTeamInventory());
    }

    public static void sendReviveTeammatePacket(java.util.UUID teammateUUID) {
        sendToServer(new PacketReviveTeammate(teammateUUID));
    }

    public static void sendSkipRevivePacket() {
        sendToServer(new PacketSkipRevive());
    }
}
