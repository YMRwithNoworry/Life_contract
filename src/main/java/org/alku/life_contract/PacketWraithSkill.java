package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketWraithSkill {
    private final int skillId;
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    public static final int SKILL_SUMMON = 1;
    public static final int SKILL_DOMAIN = 2;
    public static final int SKILL_BARRAGE_START = 3;
    public static final int SKILL_BARRAGE_RELEASE = 4;
    public static final int SKILL_ULTIMATE = 5;

    public PacketWraithSkill(int skillId) {
        this.skillId = skillId;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
    }

    public PacketWraithSkill(int skillId, double targetX, double targetY, double targetZ) {
        this.skillId = skillId;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    public PacketWraithSkill(FriendlyByteBuf buffer) {
        this.skillId = buffer.readInt();
        this.targetX = buffer.readDouble();
        this.targetY = buffer.readDouble();
        this.targetZ = buffer.readDouble();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(skillId);
        buffer.writeDouble(targetX);
        buffer.writeDouble(targetY);
        buffer.writeDouble(targetZ);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            switch (skillId) {
                case SKILL_SUMMON -> GhostSenatorSystem.useSummonSkill(player);
                case SKILL_DOMAIN -> {
                    Vec3 targetPos = new Vec3(targetX, targetY, targetZ);
                    GhostSenatorSystem.useDomainSkill(player, targetPos);
                }
                case SKILL_BARRAGE_START -> GhostSenatorSystem.startBarrageCharge(player);
                case SKILL_BARRAGE_RELEASE -> GhostSenatorSystem.releaseBarrage(player);
                case SKILL_ULTIMATE -> GhostSenatorSystem.useUltimate(player);
            }
        });
        context.setPacketHandled(true);
    }
}
