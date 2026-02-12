package net.blay09.mods.waystones.mixins.early.minecraft;

import java.util.LinkedList;
import java.util.Random;

import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.util.WaystoneEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureNetherBridgePieces;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class MixinMapGenNetherBridgeStart {

    private static final String STRUCTURE_ID = "fortress";

    @Shadow
    protected LinkedList<StructureComponent> components;

    @Unique
    private boolean waystones$hasPlacedWaystone = false;
    @Unique
    private boolean waystones$hasResolvedWaystone = false;

    @Inject(method = "func_143022_a", at = @At("TAIL"))
    private void waystones$writeNBT(NBTTagCompound nbt, CallbackInfo ci) {
        if (!((Object) this instanceof MapGenNetherBridge.Start)) return;
        nbt.setBoolean("WaystonePlaced", waystones$hasPlacedWaystone);
        nbt.setBoolean("WaystoneResolved", waystones$hasResolvedWaystone);
    }

    @Inject(method = "func_143017_b", at = @At("TAIL"))
    private void waystones$readNBT(NBTTagCompound nbt, CallbackInfo ci) {
        if (!((Object) this instanceof MapGenNetherBridge.Start)) return;
        waystones$hasPlacedWaystone = nbt.getBoolean("WaystonePlaced");
        waystones$hasResolvedWaystone = nbt.getBoolean("WaystoneResolved");
    }

    @Inject(method = "generateStructure", at = @At("TAIL"))
    private void waystones$placeWaystone(World world, Random rand, StructureBoundingBox box, CallbackInfo ci) {
        if (!((Object) this instanceof MapGenNetherBridge.Start)) return;
        if (waystones$hasResolvedWaystone || world.isRemote) return;

        for (Object obj : this.components) {
            if (!(obj instanceof StructureNetherBridgePieces.Start)) {
                continue;
            }

            StructureComponent start = (StructureComponent) obj;
            if (!start.getBoundingBox()
                .intersectsWith(box)) {
                continue;
            }

            AccessorStructureComponent accessor = (AccessorStructureComponent) start;
            // Center of fortress start crossing3 room.
            int localX = 9;
            int localY = 5;
            int localZ = 9;

            int x = accessor.callGetXWithOffset(localX, localZ);
            int y = accessor.callGetYWithOffset(localY);
            int z = accessor.callGetZWithOffset(localX, localZ);

            if (!box.isVecInside(x, y, z) || !box.isVecInside(x, y + 1, z)) {
                continue;
            }

            // We reached the definitive spawn location for this fortress start
            // resolve once so chance isn't re-rolled on later chunk passes
            waystones$hasResolvedWaystone = true;

            int biomeId = world.getBiomeGenForCoords(x, z).biomeID;
            int variant = Waystones.varInstanceCommon
                .resolveStructureWaystoneVariant(STRUCTURE_ID, TileWaystone.VARIANT_STONE, world, biomeId, rand);
            if (variant < 0) {
                return;
            }

            int fortressHash = waystones$getFortressHash(world);
            Waystones.debug(
                "Spawned fortress waystone at " + x
                    + " "
                    + y
                    + " "
                    + z
                    + " fortressHash="
                    + Integer.toHexString(fortressHash));

            if (variant == TileWaystone.VARIANT_MOSSY) {
                world.setBlock(x, y, z, Waystones.blockWaystoneMossy, 2, 2);
                world.setBlock(x, y + 1, z, Waystones.blockWaystoneMossy, ForgeDirection.UNKNOWN.ordinal(), 2);
            } else if (variant == TileWaystone.VARIANT_SANDSTONE) {
                world.setBlock(x, y, z, Waystones.blockWaystoneSandstone, 2, 2);
                world.setBlock(x, y + 1, z, Waystones.blockWaystoneSandstone, ForgeDirection.UNKNOWN.ordinal(), 2);
            } else {
                world.setBlock(x, y, z, Waystones.blockWaystone, 2, 2);
                world.setBlock(x, y + 1, z, Waystones.blockWaystone, ForgeDirection.UNKNOWN.ordinal(), 2);
            }

            TileWaystone tile = (TileWaystone) world.getTileEntity(x, y, z);
            if (tile == null) {
                return;
            }

            tile.setVariant(variant);

            String resolvedName = Waystones.varInstanceCommon.resolveStructureWaystoneName(STRUCTURE_ID, null);
            if (resolvedName != null && !resolvedName.isEmpty()) {
                tile.setWaystoneName(resolvedName);
            }

            if (Waystones.varInstanceCommon.shouldForceGlobalStructureWaystone(STRUCTURE_ID)
                && tile.getWaystoneName() != null
                && !tile.getWaystoneName()
                    .isEmpty()) {
                if (Waystones.varInstanceCommon.shouldAutoActivateGlobalStructureWaystone(STRUCTURE_ID)) {
                    WaystoneManager.addServerWaystone(new WaystoneEntry(tile));
                } else {
                    tile.setForceGlobalOnActivation(true);
                }
            }

            waystones$hasPlacedWaystone = true;
            return;
        }
    }

    @Unique
    private int waystones$getFortressHash(World world) {
        StructureStart start = (StructureStart) (Object) this;
        int dim = world.provider.dimensionId;
        int chunkX = start.func_143019_e();
        int chunkZ = start.func_143018_f();

        int hash = 17;
        hash = 31 * hash + dim;
        hash = 31 * hash + chunkX;
        hash = 31 * hash + chunkZ;
        return hash;
    }
}
