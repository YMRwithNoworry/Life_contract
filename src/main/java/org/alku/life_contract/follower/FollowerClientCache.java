package org.alku.life_contract.follower;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FollowerClientCache {
    private static final Map<UUID, UUID> FOLLOWER_OWNER_MAP = new HashMap<>();
    private static final Map<UUID, Integer> FOLLOWER_ENTITY_IDS = new HashMap<>();
    private static final Map<UUID, Set<Integer>> OWNER_FOLLOWER_IDS = new HashMap<>();

    public static void registerFollower(UUID entityUUID, int entityId, UUID ownerUUID) {
        UUID previousOwner = FOLLOWER_OWNER_MAP.put(entityUUID, ownerUUID);
        Integer previousId = FOLLOWER_ENTITY_IDS.put(entityUUID, entityId);
        if (previousOwner != null && previousId != null) {
            removeOwnerIndex(previousOwner, previousId);
        }
        OWNER_FOLLOWER_IDS.computeIfAbsent(ownerUUID, ignored -> new HashSet<>()).add(entityId);
    }

    public static void unregisterFollower(UUID entityUUID) {
        UUID ownerUUID = FOLLOWER_OWNER_MAP.remove(entityUUID);
        Integer entityId = FOLLOWER_ENTITY_IDS.remove(entityUUID);
        if (ownerUUID != null && entityId != null) {
            removeOwnerIndex(ownerUUID, entityId);
        }
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

    public static Iterable<Integer> getFollowerEntityIds(UUID ownerUUID) {
        Set<Integer> ids = OWNER_FOLLOWER_IDS.get(ownerUUID);
        return ids != null ? ids : Set.of();
    }

    private static void removeOwnerIndex(UUID ownerUUID, int entityId) {
        Set<Integer> ids = OWNER_FOLLOWER_IDS.get(ownerUUID);
        if (ids != null) {
            ids.remove(entityId);
            if (ids.isEmpty()) {
                OWNER_FOLLOWER_IDS.remove(ownerUUID);
            }
        }
    }

    public static void clear() {
        FOLLOWER_OWNER_MAP.clear();
        FOLLOWER_ENTITY_IDS.clear();
        OWNER_FOLLOWER_IDS.clear();
    }
}
