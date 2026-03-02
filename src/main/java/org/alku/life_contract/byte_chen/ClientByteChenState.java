package org.alku.life_contract.byte_chen;

public class ClientByteChenState {
    private static int compute = 0;
    private static int fullReadCooldown = 0;
    private static int dataDispatchCooldown = 0;
    private static int dataBanCooldown = 0;
    private static int ultimateCooldown = 0;
    private static int recycleCooldown = 0;
    private static int exhaustTimer = 0;
    private static boolean ultimateActive = false;
    private static int ultimateTimer = 0;
    private static int nodeCount = 0;

    public static int getCompute() { return compute; }
    public static void setCompute(int value) { compute = value; }

    public static int getFullReadCooldown() { return fullReadCooldown; }
    public static void setFullReadCooldown(int value) { fullReadCooldown = value; }

    public static int getDataDispatchCooldown() { return dataDispatchCooldown; }
    public static void setDataDispatchCooldown(int value) { dataDispatchCooldown = value; }

    public static int getDataBanCooldown() { return dataBanCooldown; }
    public static void setDataBanCooldown(int value) { dataBanCooldown = value; }

    public static int getUltimateCooldown() { return ultimateCooldown; }
    public static void setUltimateCooldown(int value) { ultimateCooldown = value; }

    public static int getRecycleCooldown() { return recycleCooldown; }
    public static void setRecycleCooldown(int value) { recycleCooldown = value; }

    public static int getExhaustTimer() { return exhaustTimer; }
    public static void setExhaustTimer(int value) { exhaustTimer = value; }

    public static boolean isUltimateActive() { return ultimateActive; }
    public static void setUltimateActive(boolean value) { ultimateActive = value; }

    public static int getUltimateTimer() { return ultimateTimer; }
    public static void setUltimateTimer(int value) { ultimateTimer = value; }

    public static int getNodeCount() { return nodeCount; }
    public static void setNodeCount(int value) { nodeCount = value; }

    public static boolean isExhausted() {
        return exhaustTimer > 0;
    }

    public static int getComputePercent(int maxCompute) {
        if (maxCompute <= 0) return 0;
        return (int)((float)compute / maxCompute * 100);
    }
}
