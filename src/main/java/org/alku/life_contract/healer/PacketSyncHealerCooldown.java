package org.alku.life_contract.healer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.ClientDataStorage;

import java.util.function.Supplier;

public class PacketSyncHealerCooldown {
    private final int cooldown;

    public PacketSyncHealerCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public static void encode(PacketSyncHealerCooldown msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.cooldown);
    }

    public static PacketSyncHealerCooldown decode(FriendlyByteBuf buffer) {
        return new PacketSyncHealerCooldown(buffer.readInt());
    }

    public static void handle(PacketSyncHealerCooldown msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientDataStorage.setHealerCooldown(msg.cooldown);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
