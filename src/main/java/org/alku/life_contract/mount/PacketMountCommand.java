package org.alku.life_contract.mount;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.NetworkHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketMountCommand {
    public static final int COMMAND_DISMOUNT = 0;
    public static final int COMMAND_ATTACK_TARGET = 1;
    public static final int COMMAND_SPECIAL_ABILITY = 2;
    public static final int COMMAND_BEHAVIOR_FOLLOW = 3;
    public static final int COMMAND_BEHAVIOR_STAY = 4;
    public static final int COMMAND_BEHAVIOR_DEFEND = 5;
    public static final int COMMAND_BEHAVIOR_ATTACK = 6;
    public static final int COMMAND_MOUNT_NEAREST = 7;

    private final int commandType;
    private final UUID targetUUID;

    public PacketMountCommand(int commandType) {
        this(commandType, null);
    }

    public PacketMountCommand(int commandType, UUID targetUUID) {
        this.commandType = commandType;
        this.targetUUID = targetUUID;
    }

    public static void encode(PacketMountCommand packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.commandType);
        buffer.writeBoolean(packet.targetUUID != null);
        if (packet.targetUUID != null) {
            buffer.writeUUID(packet.targetUUID);
        }
    }

    public static PacketMountCommand decode(FriendlyByteBuf buffer) {
        int commandType = buffer.readInt();
        boolean hasTarget = buffer.readBoolean();
        UUID targetUUID = hasTarget ? buffer.readUUID() : null;
        return new PacketMountCommand(commandType, targetUUID);
    }

    public static void handle(PacketMountCommand packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (!BeastRiderMountSystem.isBeastRider(player)) return;

            switch (packet.commandType) {
                case COMMAND_DISMOUNT -> {
                    BeastRiderMountSystem.dismountCreature(player);
                }
                case COMMAND_ATTACK_TARGET -> {
                    if (packet.targetUUID != null) {
                        if (player.level() instanceof ServerLevel serverLevel) {
                            net.minecraft.world.entity.Entity entity = serverLevel.getEntity(packet.targetUUID);
                            if (entity instanceof LivingEntity livingEntity) {
                                BeastRiderMountSystem.commandAttack(player, livingEntity);
                            }
                        }
                    }
                }
                case COMMAND_SPECIAL_ABILITY -> {
                    BeastRiderMountSystem.commandSpecialAbility(player);
                }
                case COMMAND_BEHAVIOR_FOLLOW -> {
                    BeastRiderMountSystem.setMountBehavior(player, BeastRiderMountSystem.MountBehavior.FOLLOW);
                }
                case COMMAND_BEHAVIOR_STAY -> {
                    BeastRiderMountSystem.setMountBehavior(player, BeastRiderMountSystem.MountBehavior.STAY);
                }
                case COMMAND_BEHAVIOR_DEFEND -> {
                    BeastRiderMountSystem.setMountBehavior(player, BeastRiderMountSystem.MountBehavior.DEFEND);
                }
                case COMMAND_BEHAVIOR_ATTACK -> {
                    BeastRiderMountSystem.setMountBehavior(player, BeastRiderMountSystem.MountBehavior.ATTACK);
                }
                case COMMAND_MOUNT_NEAREST -> {
                    mountNearestFollower(player);
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static void mountNearestFollower(ServerPlayer player) {
        double nearestDist = Double.MAX_VALUE;
        Mob nearestMob = null;

        for (net.minecraft.world.entity.Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(5))) {
            if (entity instanceof Mob mob) {
                if (BeastRiderMountSystem.canMount(player, mob)) {
                    double dist = player.distanceToSqr(mob);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearestMob = mob;
                    }
                }
            }
        }

        if (nearestMob != null) {
            BeastRiderMountSystem.mountCreature(player, nearestMob);
        } else {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§c[驯兽师] §r附近没有可骑乘的己方生物"),
                true
            );
        }
    }

    public static void sendDismount() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_DISMOUNT));
    }

    public static void sendAttackTarget(UUID targetUUID) {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_ATTACK_TARGET, targetUUID));
    }

    public static void sendSpecialAbility() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_SPECIAL_ABILITY));
    }

    public static void sendBehaviorFollow() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_BEHAVIOR_FOLLOW));
    }

    public static void sendBehaviorStay() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_BEHAVIOR_STAY));
    }

    public static void sendBehaviorDefend() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_BEHAVIOR_DEFEND));
    }

    public static void sendBehaviorAttack() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_BEHAVIOR_ATTACK));
    }

    public static void sendMountNearest() {
        NetworkHandler.CHANNEL.sendToServer(new PacketMountCommand(COMMAND_MOUNT_NEAREST));
    }
}
