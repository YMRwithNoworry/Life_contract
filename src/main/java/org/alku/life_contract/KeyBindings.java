package org.alku.life_contract;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    public static final String CATEGORY = "key.categories.life_contract";
    
    public static final KeyMapping OPEN_TEAM_INVENTORY = new KeyMapping(
            "key.life_contract.open_team_inventory",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );
    public static final KeyMapping OPEN_MUTATION_TREE = new KeyMapping(
            "key.life_contract.open_mutation_tree", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, CATEGORY);
    /** 释放饰品主动技（取品阶最高且冷却已结束的那一件）。 */
    public static final KeyMapping USE_ACCESSORY_ACTIVE = new KeyMapping(
            "key.life_contract.use_accessory_active", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TEAM_INVENTORY);
        event.register(OPEN_MUTATION_TREE);
        event.register(USE_ACCESSORY_ACTIVE);
    }
}
