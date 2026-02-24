package org.alku.life_contract.revive;

import java.util.ArrayList;
import java.util.List;

public class ClientReviveData {
    private static List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates = new ArrayList<>();

    public static void setDeadTeammates(List<ReviveTeammateSystem.DeadTeammateInfo> teammates) {
        deadTeammates = new ArrayList<>(teammates);
    }

    public static List<ReviveTeammateSystem.DeadTeammateInfo> getDeadTeammates() {
        return new ArrayList<>(deadTeammates);
    }

    public static void clear() {
        deadTeammates.clear();
    }
}
