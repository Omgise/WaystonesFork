package net.blay09.mods.waystones.worldgen;

import java.util.List;
import java.util.Random;

import net.blay09.mods.waystones.Waystones;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class VillageWaystone extends StructureVillagePieces.Village {

    public VillageWaystone() {}

    public VillageWaystone(StructureVillagePieces.Start start, int type, Random rand, StructureBoundingBox box,
        int coordBaseMode) {
        super(start, type);
        this.coordBaseMode = coordBaseMode;
        this.boundingBox = box;
    }

    public static VillageWaystone buildComponent(StructureVillagePieces.Start start, List pieces, Random rand, int x,
        int y, int z, int coordBaseMode, int type) {
        StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(
            x,
            y,
            z,
            0,
            0,
            0,
            5,
            6,
            5, // size of structure
            coordBaseMode);

        if (!canVillageGoDeeper(box)) return null;
        if (StructureComponent.findIntersecting(pieces, box) != null) return null;

        return new VillageWaystone(start, type, rand, box, coordBaseMode);
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {

        if (this.field_143015_k < 0) {
            this.field_143015_k = this.getAverageGroundLevel(world, box);
            if (this.field_143015_k < 0) return true;
            this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.minY, 0);
        }

        // Clear area
        this.fillWithAir(world, box, 0, 0, 0, 4, 5, 4);

        // Base
        this.fillWithBlocks(world, box, 0, 0, 0, 4, 0, 4, Blocks.cobblestone, Blocks.cobblestone, false);

        // Waystone block in center
        this.placeBlockAtCurrentPosition(world, GameRegistry.findBlock(Waystones.MOD_ID, "waystone"), 0, 2, 1, 2, box);

        return true;
    }

    public static class CreationHandler implements VillagerRegistry.IVillageCreationHandler {

        @Override
        public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int villageSize) {
            return new StructureVillagePieces.PieceWeight(
                VillageWaystone.class,
                99999, // weight (lower = rarer)
                1 // max per village
            );
        }

        @Override
        public Class<?> getComponentClass() {
            return VillageWaystone.class;
        }

        @Override
        public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight pieceWeight,
            StructureVillagePieces.Start start, List pieces, Random random, int x, int y, int z, int coordBaseMode,
            int componentType) {
            return VillageWaystone.buildComponent(start, pieces, random, x, y, z, coordBaseMode, componentType);
        }
    }
}
