package org.alku.life_contract.wraith_councilor;

import net.minecraft.resources.ResourceLocation;
import org.alku.life_contract.Life_contract;
import software.bernie.geckolib.model.GeoModel;

public class SoulforgedArmorModel extends GeoModel<SoulforgedArmorItem> {
    
    private static final ResourceLocation HELMET_MODEL = new ResourceLocation(Life_contract.MODID, "geo/soulforged_helmet.geo.json");
    private static final ResourceLocation CHESTPLATE_MODEL = new ResourceLocation(Life_contract.MODID, "geo/soulforged_chestplate.geo.json");
    private static final ResourceLocation LEGGINGS_MODEL = new ResourceLocation(Life_contract.MODID, "geo/soulforged_leggings.geo.json");
    private static final ResourceLocation BOOTS_MODEL = new ResourceLocation(Life_contract.MODID, "geo/soulforged_boots.geo.json");
    
    private static final ResourceLocation HELMET_TEXTURE = new ResourceLocation(Life_contract.MODID, "textures/armor/soulforged_helmet.png");
    private static final ResourceLocation CHESTPLATE_TEXTURE = new ResourceLocation(Life_contract.MODID, "textures/armor/soulforged_chestplate.png");
    private static final ResourceLocation LEGGINGS_TEXTURE = new ResourceLocation(Life_contract.MODID, "textures/armor/soulforged_leggings.png");
    private static final ResourceLocation BOOTS_TEXTURE = new ResourceLocation(Life_contract.MODID, "textures/armor/soulforged_boots.png");
    
    private static final ResourceLocation ANIMATION = new ResourceLocation(Life_contract.MODID, "animations/soulforged_armor.animation.json");
    
    @Override
    public ResourceLocation getModelResource(SoulforgedArmorItem animatable) {
        return switch (animatable.getType()) {
            case HELMET -> HELMET_MODEL;
            case CHESTPLATE -> CHESTPLATE_MODEL;
            case LEGGINGS -> LEGGINGS_MODEL;
            case BOOTS -> BOOTS_MODEL;
        };
    }
    
    @Override
    public ResourceLocation getTextureResource(SoulforgedArmorItem animatable) {
        return switch (animatable.getType()) {
            case HELMET -> HELMET_TEXTURE;
            case CHESTPLATE -> CHESTPLATE_TEXTURE;
            case LEGGINGS -> LEGGINGS_TEXTURE;
            case BOOTS -> BOOTS_TEXTURE;
        };
    }
    
    @Override
    public ResourceLocation getAnimationResource(SoulforgedArmorItem animatable) {
        return ANIMATION;
    }
}
