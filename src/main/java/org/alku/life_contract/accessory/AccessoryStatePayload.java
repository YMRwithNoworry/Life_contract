package org.alku.life_contract.accessory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把玩家自己的饰品运行时状态同步给客户端：
 * 生效的各派系件数（用于显示共鸣进度）与各饰品当前的击杀充能层数。
 * <p>
 * 详情卡是客户端界面，读不到服务端的 {@link AccessoryState} 与 Curios 槽位，
 * 所以这两个数值必须同步下来；没有同步数据时详情卡只是不显示"当前"那一行，
 * 静态机制说明依然完整。
 */
public record AccessoryStatePayload(Map<String, Integer> factionCounts,
                                    Map<String, Integer> chargeStacks) implements CustomPacketPayload {

    public static final Type<AccessoryStatePayload> TYPE = NetworkHandler.type("accessory_state");
    public static final StreamCodec<RegistryFriendlyByteBuf, AccessoryStatePayload> STREAM_CODEC =
            NetworkHandler.codec((buffer, payload) -> payload.encode(buffer), AccessoryStatePayload::new);

    public AccessoryStatePayload(RegistryFriendlyByteBuf buffer) {
        this(readMap(buffer), readMap(buffer));
    }

    private static Map<String, Integer> readMap(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<String, Integer> map = new LinkedHashMap<>(Math.max(4, size));
        for (int i = 0; i < size; i++) {
            map.put(buffer.readUtf(64), buffer.readVarInt());
        }
        return map;
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        writeMap(buffer, factionCounts);
        writeMap(buffer, chargeStacks);
    }

    private static void writeMap(FriendlyByteBuf buffer, Map<String, Integer> map) {
        buffer.writeVarInt(map.size());
        map.forEach((key, value) -> {
            buffer.writeUtf(key, 64);
            buffer.writeVarInt(value);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AccessoryStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> AccessoryClientState.update(payload));
    }
}
