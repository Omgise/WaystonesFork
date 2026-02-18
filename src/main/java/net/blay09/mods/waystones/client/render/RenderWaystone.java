package net.blay09.mods.waystones.client.render;

import java.nio.DoubleBuffer;
import java.util.Random;

import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.varinstances.VarInstanceClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
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
    private static final ResourceLocation textureStonebrick = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/stonebrick.png");
    private static final ResourceLocation textureMossyStonebrick = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/mossystonebrick.png");
    private static final ResourceLocation textureNether = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/netherbrick.png");
    private static final ResourceLocation textureEnd = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/endstone.png");
    private static final ResourceLocation textureActive = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/waystone_active.png");
    private static final ResourceLocation textureActiveSandstone = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/sandstone_active.png");
    private static final ResourceLocation textureActiveMossy = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/mossy_active.png");
    private static final ResourceLocation textureActiveStonebrick = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/stonebrick_active.png");
    private static final ResourceLocation textureActiveNether = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/netherbrick_active.png");
    private static final ResourceLocation textureActiveEnd = new ResourceLocation(
        Waystones.MODID,
        "textures/entity/endstone_active.png");

    private static final float LAVA_TEXTURE_SCALE = 1.0f;
    private static final float LAVA_TEXTURE_X_OFFSET = 0f;
    private static final float LAVA_TEXTURE_Y_OFFSET = 0f;

    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random END_PORTAL_RANDOM = new Random(31100L);

    private static final DoubleBuffer clipPlaneBuffer = BufferUtils.createDoubleBuffer(4);
    private final ModelWaystone model = new ModelWaystone();
    private static int waystones$stencilTag = 1;

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
        bindTexture(getBaseTexture(tileWaystone.getVariant()));

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
                    float glowIntensity = 1f;

                    // Emissive rendering: disable lighting and lightmap so symbols
                    // render at constant brightness regardless of ambient light
                    GL11.glDisable(GL11.GL_LIGHTING);
                    Minecraft.getMinecraft().entityRenderer.disableLightmap(0);

                    bindTexture(getOverlayTexture(tileWaystone.getVariant()));
                    GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                    GL11.glPolygonOffset(-1.0f, -1.0f);
                    GL11.glDepthFunc(GL11.GL_LEQUAL);

                    // Glow blend: overlay adds light to the underlying stone
                    // alpha=1 symbols add (intensity,intensity,intensity), alpha=0 areas add nothing
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                    GL11.glColor4f(glowIntensity, glowIntensity, glowIntensity, 1f);

                    // Clip plane reveals from bottom to top as progress goes 0 -> 1
                    VarInstanceClient.OverlayClipBounds clipBounds = Waystones.varInstanceClient
                        .getOverlayClipBounds(tileWaystone.getVariant());
                    float pillarBottom = clipBounds.lower;
                    float pillarTop = clipBounds.upper;
                    float clipY = pillarBottom + progress * (pillarTop - pillarBottom);

                    clipPlaneBuffer.clear();
                    clipPlaneBuffer.put(0.0)
                        .put(1.0)
                        .put(0.0)
                        .put((double) -clipY);
                    clipPlaneBuffer.flip();
                    GL11.glClipPlane(GL11.GL_CLIP_PLANE0, clipPlaneBuffer);
                    GL11.glEnable(GL11.GL_CLIP_PLANE0);

                    int variant = tileWaystone.getVariant();
                    if (variant == TileWaystone.VARIANT_NETHER) {
                        renderNetherLavaOverlay(glowIntensity);
                    } else if (variant == TileWaystone.VARIANT_END) {
                        renderEndPortalOverlay(glowIntensity);
                    } else {
                        model.renderPillar();
                    }

                    GL11.glDisable(GL11.GL_CLIP_PLANE0);
                    GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
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

    private void renderNetherLavaOverlay(float glowIntensity) {
        int tag = waystones$stencilTag++;
        if (waystones$stencilTag > 255) {
            waystones$stencilTag = 1;
        }

        // Pass 1: write overlay alpha shape into stencil using the regular overlay UVs
        bindTexture(textureActiveNether);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, tag, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_BLEND);
        model.renderPillar();

        // Pass 2: draw animated lava only where the stencil mask was written
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, tag, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(glowIntensity, glowIntensity, glowIntensity, 1f);
        setupLavaTextureUvMapping();
        model.renderPillar();
        cleanupLavaTextureUvMapping();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    private void setupLavaTextureUvMapping() {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);

        IIcon lavaIcon = Blocks.lava.getIcon(0, 0);
        float minU = lavaIcon.getMinU();
        float maxU = lavaIcon.getMaxU();
        float minV = lavaIcon.getMinV();
        float maxV = lavaIcon.getMaxV();
        float du = maxU - minU;
        float dv = maxV - minV;

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        // Use the same model UVs as the overlay, then remap to the lava atlas tile
        GL11.glTranslatef(minU, minV, 0f);
        GL11.glScalef(du, dv, 1f);
        GL11.glTranslatef(LAVA_TEXTURE_X_OFFSET, LAVA_TEXTURE_Y_OFFSET, 0f);
        float invScale = 1.0f / Math.max(0.0001f, LAVA_TEXTURE_SCALE);
        GL11.glScalef(invScale, invScale, 1f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private static void cleanupLavaTextureUvMapping() {
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void renderEndPortalOverlay(float glowIntensity) {
        int tag = waystones$stencilTag++;
        if (waystones$stencilTag > 255) {
            waystones$stencilTag = 1;
        }

        // Pass 1: write overlay alpha shape into stencil
        bindTexture(textureActiveEnd);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, tag, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_BLEND);
        model.renderPillar();

        // Pass 2: draw end portal layers only where the stencil was written
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, tag, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        END_PORTAL_RANDOM.setSeed(31100L);

        for (int i = 0; i < 16; i++) {
            float layerDepth = (float) (16 - i);
            float scale = i == 0 ? 0.125f : 0.5f;
            float brightness = 1.0f / (layerDepth + 1.0f);

            if (i == 0) {
                bindTexture(END_SKY_TEXTURE);
                brightness = 0.1f;
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
            if (i == 1) {
                bindTexture(END_PORTAL_TEXTURE);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            }

            float r = END_PORTAL_RANDOM.nextFloat() * 0.5f + 0.1f;
            float g = END_PORTAL_RANDOM.nextFloat() * 0.5f + 0.4f;
            float b = END_PORTAL_RANDOM.nextFloat() * 0.5f + 0.5f;
            if (i == 0) {
                r = g = b = 1.0f;
            }

            GL11.glColor4f(
                r * brightness * glowIntensity,
                g * brightness * glowIntensity,
                b * brightness * glowIntensity,
                1.0f);

            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0f, (float) (Minecraft.getSystemTime() % 700000L) / 700000.0f, 0.0f);
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.5f, 0.5f, 0.0f);
            GL11.glRotatef((float) (i * i * 4321 + i * 9) * 2.0f, 0.0f, 0.0f, 1.0f);
            GL11.glTranslatef(-0.5f, -0.5f, 0.0f);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);

            model.renderPillar();

            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }

        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    private static ResourceLocation getBaseTexture(int variant) {
        switch (variant) {
            case TileWaystone.VARIANT_SANDSTONE:
                return textureSandstone;
            case TileWaystone.VARIANT_MOSSY:
                return textureMossy;
            case TileWaystone.VARIANT_STONEBRICK:
                return textureStonebrick;
            case TileWaystone.VARIANT_MOSSY_STONEBRICK:
                return textureMossyStonebrick;
            case TileWaystone.VARIANT_NETHER:
                return textureNether;
            case TileWaystone.VARIANT_END:
                return textureEnd;
            default:
                return texture;
        }
    }

    private static ResourceLocation getOverlayTexture(int variant) {
        switch (variant) {
            case TileWaystone.VARIANT_SANDSTONE:
                return textureActiveSandstone;
            case TileWaystone.VARIANT_MOSSY:
                return textureActiveMossy;
            case TileWaystone.VARIANT_STONEBRICK:
            case TileWaystone.VARIANT_MOSSY_STONEBRICK:
                return textureActiveStonebrick;
            case TileWaystone.VARIANT_NETHER:
                return textureActiveNether;
            case TileWaystone.VARIANT_END:
                return textureActiveEnd;
            default:
                return textureActive;
        }
    }
}
