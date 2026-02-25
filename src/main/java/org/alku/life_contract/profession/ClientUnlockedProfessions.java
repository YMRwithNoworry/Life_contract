package org.alku.life_contract.profession;

import java.util.HashSet;
import java.util.Set;

public class ClientUnlockedProfessions {
    private static Set<String> unlockedProfessions = new HashSet<>();
    private static Set<String> lockedProfessions = new HashSet<>();

    public static void setUnlockedProfessions(Set<String> professions) {
        unlockedProfessions = new HashSet<>(professions);
    }

    public static void setLockedProfessions(Set<String> professions) {
        lockedProfessions = new HashSet<>(professions);
    }

    public static boolean isUnlocked(String professionId) {
        return unlockedProfessions.contains(professionId);
    }

    public static boolean isLocked(String professionId) {
        return lockedProfessions.contains(professionId);
    }

    public static Set<String> getUnlockedProfessions() {
        return new HashSet<>(unlockedProfessions);
    }

    public static Set<String> getLockedProfessions() {
        return new HashSet<>(lockedProfessions);
    }

    public static void clear() {
        unlockedProfessions.clear();
        lockedProfessions.clear();
    }
}
