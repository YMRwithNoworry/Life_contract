package org.alku.life_contract.wraith_councilor;

import net.minecraft.resources.ResourceLocation;
import org.alku.life_contract.Life_contract;
import software.bernie.geckolib.model.GeoModel;

public class PurpleHoodedSkullArmorModel extends GeoModel<PurpleHoodedSkullArmorItem> {
    
    private static final ResourceLocation CHESTPLATE_MODEL = new ResourceLocation(Life_contract.MODID, "geo/purple_hooded_skull_chestplate.geo.json");
    
    private static final ResourceLocation CHESTPLATE_TEXTURE = new ResourceLocation(Life_contract.MODID, "textures/armor/purple_hooded_skull_chestplate.png");
    
    private static final ResourceLocation ANIMATION = new ResourceLocation(Life_contract.MODID, "animations/purple_hooded_skull_armor.animation.json");
    
    @Override
    public ResourceLocation getModelResource(PurpleHoodedSkullArmorItem animatable) {
        return CHESTPLATE_MODEL;
    }
    
    @Override
    public ResourceLocation getTextureResource(PurpleHoodedSkullArmorItem animatable) {
        return CHESTPLATE_TEXTURE;
    }
    
    @Override
    public ResourceLocation getAnimationResource(PurpleHoodedSkullArmorItem animatable) {
        return ANIMATION;
    }
}
