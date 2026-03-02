package org.alku.life_contract.byte_chen;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum DataTerminalArmorMaterial implements ArmorMaterial {
    INSTANCE;
    
    private static final int[] BASE_DURABILITY = {11, 13, 14, 10};
    private static final int[] DEFENSE_VALUES = {2, 4, 3, 1};
    
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getSlot().getIndex()] * 12;
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
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }
    
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
    
    @Override
    public String getName() {
        return "data_terminal";
    }
    
    @Override
    public float getToughness() {
        return 0.0F;
    }
    
    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
