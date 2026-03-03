package org.alku.life_contract.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.alku.life_contract.follower.FollowerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.SpawnEggItem.class)
public class SpawnEggItemMixin {

    @Inject(method = "spawnOffspringFromSpawnEgg", at = @At("RETURN"))
    private void onSpawnOffspringFromSpawnEgg(
            Player player,
            Mob source,
            EntityType<?> entityType,
            ServerLevel level,
            Vec3 pos,
            ItemStack stack,
            CallbackInfoReturnable<Entity> cir) {
        
        Entity entity = cir.getReturnValue();
        if (entity instanceof Mob mob && player != null) {
            CompoundTag itemTag = stack.getTag();
            if (itemTag != null && itemTag.contains("EggShopFollower") && itemTag.getBoolean("EggShopFollower")) {
                FollowerEvents.registerFollower(mob, player.getUUID());
            }
        }
    }
}
