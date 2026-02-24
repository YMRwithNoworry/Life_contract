package org.alku.life_contract.profession;

import java.util.ArrayList;
import java.util.List;

public class ClientProfessionCache {
    private static List<Profession> professions = new ArrayList<>();

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

    public static void clear() {
        professions.clear();
    }
}
