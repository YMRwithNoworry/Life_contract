package org.alku.life_contract.mount;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.NetworkHandler;

import java.util.function.Supplier;

public class PacketMountMovement {
    private final double x;
    private final double y;
    private final double z;

    public PacketMountMovement(Vec3 movement) {
        this.x = movement.x;
        this.y = movement.y;
        this.z = movement.z;
    }

    public PacketMountMovement(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(PacketMountMovement packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
    }

    public static PacketMountMovement decode(FriendlyByteBuf buffer) {
        return new PacketMountMovement(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble()
        );
    }

    public static void handle(PacketMountMovement packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (!BeastRiderMountSystem.isBeastRider(player)) return;
            if (!BeastRiderMountSystem.isMounted(player)) return;

            Mob mount = BeastRiderMountSystem.getMountEntity(player);
            if (mount == null || !mount.isAlive()) {
                BeastRiderMountSystem.dismountCreature(player);
                return;
            }

            Vec3 movement = new Vec3(packet.x, packet.y, packet.z);
            BeastRiderMountSystem.controlMovement(player, movement);
        });
        context.setPacketHandled(true);
    }

    public Vec3 getMovement() {
        return new Vec3(x, y, z);
    }
}
