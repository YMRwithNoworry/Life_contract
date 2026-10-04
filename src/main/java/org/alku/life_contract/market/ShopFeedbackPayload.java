package org.alku.life_contract.market;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.alku.life_contract.NetworkHandler;

/**
 * 兑换结果反馈（服务端 → 客户端）。
 * <p>
 * 商店界面里有一行状态文字，兑换成功/失败都会立刻显示在那里；
 * 聊天栏里同样会收到一条系统消息，两条路互不影响。
 *
 * @param success 成功为 true（界面显示绿色），失败为 false（红色）
 * @param text    已经组装好的纯文本
 */
public record ShopFeedbackPayload(boolean success, String text) implements CustomPacketPayload {

    public static final Type<ShopFeedbackPayload> TYPE = NetworkHandler.type("shop_feedback");
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopFeedbackPayload> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), ShopFeedbackPayload::new);

    public ShopFeedbackPayload(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readUtf(160));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(success);
        buffer.writeUtf(text, 160);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
