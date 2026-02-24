package org.alku.life_contract.mineral_generator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.ClientDataStorage;

import java.util.function.Supplier;

public class PacketRemoveMineralGenerator {

    private final BlockPos pos;

    public PacketRemoveMineralGenerator(BlockPos pos) {
        this.pos = pos;
    }

    public static PacketRemoveMineralGenerator decode(FriendlyByteBuf buffer) {
        return new PacketRemoveMineralGenerator(buffer.readBlockPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("removeMineralGeneratorData", BlockPos.class).invoke(null, pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
