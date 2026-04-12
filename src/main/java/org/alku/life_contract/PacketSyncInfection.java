package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncInfection {
    private final int infection;
    
    public PacketSyncInfection(int infection) {
        this.infection = infection;
    }
    
    public PacketSyncInfection(FriendlyByteBuf buf) {
        this.infection = buf.readInt();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(infection);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientInfectionData.setInfection(infection);
        });
        context.setPacketHandled(true);
    }
}
