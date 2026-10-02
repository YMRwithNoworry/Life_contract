package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ContractHUD {
    private static final ResourceLocation HUD_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "contract_status");

    public static boolean isHudEnabled = true;

    private ContractHUD() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, HUD_ID, ContractHUD::renderHud);
    }

    private static void renderHud(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (!isHudEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        UUID myUUID = player.getUUID();
        ClientDataStorage.PlayerData myData = ClientDataStorage.get(myUUID);
        String selfMod = myData != null && !myData.contractMod.isBlank() ? myData.contractMod
                : player.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
        UUID leaderUUID = myData != null ? myData.leaderUUID
                : (player.getPersistentData().hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)
                        ? player.getPersistentData().getUUID(TeamOrganizerItem.TAG_LEADER_UUID) : null);
        int teamNumber = myData != null && myData.teamNumber != -1 ? myData.teamNumber
                : (player.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)
                        ? player.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER) : -1);
        UUID myTeamUUID = leaderUUID != null ? leaderUUID : myUUID;

        List<ClientDataStorage.PlayerData> teamMembers = new ArrayList<>();
        String myName = player.getName().getString();
        for (ClientDataStorage.PlayerData data : ClientDataStorage.PLAYER_DATA_CACHE.values()) {
            UUID theirTeamUUID = data.leaderUUID != null ? data.leaderUUID : data.playerUUID;
            if (myTeamUUID.equals(theirTeamUUID)) {
                teamMembers.add(data);
            }
        }

        int contentHeight = 12 + (teamNumber != -1 ? 10 : 0) + 15;
        if (!teamMembers.isEmpty()) {
            contentHeight += 10 + teamMembers.size() * 10;
        }
        int x = 10;
        int y = Math.max(4, (guiGraphics.guiHeight() - contentHeight) / 2);
        int color = 0xFFFFFF;

        guiGraphics.drawString(mc.font,
                Component.translatable("hud.life_contract.title").withStyle(ChatFormatting.YELLOW), x, y, color);
        y += 12;
        if (teamNumber != -1) {
            guiGraphics.drawString(mc.font, "§6队伍编号: §b" + teamNumber, x, y, color);
            y += 10;
        }
        Component contractLine = selfMod.isEmpty()
                ? Component.translatable("hud.life_contract.contract_mod_none").withStyle(ChatFormatting.GRAY)
                : Component.translatable("hud.life_contract.contract_mod")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(selfMod).withStyle(ChatFormatting.GREEN));
        guiGraphics.drawString(mc.font, contractLine, x, y, color);
        y += 15;

        if (!teamMembers.isEmpty()) {
            guiGraphics.drawString(mc.font, "§6队友:", x, y, color);
            y += 10;
            for (ClientDataStorage.PlayerData memberData : teamMembers) {
                String memberName = memberData.playerName;
                boolean self = memberName.equals(myName);
                guiGraphics.drawString(mc.font, (self ? "§a● " : "§7- ") + memberName, x, y,
                        self ? 0x00FF00 : 0xAAAAAA);
                y += 10;
            }
        }
    }
}
