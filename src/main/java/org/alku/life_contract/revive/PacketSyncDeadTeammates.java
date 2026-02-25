package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncDeadTeammates {

    private final List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates;

    public PacketSyncDeadTeammates(List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates) {
        this.deadTeammates = deadTeammates;
    }

    public static void encode(PacketSyncDeadTeammates msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.deadTeammates.size());
        for (ReviveTeammateSystem.DeadTeammateInfo info : msg.deadTeammates) {
            buffer.writeUUID(info.getUuid());
            buffer.writeUtf(info.getName());
            buffer.writeLong(info.getDeathTime());
            buffer.writeDouble(info.getDeathX());
            buffer.writeDouble(info.getDeathY());
            buffer.writeDouble(info.getDeathZ());
            buffer.writeResourceLocation(info.getDimension());
        }
    }

    public static PacketSyncDeadTeammates decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UUID uuid = buffer.readUUID();
            String name = buffer.readUtf();
            long deathTime = buffer.readLong();
            double deathX = buffer.readDouble();
            double deathY = buffer.readDouble();
            double deathZ = buffer.readDouble();
            ResourceLocation dimension = buffer.readResourceLocation();
            deadTeammates.add(new ReviveTeammateSystem.DeadTeammateInfo(
                uuid, name, deathTime, deathX, deathY, deathZ, dimension
            ));
        }
        return new PacketSyncDeadTeammates(deadTeammates);
    }

    public static void handle(PacketSyncDeadTeammates msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    java.lang.reflect.Method method = proxyClass.getMethod("openReviveScreen", java.util.List.class);
                    method.invoke(null, msg.deadTeammates);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public List<ReviveTeammateSystem.DeadTeammateInfo> getDeadTeammates() {
        return deadTeammates;
    }
}
