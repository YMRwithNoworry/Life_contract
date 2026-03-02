package org.alku.life_contract.heavy_knight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncHeavyKnightState {
    private final int battleWill;
    private final boolean shieldWallActive;
    private final int chargeCooldown;
    private final int protectCooldown;
    private final int shieldBashCooldown;

    public PacketSyncHeavyKnightState(int battleWill, boolean shieldWallActive, 
                                       int chargeCooldown, int protectCooldown, int shieldBashCooldown) {
        this.battleWill = battleWill;
        this.shieldWallActive = shieldWallActive;
        this.chargeCooldown = chargeCooldown;
        this.protectCooldown = protectCooldown;
        this.shieldBashCooldown = shieldBashCooldown;
    }

    public static void encode(PacketSyncHeavyKnightState msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.battleWill);
        buffer.writeBoolean(msg.shieldWallActive);
        buffer.writeInt(msg.chargeCooldown);
        buffer.writeInt(msg.protectCooldown);
        buffer.writeInt(msg.shieldBashCooldown);
    }

    public static PacketSyncHeavyKnightState decode(FriendlyByteBuf buffer) {
        return new PacketSyncHeavyKnightState(
            buffer.readInt(),
            buffer.readBoolean(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static void handle(PacketSyncHeavyKnightState msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                ClientHeavyKnightState.receive(msg.battleWill, msg.shieldWallActive, 
                    msg.chargeCooldown, msg.protectCooldown, msg.shieldBashCooldown)
            );
        });
        context.setPacketHandled(true);
    }
}
