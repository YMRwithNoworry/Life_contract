package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContractHUD {

    public static boolean isHudEnabled = true;

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "contract_status", HUD_OVERLAY);
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "mineral_generator", MINERAL_GENERATOR_OVERLAY);
    }

    public static final IGuiOverlay HUD_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isHudEnabled)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        int x = 10;
        int y = screenHeight / 2 - 20;
        int color = 0xFFFFFF;

        UUID myUUID = player.getUUID();
        ClientDataStorage.PlayerData myData = ClientDataStorage.get(myUUID);

        String selfMod = myData != null ? myData.contractMod
                : player.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
        UUID leaderUUID = myData != null ? myData.leaderUUID
                : (player.getPersistentData().hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)
                        ? player.getPersistentData().getUUID(TeamOrganizerItem.TAG_LEADER_UUID)
                        : null);
        int teamNumber = myData != null && myData.teamNumber != -1 ? myData.teamNumber
                : (player.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)
                        ? player.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER)
                        : -1);
        String selfProfessionId = myData != null ? myData.profession
                : player.getPersistentData().getString("LifeContractProfession");
        String selfProfessionName = getProfessionDisplayName(selfProfessionId);

        UUID myTeamUUID = (leaderUUID != null) ? leaderUUID : myUUID;

        guiGraphics.drawString(mc.font, "§e== 生灵契约 ==", x, y, color);
        y += 10;

        if (teamNumber != -1) {
            guiGraphics.drawString(mc.font, "§6队伍编号: §b" + teamNumber, x, y, color);
            y += 10;
        }

        if (!selfMod.isEmpty()) {
            guiGraphics.drawString(mc.font, "契约模组: §a" + selfMod, x, y, color);
            y += 10;
        } else {
            guiGraphics.drawString(mc.font, "契约模组: §7无", x, y, color);
            y += 10;
        }

        if (!selfProfessionName.isEmpty()) {
            guiGraphics.drawString(mc.font, "§6职业: §d" + selfProfessionName, x, y, color);
            y += 10;
            
            Profession profession = ProfessionConfig.getProfession(selfProfessionId);
            if (profession != null && profession.hasDiceAbility()) {
                boolean hasDice = hasGamblerDice(player);
                String diceStatus = hasDice ? "§a[骰子已装备]" : "§c[骰子缺失]";
                guiGraphics.drawString(mc.font, "§e赌徒骰子: " + diceStatus, x, y, color);
                y += 10;
            }
        }

        List<ClientDataStorage.PlayerData> teamMembers = new ArrayList<>();
        String myName = player.getName().getString();
        for (ClientDataStorage.PlayerData data : ClientDataStorage.PLAYER_DATA_CACHE.values()) {
            UUID theirTeamUUID = (data.leaderUUID != null) ? data.leaderUUID : data.playerUUID;
            
            if (myTeamUUID.equals(theirTeamUUID)) {
                teamMembers.add(data);
            }
        }

        if (!teamMembers.isEmpty()) {
            y += 5;
            guiGraphics.drawString(mc.font, "§6队友:", x, y, color);
            y += 10;
            for (ClientDataStorage.PlayerData memberData : teamMembers) {
                String memberName = memberData.playerName;
                String memberProfessionId = memberData.profession;
                String memberProfessionName = getProfessionDisplayName(memberProfessionId);
                String prefix = memberName.equals(myName) ? "§a● " : "§7- ";
                String displayText = prefix + memberName;
                if (!memberProfessionName.isEmpty()) {
                    displayText += " §d[" + memberProfessionName + "]";
                }
                guiGraphics.drawString(mc.font, displayText, x, y, memberName.equals(myName) ? 0x00FF00 : 0xAAAAAA);
                y += 10;
            }
        }
    };
    
    public static final IGuiOverlay MINERAL_GENERATOR_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null)
            return;

        int x = screenWidth - 10;
        int y = 10;
        int color = 0xFFFFFF;

        BlockPos playerPos = player.blockPosition();
        for (Map.Entry<BlockPos, ClientDataStorage.MineralGeneratorData> entry : ClientDataStorage.MINERAL_GENERATOR_CACHE.entrySet()) {
            BlockPos pos = entry.getKey();
            ClientDataStorage.MineralGeneratorData data = entry.getValue();
            
            if (pos.distSqr(playerPos) <= 100.0) {
                String mineralName = getMineralDisplayName(data.mineralType);
                String statusText;
                
                if (data.enabled) {
                    long currentClientTick = mc.level.getGameTime();
                    long tickDelta = currentClientTick - data.serverTick;
                    long ticksRemaining = Math.max(0, data.lastTick + data.interval * 20 - (data.serverTick + tickDelta));
                    int secondsRemaining = (int) Math.ceil(ticksRemaining / 20.0);
                    statusText = "§6" + mineralName + "\n§e下次产出: §a" + secondsRemaining + "§e秒";
                } else {
                    statusText = "§7" + mineralName + "\n§8已关闭";
                }
                
                String[] lines = statusText.split("\n");
                int lineHeight = mc.font.lineHeight;
                int totalHeight = lines.length * lineHeight;
                
                int textY = y;
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    int textWidth = mc.font.width(line);
                    guiGraphics.drawString(mc.font, line, x - textWidth, textY + i * lineHeight, color);
                }
                
                y += totalHeight + 10;
            }
        }
    };
    
    private static String getMineralDisplayName(String mineralType) {
        return switch (mineralType.toUpperCase()) {
            case "IRON" -> "铁锭";
            case "GOLD" -> "金锭";
            case "DIAMOND" -> "钻石";
            case "EMERALD" -> "绿宝石";
            default -> mineralType;
        };
    }
    
    private static String getProfessionDisplayName(String professionId) {
        if (professionId == null || professionId.isEmpty()) {
            return "";
        }
        Profession profession = ProfessionConfig.getProfession(professionId);
        return profession != null ? profession.getName() : "";
    }
    
    private static boolean hasGamblerDice(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Life_contract.GAMBLER_DICE.get()) {
                return true;
            }
        }
        return false;
    }
}
