package org.alku.life_contract.heavy_knight;

public class ClientHeavyKnightState {
    private static int battleWill = 0;
    private static boolean shieldWallActive = false;
    private static int chargeCooldown = 0;
    private static int protectCooldown = 0;
    private static int shieldBashCooldown = 0;

    public static void receive(int will, boolean shieldWall, int charge, int protect, int bash) {
        battleWill = will;
        shieldWallActive = shieldWall;
        chargeCooldown = charge;
        protectCooldown = protect;
        shieldBashCooldown = bash;
    }

    public static int getBattleWill() {
        return battleWill;
    }

    public static boolean isShieldWallActive() {
        return shieldWallActive;
    }

    public static int getChargeCooldown() {
        return chargeCooldown;
    }

    public static int getProtectCooldown() {
        return protectCooldown;
    }

    public static int getShieldBashCooldown() {
        return shieldBashCooldown;
    }
}
