package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncChaosBalance {
    private final double bonusPercent;
    private final int infectedCount;
    private final int nonInfectedCount;

    public PacketSyncChaosBalance(double bonusPercent, int infectedCount, int nonInfectedCount) {
        this.bonusPercent = bonusPercent;
        this.infectedCount = infectedCount;
        this.nonInfectedCount = nonInfectedCount;
    }

    public PacketSyncChaosBalance(FriendlyByteBuf buffer) {
        this.bonusPercent = buffer.readDouble();
        this.infectedCount = buffer.readInt();
        this.nonInfectedCount = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(bonusPercent);
        buffer.writeInt(infectedCount);
        buffer.writeInt(nonInfectedCount);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientDataStorage.setChaosBalanceData(bonusPercent, infectedCount, nonInfectedCount);
            });
        });
        context.setPacketHandled(true);
    }
}
