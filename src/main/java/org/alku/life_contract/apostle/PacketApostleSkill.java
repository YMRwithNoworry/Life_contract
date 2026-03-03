package org.alku.life_contract.apostle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketApostleSkill {
    private final int skillId;

    public static final int SKILL_TELEPORT = 1;
    public static final int SKILL_FIREBALL = 2;

    public PacketApostleSkill(int skillId) {
        this.skillId = skillId;
    }

    public PacketApostleSkill(FriendlyByteBuf buffer) {
        this.skillId = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(skillId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                switch (skillId) {
                    case SKILL_TELEPORT:
                        ApostleSystem.useTeleport(player);
                        break;
                    case SKILL_FIREBALL:
                        ApostleSystem.useFireball(player);
                        break;
                }
            }
        });
        context.setPacketHandled(true);
    }
}
