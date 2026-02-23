package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncMineralGenerationState {

    private final boolean enabled;

    public PacketSyncMineralGenerationState(boolean enabled) {
        this.enabled = enabled;
    }

    public PacketSyncMineralGenerationState(FriendlyByteBuf buf) {
        this.enabled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(enabled);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("setMineralGenerationEnabled", boolean.class).invoke(null, enabled);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
