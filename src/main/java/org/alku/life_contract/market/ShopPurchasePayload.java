package org.alku.life_contract.market;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

/**
 * 兑换请求（客户端 → 服务端）。
 * <p>
 * 客户端只说"买哪个商品、买几份"：价格、数量、发放内容全部由服务端查表，
 * 所以改客户端改不出便宜货，也不会因为界面与服务端不同步而发错东西。
 */
public record ShopPurchasePayload(String productId, int bundles) implements CustomPacketPayload {

    public static final Type<ShopPurchasePayload> TYPE = NetworkHandler.type("shop_purchase");
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopPurchasePayload> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), ShopPurchasePayload::new);

    public ShopPurchasePayload(FriendlyByteBuf buffer) {
        this(buffer.readUtf(96), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(productId, 96);
        buffer.writeVarInt(bundles);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShopPurchasePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BulletShopService.PurchaseResult result =
                    BulletShopService.purchase(player, payload.productId(), payload.bundles());
            player.sendSystemMessage(result.message());
            NetworkHandler.sendToPlayer(player,
                    new ShopFeedbackPayload(result.success(), result.message().getString()));
        });
    }
}
