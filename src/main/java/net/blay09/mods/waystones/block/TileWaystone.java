package net.blay09.mods.waystones.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileWaystone extends TileEntity {

    public static final int VARIANT_STONE = 0;
    public static final int VARIANT_SANDSTONE = 1;
    public static final int VARIANT_MOSSY = 2;

    private String waystoneName = "";
    private int variant = VARIANT_STONE;

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setString("WaystoneName", waystoneName);
        tagCompound.setInteger("Variant", variant);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        waystoneName = tagCompound.getString("WaystoneName");
        if (tagCompound.hasKey("Variant")) {
            setVariant(tagCompound.getInteger("Variant"));
        } else {
            variant = VARIANT_STONE;
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBT(tagCompound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tagCompound);
    }

    public String getWaystoneName() {
        return waystoneName;
    }

    public void setWaystoneName(String waystoneName) {
        this.waystoneName = waystoneName;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        if (variant == VARIANT_SANDSTONE) {
            this.variant = VARIANT_SANDSTONE;
        } else if (variant == VARIANT_MOSSY) {
            this.variant = VARIANT_MOSSY;
        } else {
            this.variant = VARIANT_STONE;
        }
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        markDirty();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }

}
