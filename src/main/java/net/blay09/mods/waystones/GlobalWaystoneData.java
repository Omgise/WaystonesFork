package net.blay09.mods.waystones;

import net.blay09.mods.waystones.util.WaystoneEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class GlobalWaystoneData extends WorldSavedData {

    private static final String DATA_NAME = "waystones_global";

    public GlobalWaystoneData() {
        super(DATA_NAME);
    }

    public GlobalWaystoneData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("GlobalWaystones", Constants.NBT.TAG_COMPOUND);
        WaystoneEntry[] entries = new WaystoneEntry[list.tagCount()];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = WaystoneEntry.read(list.getCompoundTagAt(i));
            entries[i].setGlobal(true);
        }
        WaystoneManager.setServerWaystones(entries);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (WaystoneEntry entry : WaystoneManager.getServerWaystones()) {
            list.appendTag(entry.writeToNBT());
        }
        nbt.setTag("GlobalWaystones", list);
    }

    public static GlobalWaystoneData get(World world) {
        GlobalWaystoneData data = (GlobalWaystoneData) world.mapStorage.loadData(GlobalWaystoneData.class, DATA_NAME);
        if (data == null) {
            data = new GlobalWaystoneData();
            world.mapStorage.setData(DATA_NAME, data);
        }
        return data;
    }

    public static void save(World world) {
        GlobalWaystoneData data = get(world);
        data.markDirty();
    }
}
