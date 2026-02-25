package org.alku.life_contract.profession;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketSyncLockedProfessions {
    private final Set<String> lockedProfessions;

    public PacketSyncLockedProfessions(Set<String> lockedProfessions) {
        this.lockedProfessions = lockedProfessions;
    }

    public static void encode(PacketSyncLockedProfessions msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.lockedProfessions.size());
        for (String id : msg.lockedProfessions) {
            buffer.writeUtf(id);
        }
    }

    public static PacketSyncLockedProfessions decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Set<String> locked = new HashSet<>();
        for (int i = 0; i < size; i++) {
            locked.add(buffer.readUtf());
        }
        return new PacketSyncLockedProfessions(locked);
    }

    public static void handle(PacketSyncLockedProfessions msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientUnlockedProfessions.setLockedProfessions(msg.lockedProfessions);
            });
        });
        context.setPacketHandled(true);
    }
}
