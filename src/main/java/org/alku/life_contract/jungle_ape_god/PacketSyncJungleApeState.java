package org.alku.life_contract.jungle_ape_god;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncJungleApeState {
    private final int rhythmStacks;
    private final boolean isBerserk;
    private final boolean isRActive;
    private final int q1Cooldown;
    private final int q2Cooldown;
    private final int q3Cooldown;
    private final int rCooldown;

    public PacketSyncJungleApeState(int rhythmStacks, boolean isBerserk, boolean isRActive,
            int q1Cooldown, int q2Cooldown, int q3Cooldown, int rCooldown) {
        this.rhythmStacks = rhythmStacks;
        this.isBerserk = isBerserk;
        this.isRActive = isRActive;
        this.q1Cooldown = q1Cooldown;
        this.q2Cooldown = q2Cooldown;
        this.q3Cooldown = q3Cooldown;
        this.rCooldown = rCooldown;
    }

    public PacketSyncJungleApeState(FriendlyByteBuf buffer) {
        this.rhythmStacks = buffer.readInt();
        this.isBerserk = buffer.readBoolean();
        this.isRActive = buffer.readBoolean();
        this.q1Cooldown = buffer.readInt();
        this.q2Cooldown = buffer.readInt();
        this.q3Cooldown = buffer.readInt();
        this.rCooldown = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(rhythmStacks);
        buffer.writeBoolean(isBerserk);
        buffer.writeBoolean(isRActive);
        buffer.writeInt(q1Cooldown);
        buffer.writeInt(q2Cooldown);
        buffer.writeInt(q3Cooldown);
        buffer.writeInt(rCooldown);
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
            data.putInt("JungleApeRhythmStacksClient", rhythmStacks);
            data.putBoolean("JungleApeBerserkClient", isBerserk);
            data.putBoolean("JungleApeRActiveClient", isRActive);
            data.putInt("JungleApeQ1CooldownClient", q1Cooldown);
            data.putInt("JungleApeQ2CooldownClient", q2Cooldown);
            data.putInt("JungleApeQ3CooldownClient", q3Cooldown);
            data.putInt("JungleApeRCooldownClient", rCooldown);
        }
    }
}
