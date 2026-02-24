package org.alku.life_contract.mount;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.NetworkHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncMount {
    private final UUID riderUUID;
    private final UUID mountUUID;
    private final boolean isMounting;

    public PacketSyncMount(UUID riderUUID, UUID mountUUID, boolean isMounting) {
        this.riderUUID = riderUUID;
        this.mountUUID = mountUUID;
        this.isMounting = isMounting;
    }

    public static void encode(PacketSyncMount packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.riderUUID);
        buffer.writeUUID(packet.mountUUID);
        buffer.writeBoolean(packet.isMounting);
    }

    public static PacketSyncMount decode(FriendlyByteBuf buffer) {
        return new PacketSyncMount(
            buffer.readUUID(),
            buffer.readUUID(),
            buffer.readBoolean()
        );
    }

    public static void handle(PacketSyncMount packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("displayMountMessage", boolean.class).invoke(null, packet.isMounting);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
