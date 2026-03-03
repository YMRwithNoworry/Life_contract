package org.alku.life_contract.gourmet;

public class ClientGourmetState {
    private static int umami = 0;
    private static int emergencyCooldown = 0;
    private static int flavorBombCooldown = 0;
    private static int warmFeedCooldown = 0;
    private static int godChefCooldown = 0;
    private static boolean godChefMode = false;

    public static int getUmami() { return umami; }
    public static void setUmami(int value) { umami = value; }

    public static int getEmergencyCooldown() { return emergencyCooldown; }
    public static void setEmergencyCooldown(int value) { emergencyCooldown = value; }

    public static int getFlavorBombCooldown() { return flavorBombCooldown; }
    public static void setFlavorBombCooldown(int value) { flavorBombCooldown = value; }

    public static int getWarmFeedCooldown() { return warmFeedCooldown; }
    public static void setWarmFeedCooldown(int value) { warmFeedCooldown = value; }

    public static int getGodChefCooldown() { return godChefCooldown; }
    public static void setGodChefCooldown(int value) { godChefCooldown = value; }

    public static boolean isGodChefMode() { return godChefMode; }
    public static void setGodChefMode(boolean value) { godChefMode = value; }

    public static int getUmamiTier() {
        if (umami >= 200) return 4;
        if (umami >= 120) return 3;
        if (umami >= 60) return 2;
        return 1;
    }
}
