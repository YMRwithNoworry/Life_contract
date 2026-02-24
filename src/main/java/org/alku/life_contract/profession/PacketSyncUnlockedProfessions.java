package org.alku.life_contract.profession;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.ClientDataStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketSyncUnlockedProfessions {
    private final Set<String> unlockedProfessions;

    public PacketSyncUnlockedProfessions(Set<String> unlockedProfessions) {
        this.unlockedProfessions = unlockedProfessions;
    }

    public static void encode(PacketSyncUnlockedProfessions msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.unlockedProfessions.size());
        for (String id : msg.unlockedProfessions) {
            buffer.writeUtf(id);
        }
    }

    public static PacketSyncUnlockedProfessions decode(FriendlyByteBuf buffer) {
        Set<String> unlocked = new HashSet<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            unlocked.add(buffer.readUtf());
        }
        return new PacketSyncUnlockedProfessions(unlocked);
    }

    public static void handle(PacketSyncUnlockedProfessions msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientUnlockedProfessions.setUnlockedProfessions(msg.unlockedProfessions);
            });
        });
        context.setPacketHandled(true);
    }
}
