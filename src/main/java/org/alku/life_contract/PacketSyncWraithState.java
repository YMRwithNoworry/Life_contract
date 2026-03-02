package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncWraithState {
    private final int soulValue;
    private final int summonCooldown;
    private final int domainCooldown;
    private final int barrageCooldown;
    private final int ultimateCooldown;
    private final boolean ultimateActive;
    private final boolean exhausted;
    private final boolean chargingBarrage;
    private final int barrageChargeTime;

    public PacketSyncWraithState(int soulValue, int summonCooldown, int domainCooldown,
            int barrageCooldown, int ultimateCooldown, boolean ultimateActive, boolean exhausted,
            boolean chargingBarrage, int barrageChargeTime) {
        this.soulValue = soulValue;
        this.summonCooldown = summonCooldown;
        this.domainCooldown = domainCooldown;
        this.barrageCooldown = barrageCooldown;
        this.ultimateCooldown = ultimateCooldown;
        this.ultimateActive = ultimateActive;
        this.exhausted = exhausted;
        this.chargingBarrage = chargingBarrage;
        this.barrageChargeTime = barrageChargeTime;
    }

    public PacketSyncWraithState(FriendlyByteBuf buffer) {
        this.soulValue = buffer.readInt();
        this.summonCooldown = buffer.readInt();
        this.domainCooldown = buffer.readInt();
        this.barrageCooldown = buffer.readInt();
        this.ultimateCooldown = buffer.readInt();
        this.ultimateActive = buffer.readBoolean();
        this.exhausted = buffer.readBoolean();
        this.chargingBarrage = buffer.readBoolean();
        this.barrageChargeTime = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(soulValue);
        buffer.writeInt(summonCooldown);
        buffer.writeInt(domainCooldown);
        buffer.writeInt(barrageCooldown);
        buffer.writeInt(ultimateCooldown);
        buffer.writeBoolean(ultimateActive);
        buffer.writeBoolean(exhausted);
        buffer.writeBoolean(chargingBarrage);
        buffer.writeInt(barrageChargeTime);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClient();
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            CompoundTag data = mc.player.getPersistentData();
            data.putInt("WraithSoulValueClient", soulValue);
            data.putInt("WraithSummonCooldownClient", summonCooldown);
            data.putInt("WraithDomainCooldownClient", domainCooldown);
            data.putInt("WraithBarrageCooldownClient", barrageCooldown);
            data.putInt("WraithUltimateCooldownClient", ultimateCooldown);
            data.putBoolean("WraithUltimateActiveClient", ultimateActive);
            data.putBoolean("WraithExhaustedClient", exhausted);
            data.putBoolean("WraithChargingBarrageClient", chargingBarrage);
            data.putInt("WraithBarrageChargeTimeClient", barrageChargeTime);
        }
    }
}
