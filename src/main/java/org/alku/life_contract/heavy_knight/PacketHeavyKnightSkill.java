package org.alku.life_contract.heavy_knight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class PacketHeavyKnightSkill {
    private final int skillId;

    public static final int SKILL_CHARGE = 1;
    public static final int SKILL_SHIELD_BASH = 2;

    public PacketHeavyKnightSkill(int skillId) {
        this.skillId = skillId;
    }

    public static void encode(PacketHeavyKnightSkill msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.skillId);
    }

    public static PacketHeavyKnightSkill decode(FriendlyByteBuf buffer) {
        return new PacketHeavyKnightSkill(buffer.readInt());
    }

    public static void handle(PacketHeavyKnightSkill msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            switch (msg.skillId) {
                case SKILL_CHARGE:
                    HeavyKnightSystem.useCharge(player);
                    break;
                case SKILL_SHIELD_BASH:
                    HeavyKnightSystem.useShieldBash(player);
                    break;
            }
        });
        context.setPacketHandled(true);
    }
}
