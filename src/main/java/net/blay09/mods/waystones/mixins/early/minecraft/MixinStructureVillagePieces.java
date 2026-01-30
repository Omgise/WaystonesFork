package net.blay09.mods.waystones.mixins.early.minecraft;

import java.util.List;
import java.util.Random;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.worldgen.VillageWaystone;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Tries to ensure exactly one spawned Waystone per village
@Mixin(StructureVillagePieces.class)
public class MixinStructureVillagePieces {

    @Inject(method = "getNextVillageComponent", at = @At("HEAD"), cancellable = true)
    private static void prioritizeWaystone(StructureVillagePieces.Start start, List pieces, Random rand, int x, int y,
        int z, int coordBaseMode, int type, CallbackInfoReturnable<StructureVillagePieces.Village> cir) {
        Waystones.debug("MixinStructureVillagePieces hook");
        for (StructureVillagePieces.PieceWeight pw : start.structureVillageWeightedPieceList) {
            // Waystones.debug(pw.villagePieceClass.getCanonicalName());
            if (pw.villagePieceClass == VillageWaystone.VillageWaystonePiece.class && pw.villagePiecesSpawned == 0) {
                StructureVillagePieces.Village village = VillageWaystone.VillageWaystonePiece
                    .buildComponent(start, pieces, rand, x, y, z, coordBaseMode, type);
                if (village != null) {
                    pw.villagePiecesSpawned++;
                    start.structureVillageWeightedPieceList.remove(pw);
                    cir.setReturnValue(village);
                }
                return;
            }
        }
    }
}
