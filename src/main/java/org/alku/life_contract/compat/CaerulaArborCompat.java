package org.alku.life_contract.compat;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CaerulaArborCompat {
    private static final String MOD_ID = "caerula_arbor";
    private static final String VARIABLES_CLASS = "net.mcreator.caerulaarbor.network.CaerulaArborModVariables";
    private static final String PLAYER_VARIABLES_CLASS = "net.mcreator.caerulaarbor.network.CaerulaArborModVariables$PlayerVariables";

    private static Capability<?> playerVariablesCapability;
    private static Field playerLivesField;
    private static Method syncPlayerVariablesMethod;
    private static boolean initialized;
    private static boolean available;

    private CaerulaArborCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static int getLifePoints(Entity entity) {
        if (entity == null || !ensureInitialized()) {
            return -1;
        }

        Object variables = getPlayerVariables(entity);
        if (variables == null) {
            return -1;
        }

        try {
            return (int) Math.round(playerLivesField.getDouble(variables));
        } catch (ReflectiveOperationException e) {
            available = false;
            return -1;
        }
    }

    public static boolean setLifePoints(Entity entity, int lifePoints) {
        if (entity == null || !ensureInitialized()) {
            return false;
        }

        Object variables = getPlayerVariables(entity);
        if (variables == null) {
            return false;
        }

        try {
            playerLivesField.setDouble(variables, Math.max(1, lifePoints));
            syncPlayerVariablesMethod.invoke(variables, entity);
            return true;
        } catch (ReflectiveOperationException e) {
            available = false;
            return false;
        }
    }

    private static Object getPlayerVariables(Entity entity) {
        try {
            return entity.getCapability(playerVariablesCapability, null).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean ensureInitialized() {
        if (!isLoaded()) {
            return false;
        }
        if (initialized) {
            return available;
        }

        initialized = true;
        try {
            Class<?> variablesClass = Class.forName(VARIABLES_CLASS);
            Class<?> playerVariablesClass = Class.forName(PLAYER_VARIABLES_CLASS);
            playerVariablesCapability = (Capability<?>) variablesClass.getField("PLAYER_VARIABLES_CAPABILITY").get(null);
            playerLivesField = playerVariablesClass.getField("player_lives");
            syncPlayerVariablesMethod = playerVariablesClass.getMethod("syncPlayerVariables", Entity.class);
            available = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            available = false;
        }

        return available;
    }
}
