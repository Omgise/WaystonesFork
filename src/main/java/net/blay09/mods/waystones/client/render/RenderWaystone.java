package net.blay09.mods.waystones.client.render;

import java.nio.DoubleBuffer;

import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class RenderWaystone extends TileEntitySpecialRenderer {

    private static final ResourceLocation texture = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/waystone.png");
    private static final ResourceLocation textureSandstone = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/sandstone.png");
    private static final ResourceLocation textureMossy = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/mossy.png");
    private static final ResourceLocation textureActive = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/waystone_active.png");

    private static final DoubleBuffer clipPlaneBuffer = BufferUtils.createDoubleBuffer(4);

    private final ModelWaystone model = new ModelWaystone();

    float getCooldownProgress(TileWaystone tileWaystone) {
        if (Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode || !WaystoneConfig.showCooldownOnWaystone) {
            return 1f;
        }
        if (!tileWaystone.hasWorldObj()) {
            return 1f; // fully charged if not in world
        }

        long lastUse = PlayerWaystoneData.getLastWarpStoneUse(Minecraft.getMinecraft().thePlayer);
        long cooldown = Waystones.getConfig().warpStoneCooldown * 1000L;
        long timeSince = System.currentTimeMillis() - lastUse;
        return Math.min(1f, Math.max(0f, (float) timeSince / cooldown));
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        TileWaystone tileWaystone = (TileWaystone) tileEntity;
        boolean stoneIsKnown = WaystoneManager.getKnownWaystone(tileWaystone.getWaystoneName()) != null
            || WaystoneManager.getServerWaystone(tileWaystone.getWaystoneName()) != null;
        boolean stoneIsGlobal = WaystoneManager.getServerWaystone(tileWaystone.getWaystoneName()) != null
            && WaystoneManager.getServerWaystone(tileWaystone.getWaystoneName())
                .isGlobal();
        if (tileWaystone.getVariant() == TileWaystone.VARIANT_SANDSTONE) {
            bindTexture(textureSandstone);
        } else if (tileWaystone.getVariant() == TileWaystone.VARIANT_MOSSY) {
            bindTexture(textureMossy);
        } else {
            bindTexture(texture);
        }

        float angle = tileEntity.hasWorldObj()
            ? WaystoneManager.getRotationYaw(ForgeDirection.getOrientation(tileEntity.getBlockMetadata()))
            : 0f;
        final float prevBrightX = OpenGlHelper.lastBrightnessX;
        final float prevBrightY = OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glTranslated(x + 0.5, y, z + 0.5);
            GL11.glRotatef(angle, 0f, 1f, 0f);
            GL11.glRotatef(-180f, 1f, 0f, 0f);
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            model.renderAll();
            if (tileWaystone.hasWorldObj() && stoneIsKnown) {
                GL11.glDisable(GL11.GL_CULL_FACE); // render all faces

                // Render active pillar overlay with emissive glow, clipped by cooldown progress
                float progress = getCooldownProgress(tileWaystone);
                if (progress > 0f) {
                    float glowIntensity = progress * WaystoneConfig.overlayGlowIntensity;

                    // Emissive rendering: disable lighting and lightmap so symbols
                    // render at constant brightness regardless of ambient light
                    GL11.glDisable(GL11.GL_LIGHTING);
                    Minecraft.getMinecraft().entityRenderer.disableLightmap(0);

                    bindTexture(textureActive);
                    GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                    GL11.glPolygonOffset(-1.0f, -1.0f);

                    // Glow blend: overlay adds light to the underlying stone
                    // alpha=1 symbols add (intensity,intensity,intensity), alpha=0 areas add nothing
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                    GL11.glColor4f(glowIntensity, glowIntensity, glowIntensity, 1f);

                    // Pillar model Y ranges from -3.0 (visual top) to -1.125 (visual bottom)
                    // Clip plane reveals from bottom to top as progress goes 0 -> 1
                    float pillarBottom = -18f * 0.0625f;
                    float pillarTop = -48f * 0.0625f;
                    float clipY = pillarBottom + progress * (pillarTop - pillarBottom);

                    clipPlaneBuffer.clear();
                    clipPlaneBuffer.put(0.0)
                        .put(1.0)
                        .put(0.0)
                        .put((double) -clipY);
                    clipPlaneBuffer.flip();
                    GL11.glClipPlane(GL11.GL_CLIP_PLANE0, clipPlaneBuffer);
                    GL11.glEnable(GL11.GL_CLIP_PLANE0);

                    model.renderPillar();

                    GL11.glDisable(GL11.GL_CLIP_PLANE0);
                    GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glColor4f(1f, 1f, 1f, 1f);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
                }

                GL11.glEnable(GL11.GL_CULL_FACE);
            }
            GL11.glDisable(GL11.GL_BLEND);
        } finally {
            Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevBrightX, prevBrightY);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        if (WaystoneConfig.showNametag && tileWaystone.hasWorldObj() && stoneIsKnown) {
            renderWaystoneName(tileWaystone, x + 0.5, y + 2.5, z + 0.5, stoneIsGlobal);
        }
    }

    private void renderWaystoneName(TileWaystone tile, double x, double y, double z, boolean isGlobal) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String name = (isGlobal ? EnumChatFormatting.YELLOW : "") + tile.getWaystoneName();

        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_TEXTURE_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x, y, z);

            // Face the player
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

            float scale = 0.01666667F * 1.6F; // adjust size
            GL11.glScalef(-scale, -scale, scale);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            int width = fontRenderer.getStringWidth(name) / 2;
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.setColorRGBA_F(0f, 0f, 0f, 0.25f);
            tess.addVertex(-width - 1, -1, 0);
            tess.addVertex(-width - 1, 8, 0);
            tess.addVertex(width + 1, 8, 0);
            tess.addVertex(width + 1, -1, 0);
            tess.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // Vanilla-like two-pass text: through-walls darker pass, then normal pass.
            fontRenderer.drawString(name, -width, 0, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            fontRenderer.drawString(name, -width, 0, 0xFFFFFF);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
        } finally {
            GL11.glPopMatrix();
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glPopAttrib();
        }
    }
}
