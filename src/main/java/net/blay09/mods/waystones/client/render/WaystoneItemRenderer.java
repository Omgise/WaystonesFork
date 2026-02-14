package net.blay09.mods.waystones.client.render;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.block.BlockWaystone;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class WaystoneItemRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        if (type != ItemRenderType.INVENTORY || !WaystoneConfig.flatInventoryIcon) {
            return false;
        }
        Block block = Block.getBlockFromItem(item.getItem());
        return block instanceof BlockWaystone && ((BlockWaystone) block).getInventoryIcon() != null;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Block block = Block.getBlockFromItem(item.getItem());
        IIcon icon = ((BlockWaystone) block).getInventoryIcon();
        // Block atlas already bound by ForgeHooksClient
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(0, 16, 0, icon.getMinU(), icon.getMaxV());
        t.addVertexWithUV(16, 16, 0, icon.getMaxU(), icon.getMaxV());
        t.addVertexWithUV(16, 0, 0, icon.getMaxU(), icon.getMinV());
        t.addVertexWithUV(0, 0, 0, icon.getMinU(), icon.getMinV());
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }
}
