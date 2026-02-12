package net.blay09.mods.waystones;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class EndWaystoneData extends WorldSavedData {

    private static final String DATA_NAME = "waystones_end";

    private boolean endSpikeWaystoneGenerated;
    private boolean worldSpawnWaystoneGenerated;

    public EndWaystoneData() {
        super(DATA_NAME);
    }

    public EndWaystoneData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        endSpikeWaystoneGenerated = nbt.getBoolean("EndSpikeWaystoneGenerated");
        worldSpawnWaystoneGenerated = nbt.getBoolean("WorldSpawnWaystoneGenerated");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("EndSpikeWaystoneGenerated", endSpikeWaystoneGenerated);
        nbt.setBoolean("WorldSpawnWaystoneGenerated", worldSpawnWaystoneGenerated);
    }

    public boolean isEndSpikeWaystoneGenerated() {
        return endSpikeWaystoneGenerated;
    }

    public void setEndSpikeWaystoneGenerated(boolean generated) {
        endSpikeWaystoneGenerated = generated;
        markDirty();
    }

    public boolean isWorldSpawnWaystoneGenerated() {
        return worldSpawnWaystoneGenerated;
    }

    public void setWorldSpawnWaystoneGenerated(boolean generated) {
        worldSpawnWaystoneGenerated = generated;
        markDirty();
    }

    public static EndWaystoneData get(World world) {
        EndWaystoneData data = (EndWaystoneData) world.mapStorage.loadData(EndWaystoneData.class, DATA_NAME);
        if (data == null) {
            data = new EndWaystoneData();
            world.mapStorage.setData(DATA_NAME, data);
        }
        return data;
    }
}
