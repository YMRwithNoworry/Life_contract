package org.alku.life_contract.profession;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncProfessions {
    private final List<Profession> professions;

    public PacketSyncProfessions(List<Profession> professions) {
        this.professions = professions;
    }

    public static void encode(PacketSyncProfessions msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.professions.size());
        for (Profession profession : msg.professions) {
            profession.writeToBuffer(buffer);
        }
    }

    public static PacketSyncProfessions decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<Profession> professions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            professions.add(Profession.readFromBuffer(buffer));
        }
        return new PacketSyncProfessions(professions);
    }

    public static void handle(PacketSyncProfessions msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientProfessionCache.setProfessions(msg.professions);
            });
        });
        context.setPacketHandled(true);
    }
}
