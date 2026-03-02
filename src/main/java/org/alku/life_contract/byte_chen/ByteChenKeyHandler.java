package org.alku.life_contract.byte_chen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ByteChenKeyHandler {

    public static final KeyMapping DEPLOY_SCOUT_NODE = new KeyMapping(
        "key.life_contract.deploy_scout_node",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        "key.categories.life_contract"
    );

    public static final KeyMapping DEPLOY_BUFF_NODE = new KeyMapping(
        "key.life_contract.deploy_buff_node",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        "key.categories.life_contract"
    );

    public static final KeyMapping DEPLOY_COUNTER_NODE = new KeyMapping(
        "key.life_contract.deploy_counter_node",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        "key.categories.life_contract"
    );

    public static final KeyMapping FULL_READ = new KeyMapping(
        "key.life_contract.full_read",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "key.categories.life_contract"
    );

    public static final KeyMapping DATA_DISPATCH = new KeyMapping(
        "key.life_contract.data_dispatch",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_B,
        "key.categories.life_contract"
    );

    public static final KeyMapping DATA_BAN = new KeyMapping(
        "key.life_contract.data_ban",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        "key.categories.life_contract"
    );

    public static final KeyMapping ULTIMATE = new KeyMapping(
        "key.life_contract.byte_chen_ultimate",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        "key.categories.life_contract"
    );

    public static final KeyMapping RECYCLE_NODES = new KeyMapping(
        "key.life_contract.recycle_nodes",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_COMMA,
        "key.categories.life_contract"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DEPLOY_SCOUT_NODE);
        event.register(DEPLOY_BUFF_NODE);
        event.register(DEPLOY_COUNTER_NODE);
        event.register(FULL_READ);
        event.register(DATA_DISPATCH);
        event.register(DATA_BAN);
        event.register(ULTIMATE);
        event.register(RECYCLE_NODES);
    }

    @Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
    public static class ClientTickHandler {
        private static boolean scoutNodePressed = false;
        private static boolean buffNodePressed = false;
        private static boolean counterNodePressed = false;
        private static boolean fullReadPressed = false;
        private static boolean dataDispatchPressed = false;
        private static boolean dataBanPressed = false;
        private static boolean ultimatePressed = false;
        private static boolean recyclePressed = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            Profession profession = ClientProfessionCache.getCurrentProfession();
            if (profession == null || !profession.isByteChen()) return;

            if (DEPLOY_SCOUT_NODE.isDown() && !scoutNodePressed) {
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_DEPLOY_SCOUT));
                scoutNodePressed = true;
            } else if (!DEPLOY_SCOUT_NODE.isDown()) {
                scoutNodePressed = false;
            }

            if (DEPLOY_BUFF_NODE.isDown() && !buffNodePressed) {
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_DEPLOY_BUFF));
                buffNodePressed = true;
            } else if (!DEPLOY_BUFF_NODE.isDown()) {
                buffNodePressed = false;
            }

            if (DEPLOY_COUNTER_NODE.isDown() && !counterNodePressed) {
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_DEPLOY_COUNTER));
                counterNodePressed = true;
            } else if (!DEPLOY_COUNTER_NODE.isDown()) {
                counterNodePressed = false;
            }

            if (FULL_READ.isDown() && !fullReadPressed) {
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_FULL_READ));
                fullReadPressed = true;
            } else if (!FULL_READ.isDown()) {
                fullReadPressed = false;
            }

            if (DATA_DISPATCH.isDown() && !dataDispatchPressed) {
                boolean shiftDown = mc.options.keyShift.isDown();
                int mode = shiftDown ? 1 : 0;
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_DATA_DISPATCH, -1, mode));
                dataDispatchPressed = true;
            } else if (!DATA_DISPATCH.isDown()) {
                dataDispatchPressed = false;
            }

            if (DATA_BAN.isDown() && !dataBanPressed) {
                int targetId = getTargetEntityId(mc, player);
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_DATA_BAN, targetId));
                dataBanPressed = true;
            } else if (!DATA_BAN.isDown()) {
                dataBanPressed = false;
            }

            if (ULTIMATE.isDown() && !ultimatePressed) {
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_ULTIMATE));
                ultimatePressed = true;
            } else if (!ULTIMATE.isDown()) {
                ultimatePressed = false;
            }

            if (RECYCLE_NODES.isDown() && !recyclePressed) {
                NetworkHandler.CHANNEL.sendToServer(new PacketByteChenSkill(PacketByteChenSkill.SKILL_RECYCLE_NODES));
                recyclePressed = true;
            } else if (!RECYCLE_NODES.isDown()) {
                recyclePressed = false;
            }
        }

        private static int getTargetEntityId(Minecraft mc, LocalPlayer player) {
            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof LivingEntity) {
                    return entity.getId();
                }
            }

            double range = 12.0;
            AABB searchBox = player.getBoundingBox().inflate(range);
            LivingEntity nearestTarget = null;
            double nearestDistance = range;

            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
                if (entity == player) continue;
                double distance = player.distanceTo(entity);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestTarget = entity;
                }
            }

            return nearestTarget != null ? nearestTarget.getId() : -1;
        }
    }
}
