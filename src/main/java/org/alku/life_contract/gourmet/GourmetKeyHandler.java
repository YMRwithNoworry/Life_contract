package org.alku.life_contract.gourmet;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GourmetKeyHandler {

    public static final String CATEGORY = "key.categories.life_contract";

    public static final KeyMapping EMERGENCY_STIR_FRY = new KeyMapping(
        "key.life_contract.gourmet_emergency",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        CATEGORY
    );

    public static final KeyMapping FLAVOR_BOMB = new KeyMapping(
        "key.life_contract.gourmet_bomb",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        CATEGORY
    );

    public static final KeyMapping WARM_FEED = new KeyMapping(
        "key.life_contract.gourmet_feed",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        CATEGORY
    );

    public static final KeyMapping GOD_CHEF_DESCENT = new KeyMapping(
        "key.life_contract.gourmet_ultimate",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(EMERGENCY_STIR_FRY);
        event.register(FLAVOR_BOMB);
        event.register(WARM_FEED);
        event.register(GOD_CHEF_DESCENT);
    }
}
