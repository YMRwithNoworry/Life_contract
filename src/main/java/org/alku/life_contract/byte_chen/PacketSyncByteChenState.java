package org.alku.life_contract.byte_chen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncByteChenState {
    private final int compute;
    private final int fullReadCooldown;
    private final int dataDispatchCooldown;
    private final int dataBanCooldown;
    private final int ultimateCooldown;
    private final int recycleCooldown;
    private final int exhaustTimer;
    private final boolean ultimateActive;
    private final int ultimateTimer;
    private final int nodeCount;

    public PacketSyncByteChenState(int compute, int fullReadCooldown, int dataDispatchCooldown,
                                    int dataBanCooldown, int ultimateCooldown, int recycleCooldown,
                                    int exhaustTimer, boolean ultimateActive, int ultimateTimer, int nodeCount) {
        this.compute = compute;
        this.fullReadCooldown = fullReadCooldown;
        this.dataDispatchCooldown = dataDispatchCooldown;
        this.dataBanCooldown = dataBanCooldown;
        this.ultimateCooldown = ultimateCooldown;
        this.recycleCooldown = recycleCooldown;
        this.exhaustTimer = exhaustTimer;
        this.ultimateActive = ultimateActive;
        this.ultimateTimer = ultimateTimer;
        this.nodeCount = nodeCount;
    }

    public static void encode(PacketSyncByteChenState msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.compute);
        buffer.writeInt(msg.fullReadCooldown);
        buffer.writeInt(msg.dataDispatchCooldown);
        buffer.writeInt(msg.dataBanCooldown);
        buffer.writeInt(msg.ultimateCooldown);
        buffer.writeInt(msg.recycleCooldown);
        buffer.writeInt(msg.exhaustTimer);
        buffer.writeBoolean(msg.ultimateActive);
        buffer.writeInt(msg.ultimateTimer);
        buffer.writeInt(msg.nodeCount);
    }

    public static PacketSyncByteChenState decode(FriendlyByteBuf buffer) {
        return new PacketSyncByteChenState(
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readBoolean(),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static void handle(PacketSyncByteChenState msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientByteChenState.setCompute(msg.compute);
            ClientByteChenState.setFullReadCooldown(msg.fullReadCooldown);
            ClientByteChenState.setDataDispatchCooldown(msg.dataDispatchCooldown);
            ClientByteChenState.setDataBanCooldown(msg.dataBanCooldown);
            ClientByteChenState.setUltimateCooldown(msg.ultimateCooldown);
            ClientByteChenState.setRecycleCooldown(msg.recycleCooldown);
            ClientByteChenState.setExhaustTimer(msg.exhaustTimer);
            ClientByteChenState.setUltimateActive(msg.ultimateActive);
            ClientByteChenState.setUltimateTimer(msg.ultimateTimer);
            ClientByteChenState.setNodeCount(msg.nodeCount);
        });
        context.setPacketHandled(true);
    }

    public int getCompute() { return compute; }
    public int getFullReadCooldown() { return fullReadCooldown; }
    public int getDataDispatchCooldown() { return dataDispatchCooldown; }
    public int getDataBanCooldown() { return dataBanCooldown; }
    public int getUltimateCooldown() { return ultimateCooldown; }
    public int getRecycleCooldown() { return recycleCooldown; }
    public int getExhaustTimer() { return exhaustTimer; }
    public boolean isUltimateActive() { return ultimateActive; }
    public int getUltimateTimer() { return ultimateTimer; }
    public int getNodeCount() { return nodeCount; }
}
