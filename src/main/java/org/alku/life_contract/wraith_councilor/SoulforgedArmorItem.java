package org.alku.life_contract.wraith_councilor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.List;
import java.util.function.Consumer;

public class SoulforgedArmorItem extends ArmorItem implements GeoItem {
    
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    
    public SoulforgedArmorItem(Type type, Properties properties) {
        super(SoulforgedArmorMaterial.INSTANCE, type, properties.stacksTo(1));
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    
    private PlayState predicate(AnimationState<SoulforgedArmorItem> event) {
        return PlayState.CONTINUE;
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;
            
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (renderer == null) {
                    renderer = new SoulforgedArmorRenderer();
                }
                renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return renderer;
            }
        });
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        String typeName = switch (getType()) {
            case HELMET -> "头部";
            case CHESTPLATE -> "胸甲";
            case LEGGINGS -> "护腿";
            case BOOTS -> "靴子";
        };
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§5魂铸议会套装 - " + typeName).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("套装效果:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 冥魂值上限 +40 点").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 暗系法术伤害 +20%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 免疫亡灵生物主动攻击").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("负面效果:").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("  §c- 阳光直射下: 最大生命值 -20%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §c- 阳光直射下: 护甲值 -30%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §c- 阳光直射下: 冥魂回复 -50%").withStyle(ChatFormatting.GRAY));
    }
}
