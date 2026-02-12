package net.blay09.mods.waystones.util;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.entity.player.EntityPlayer;

public class WaystoneXpCost {

    public static int getXpCost(EntityPlayer player, WaystoneEntry to) {
        if (Waystones.getConfig().xpBaseCost < 0) {
            return -1;
        }
        int base = Waystones.getConfig().xpBaseCost;
        int blocksPerLevel = Waystones.getConfig().xpBlocksPerLevel;

        if (blocksPerLevel <= 0) {
            return base;
        }

        // Cross-dimension cost
        if (player.worldObj.provider.dimensionId != to.getDimensionId()) {
            return base + Waystones.getConfig().xpCrossDimCost;
        }

        double dx = player.posX - to.getPos()
            .getX();
        double dy = player.posY - to.getPos()
            .getY();
        double dz = player.posZ - to.getPos()
            .getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return base + (int) Math.floor(distance / blocksPerLevel);
    }

    public static WaystoneEntry entryFromTile(TileWaystone tile, EntityPlayer player) {
        WaystoneEntry[] entries = WaystoneEntry.getCombinedWaystones(player, false);
        for (WaystoneEntry w : entries) {
            if (WaystoneEntry.tileAndEntryShareCoords(w, tile)) {
                return w;
            }
        }
        return null;
    }

    public static int getXpCost(TileWaystone from, WaystoneEntry to, EntityPlayer player) {
        if (Waystones.getConfig().xpBaseCost < 0) {
            return -1;
        }
        if (from == null) {
            return getXpCost(player, to);
        }
        int base = Waystones.getConfig().xpBaseCost;
        int blocksPerLevel = Waystones.getConfig().xpBlocksPerLevel;

        if (blocksPerLevel <= 0) {
            return base;
        }

        // Cross-dimension cost
        WaystoneEntry fromEntry = entryFromTile(from, player);
        if (fromEntry != null) {
            if (fromEntry.getDimensionId() != to.getDimensionId()) {
                return base + Waystones.getConfig().xpCrossDimCost;
            }
        }

        double dx = from.xCoord - to.getPos()
            .getX();
        double dy = from.yCoord - to.getPos()
            .getY();
        double dz = from.zCoord - to.getPos()
            .getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return base + (int) Math.floor(distance / blocksPerLevel);
    }
}
