package org.alku.life_contract.byte_chen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketByteChenSkill {
    private final int skillId;
    private final int targetId;
    private final int mode;

    public static final int SKILL_DEPLOY_SCOUT = 0;
    public static final int SKILL_DEPLOY_BUFF = 1;
    public static final int SKILL_DEPLOY_COUNTER = 2;
    public static final int SKILL_FULL_READ = 3;
    public static final int SKILL_DATA_DISPATCH = 4;
    public static final int SKILL_DATA_DISPATCH_RECYCLE = 5;
    public static final int SKILL_DATA_BAN = 6;
    public static final int SKILL_ULTIMATE = 7;
    public static final int SKILL_RECYCLE_NODES = 8;

    public PacketByteChenSkill(int skillId) {
        this.skillId = skillId;
        this.targetId = -1;
        this.mode = 0;
    }

    public PacketByteChenSkill(int skillId, int targetId) {
        this.skillId = skillId;
        this.targetId = targetId;
        this.mode = 0;
    }

    public PacketByteChenSkill(int skillId, int targetId, int mode) {
        this.skillId = skillId;
        this.targetId = targetId;
        this.mode = mode;
    }

    public static void encode(PacketByteChenSkill msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.skillId);
        buffer.writeInt(msg.targetId);
        buffer.writeInt(msg.mode);
    }

    public static PacketByteChenSkill decode(FriendlyByteBuf buffer) {
        int skillId = buffer.readInt();
        int targetId = buffer.readInt();
        int mode = buffer.readInt();
        return new PacketByteChenSkill(skillId, targetId, mode);
    }

    public static void handle(PacketByteChenSkill msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            switch (msg.skillId) {
                case SKILL_DEPLOY_SCOUT:
                    ByteChenSystem.deployNode(player, ByteChenSystem.NodeType.SCOUT);
                    break;
                case SKILL_DEPLOY_BUFF:
                    ByteChenSystem.deployNode(player, ByteChenSystem.NodeType.BUFF);
                    break;
                case SKILL_DEPLOY_COUNTER:
                    ByteChenSystem.deployNode(player, ByteChenSystem.NodeType.COUNTER);
                    break;
                case SKILL_FULL_READ:
                    ByteChenSystem.useFullRead(player);
                    break;
                case SKILL_DATA_DISPATCH:
                    ByteChenSystem.useDataDispatch(player, 0);
                    break;
                case SKILL_DATA_DISPATCH_RECYCLE:
                    ByteChenSystem.useDataDispatch(player, 1);
                    break;
                case SKILL_DATA_BAN:
                    ByteChenSystem.useDataBan(player, msg.targetId);
                    break;
                case SKILL_ULTIMATE:
                    ByteChenSystem.useUltimate(player);
                    break;
                case SKILL_RECYCLE_NODES:
                    ByteChenSystem.recycleNodes(player);
                    break;
            }
        });
        context.setPacketHandled(true);
    }
}
