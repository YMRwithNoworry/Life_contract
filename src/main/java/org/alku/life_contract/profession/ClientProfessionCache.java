package org.alku.life_contract.profession;

import java.util.ArrayList;
import java.util.List;

public class ClientProfessionCache {
    private static List<Profession> professions = new ArrayList<>();
    private static String currentProfessionId = null;

    public static void setProfessions(List<Profession> profs) {
        professions = new ArrayList<>(profs);
    }

    public static List<Profession> getProfessions() {
        return new ArrayList<>(professions);
    }

    public static Profession getProfession(String id) {
        for (Profession profession : professions) {
            if (profession.getId().equals(id)) {
                return profession;
            }
        }
        return null;
    }

    public static void setCurrentProfessionId(String id) {
        currentProfessionId = id;
    }

    public static String getCurrentProfessionId() {
        return currentProfessionId;
    }

    public static Profession getCurrentProfession() {
        if (currentProfessionId == null) return null;
        return getProfession(currentProfessionId);
    }

    public static void clear() {
        professions.clear();
        currentProfessionId = null;
    }
}
