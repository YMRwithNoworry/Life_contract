package org.alku.life_contract.wraith_councilor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum SoulforgedArmorMaterial implements ArmorMaterial {
    INSTANCE;
    
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] DEFENSE_VALUES = {2, 5, 4, 2};
    
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getSlot().getIndex()] * 15;
    }
    
    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DEFENSE_VALUES[type.getSlot().getIndex()];
    }
    
    @Override
    public int getEnchantmentValue() {
        return 15;
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
        return "soulforged_council";
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
