package org.alku.life_contract;

public class ClientInfectionData {
    private static int infection = 0;
    
    public static void setInfection(int value) {
        infection = value;
    }
    
    public static int getInfection() {
        return infection;
    }
    
    public static float getInfectionPercent() {
        return (float) infection / PlayerInfectionSystem.getMaxInfection();
    }
}
