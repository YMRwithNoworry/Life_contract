package org.alku.life_contract.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StrongholdStructure.class)
public abstract class StrongholdStructureMixin {
    @Unique
    private static final int LIFE_CONTRACT_PORTAL_Y = -90;

    @SuppressWarnings("deprecation")
    @Redirect(
            method = "generatePieces",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePiecesBuilder;moveBelowSeaLevel(IILnet/minecraft/util/RandomSource;I)I"),
            require = 1)
    private static int lifeContract$movePortalToDeepLayer(
            StructurePiecesBuilder builder,
            int seaLevel,
            int minY,
            RandomSource random,
            int margin) {
        StrongholdPieces.PortalRoom portalRoom = builder.build().pieces().stream()
                .filter(StrongholdPieces.PortalRoom.class::isInstance)
                .map(StrongholdPieces.PortalRoom.class::cast)
                .findFirst()
                .orElse(null);
        if (portalRoom == null) {
            return builder.moveBelowSeaLevel(seaLevel, minY, random, margin);
        }

        BoundingBox portalBounds = portalRoom.getBoundingBox();
        int portalFrameY = portalBounds.minY() + 3;
        int offset = LIFE_CONTRACT_PORTAL_Y - portalFrameY;
        builder.offsetPiecesVertically(offset);
        return offset;
    }
}
