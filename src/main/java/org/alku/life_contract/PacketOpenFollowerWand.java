package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenFollowerWand {

    private boolean isOffHand;

    public PacketOpenFollowerWand() {
    }

    public PacketOpenFollowerWand(InteractionHand hand) {
        this.isOffHand = (hand == InteractionHand.OFF_HAND);
    }

    public PacketOpenFollowerWand(FriendlyByteBuf buffer) {
        this.isOffHand = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isOffHand);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                InteractionHand hand = isOffHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                player.openMenu(new SimpleMenuProvider(
                        (windowId, inv, p) -> new FollowerWandMenu(windowId, inv, hand),
                        net.minecraft.network.chat.Component.literal("跟随之杖")
                ));
            }
        });
        context.setPacketHandled(true);
    }
}
