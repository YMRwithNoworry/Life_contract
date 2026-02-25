package org.alku.life_contract.jungle_ape_god;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketJungleApeSkill {
    private final int skillId;
    private final int targetId;

    public static final int SKILL_Q1 = 1;
    public static final int SKILL_Q2 = 2;
    public static final int SKILL_Q3 = 3;
    public static final int SKILL_R = 4;

    public PacketJungleApeSkill(int skillId) {
        this.skillId = skillId;
        this.targetId = -1;
    }

    public PacketJungleApeSkill(int skillId, int targetId) {
        this.skillId = skillId;
        this.targetId = targetId;
    }

    public PacketJungleApeSkill(FriendlyByteBuf buffer) {
        this.skillId = buffer.readInt();
        this.targetId = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(skillId);
        buffer.writeInt(targetId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            switch (skillId) {
                case SKILL_Q1 -> JungleApeGodSystem.useQ1(player);
                case SKILL_Q2 -> {
                    if (targetId >= 0) {
                        net.minecraft.world.entity.Entity target = player.level().getEntity(targetId);
                        JungleApeGodSystem.useQ2(player, target);
                    } else {
                        JungleApeGodSystem.useQ2(player, null);
                    }
                }
                case SKILL_Q3 -> JungleApeGodSystem.useQ3(player);
                case SKILL_R -> JungleApeGodSystem.useR(player);
            }
        });
        context.setPacketHandled(true);
    }
}
