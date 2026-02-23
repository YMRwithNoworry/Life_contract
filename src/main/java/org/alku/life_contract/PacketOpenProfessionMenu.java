package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenProfessionMenu {

    public PacketOpenProfessionMenu() {
    }

    public static void encode(PacketOpenProfessionMenu msg, FriendlyByteBuf buffer) {
    }

    public static PacketOpenProfessionMenu decode(FriendlyByteBuf buffer) {
        return new PacketOpenProfessionMenu();
    }

    @SuppressWarnings("unchecked")
    public static void handle(PacketOpenProfessionMenu msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    java.lang.reflect.Method method = proxyClass.getMethod("openProfessionScreen");
                    method.invoke(null);
                } catch (ClassNotFoundException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
