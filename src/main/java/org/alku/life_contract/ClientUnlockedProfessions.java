package org.alku.life_contract;

import java.util.HashSet;
import java.util.Set;

public class ClientUnlockedProfessions {
    private static Set<String> unlockedProfessions = new HashSet<>();

    public static void setUnlockedProfessions(Set<String> professions) {
        unlockedProfessions = new HashSet<>(professions);
    }

    public static boolean isUnlocked(String professionId) {
        return unlockedProfessions.contains(professionId);
    }

    public static Set<String> getUnlockedProfessions() {
        return new HashSet<>(unlockedProfessions);
    }

    public static void clear() {
        unlockedProfessions.clear();
    }
}
