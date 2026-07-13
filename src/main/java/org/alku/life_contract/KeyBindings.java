package org.alku.life_contract;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TEAM_INVENTORY);
        event.register(OPEN_MUTATION_TREE);
    }
}
