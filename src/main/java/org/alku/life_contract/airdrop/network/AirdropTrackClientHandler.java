package org.alku.life_contract.airdrop.network;

import net.minecraft.world.phys.Vec3;

/**
 * 客户端追踪目标持有者，由 AirdropPayload 的网络处理器设置。
 */
public class AirdropTrackClientHandler {
    private static Vec3 targetPos = null;

    public static void setTarget(AirdropPayload payload) {
        if (payload.hasTarget()) {
            targetPos = new Vec3(payload.targetX(), payload.targetY(), payload.targetZ());
        } else {
            targetPos = null;
        }
    }

    public static Vec3 getTargetPos() {
        return targetPos;
    }

    public static void clearTarget() {
        targetPos = null;
    }
}
