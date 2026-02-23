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
    
    public static final KeyMapping MOUNT_NEAREST = new KeyMapping(
            "key.life_contract.mount_nearest",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    
    public static final KeyMapping DISMOUNT = new KeyMapping(
            "key.life_contract.dismount",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );
    
    public static final KeyMapping MOUNT_BEHAVIOR_CYCLE = new KeyMapping(
            "key.life_contract.mount_behavior_cycle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    
    public static final KeyMapping MOUNT_SPECIAL_ABILITY = new KeyMapping(
            "key.life_contract.mount_special_ability",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            CATEGORY
    );
    
    public static final KeyMapping HEALER_ACTIVE_HEAL = new KeyMapping(
            "key.life_contract.healer_active_heal",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );
    
    public static final KeyMapping FOOL_STEAL_PROFESSION = new KeyMapping(
            "key.life_contract.fool_steal_profession",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TEAM_INVENTORY);
        event.register(MOUNT_NEAREST);
        event.register(DISMOUNT);
        event.register(MOUNT_BEHAVIOR_CYCLE);
        event.register(MOUNT_SPECIAL_ABILITY);
        event.register(HEALER_ACTIVE_HEAL);
        event.register(FOOL_STEAL_PROFESSION);
    }
}
