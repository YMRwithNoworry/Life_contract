package org.alku.life_contract.follower;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FollowerClientCache {
    private static final Map<UUID, UUID> FOLLOWER_OWNER_MAP = new HashMap<>();

    public static void registerFollower(UUID entityUUID, UUID ownerUUID) {
        FOLLOWER_OWNER_MAP.put(entityUUID, ownerUUID);
    }

    public static void unregisterFollower(UUID entityUUID) {
        FOLLOWER_OWNER_MAP.remove(entityUUID);
    }

    public static UUID getOwnerUUID(UUID entityUUID) {
        return FOLLOWER_OWNER_MAP.get(entityUUID);
    }

    public static boolean isFollower(UUID entityUUID) {
        return FOLLOWER_OWNER_MAP.containsKey(entityUUID);
    }

    public static boolean isFollowerOf(UUID entityUUID, UUID playerUUID) {
        UUID ownerUUID = FOLLOWER_OWNER_MAP.get(entityUUID);
        return ownerUUID != null && ownerUUID.equals(playerUUID);
    }

    public static void clear() {
        FOLLOWER_OWNER_MAP.clear();
    }
}
