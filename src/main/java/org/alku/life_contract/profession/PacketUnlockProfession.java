package org.alku.life_contract.profession;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import org.alku.life_contract.NetworkHandler;

import java.util.function.Supplier;

public class PacketUnlockProfession {
    private final String professionId;
    private final String password;

    public PacketUnlockProfession(String professionId, String password) {
        this.professionId = professionId;
        this.password = password;
    }

    public static void encode(PacketUnlockProfession msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.professionId);
        buffer.writeUtf(msg.password);
    }

    public static PacketUnlockProfession decode(FriendlyByteBuf buffer) {
        return new PacketUnlockProfession(buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(PacketUnlockProfession msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Profession profession = ProfessionConfig.getProfession(msg.professionId);
                if (profession != null && profession.requiresPassword()) {
                    if (ProfessionConfig.unlockProfession(player.getUUID(), msg.professionId, msg.password)) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§a[生灵契约] §f成功解锁职业: §e" + profession.getName()));
                        NetworkHandler.syncUnlockedProfessions(player);
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§c[生灵契约] 密码错误，解锁失败！"));
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
