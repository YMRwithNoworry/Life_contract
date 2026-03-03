package org.alku.life_contract.gourmet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGourmetSkill {

    private final int skillId;
    private final int targetId;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final boolean isGroupMode;

    public static final int EMERGENCY_STIR_FRY = 1;
    public static final int FLAVOR_BOMB = 2;
    public static final int WARM_FEED = 3;
    public static final int GOD_CHEF_DESCENT = 4;

    public PacketGourmetSkill(int skillId) {
        this.skillId = skillId;
        this.targetId = -1;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.isGroupMode = false;
    }

    public PacketGourmetSkill(int skillId, int targetId) {
        this.skillId = skillId;
        this.targetId = targetId;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.isGroupMode = false;
    }

    public PacketGourmetSkill(int skillId, double x, double y, double z) {
        this.skillId = skillId;
        this.targetId = -1;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.isGroupMode = false;
    }

    public PacketGourmetSkill(int skillId, boolean isGroupMode) {
        this.skillId = skillId;
        this.targetId = -1;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.isGroupMode = isGroupMode;
    }

    public static void encode(PacketGourmetSkill msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.skillId);
        buffer.writeInt(msg.targetId);
        buffer.writeDouble(msg.targetX);
        buffer.writeDouble(msg.targetY);
        buffer.writeDouble(msg.targetZ);
        buffer.writeBoolean(msg.isGroupMode);
    }

    public static PacketGourmetSkill decode(FriendlyByteBuf buffer) {
        int skillId = buffer.readInt();
        int targetId = buffer.readInt();
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        boolean isGroupMode = buffer.readBoolean();

        PacketGourmetSkill msg = new PacketGourmetSkill(skillId, isGroupMode);
        return msg;
    }

    public static void handle(PacketGourmetSkill msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
                return;
            }

            switch (msg.skillId) {
                case EMERGENCY_STIR_FRY -> GourmetSkills.useEmergencyStirFry(player);
                case FLAVOR_BOMB -> GourmetSkills.useFlavorBomb(player, msg.targetX, msg.targetY, msg.targetZ);
                case WARM_FEED -> GourmetSkills.useWarmFeed(player, msg.isGroupMode);
                case GOD_CHEF_DESCENT -> GourmetSkills.useGodChefDescent(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}
