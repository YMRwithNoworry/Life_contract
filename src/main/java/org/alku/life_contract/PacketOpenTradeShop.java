package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenTradeShop {
    private final boolean isRemoveMode;

    public PacketOpenTradeShop(boolean isRemoveMode) {
        this.isRemoveMode = isRemoveMode;
    }

    public PacketOpenTradeShop(FriendlyByteBuf buf) {
        this.isRemoveMode = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isRemoveMode);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("setTradeShopRemoveMode", boolean.class).invoke(null, isRemoveMode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
