package org.alku.life_contract.wraith_councilor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum PurpleHoodedSkullArmorMaterial implements ArmorMaterial {
    INSTANCE;
    
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] DEFENSE_VALUES = {3, 8, 6, 3};
    
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getSlot().getIndex()] * 25;
    }
    
    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DEFENSE_VALUES[type.getSlot().getIndex()];
    }
    
    @Override
    public int getEnchantmentValue() {
        return 20;
    }
    
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }
    
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
    
    @Override
    public String getName() {
        return "purple_hooded_skull";
    }
    
    @Override
    public float getToughness() {
        return 2.0F;
    }
    
    @Override
    public float getKnockbackResistance() {
        return 0.1F;
    }
}
