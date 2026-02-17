package net.blay09.mods.waystones.block;

import net.blay09.mods.waystones.WaystoneConfig;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockWaystone extends ItemBlock {

    public ItemBlockWaystone(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        if (WaystoneConfig.flatInventoryIcon && field_150939_a instanceof BlockWaystone) {
            IIcon icon = ((BlockWaystone) field_150939_a).getInventoryIcon();
            if (icon != null) {
                return icon;
            }
        }
        return super.getIconFromDamage(damage);
    }
}
