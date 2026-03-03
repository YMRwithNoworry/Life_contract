package org.alku.life_contract.apostle;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncApostleState {
    private final int teleportCooldown;
    private final int fireballCooldown;
    private final boolean inFire;

    public PacketSyncApostleState(int teleportCooldown, int fireballCooldown, boolean inFire) {
        this.teleportCooldown = teleportCooldown;
        this.fireballCooldown = fireballCooldown;
        this.inFire = inFire;
    }

    public PacketSyncApostleState(FriendlyByteBuf buffer) {
        this.teleportCooldown = buffer.readInt();
        this.fireballCooldown = buffer.readInt();
        this.inFire = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(teleportCooldown);
        buffer.writeInt(fireballCooldown);
        buffer.writeBoolean(inFire);
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
            data.putInt("ApostleTeleportCooldownClient", teleportCooldown);
            data.putInt("ApostleFireballCooldownClient", fireballCooldown);
            data.putBoolean("ApostleInFireClient", inFire);
        }
    }
}
