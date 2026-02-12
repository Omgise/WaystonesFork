package net.blay09.mods.waystones.client.resource;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WaystonesAlternateResourcePack implements IResourcePack {

    private static final String PACK_NAME = "Waystones Alternate Textures";
    private static final String PREFIX = "/waystones_alt_pack/";
    private static final String PACK_META_JSON = "{" + "\"pack\":{"
        + "\"description\":\"Alternate textures for Waystones-X\","
        + "\"pack_format\":1"
        + "}"
        + "}";

    private String locationToName(ResourceLocation loc) {
        return String.format("assets/%s/%s", loc.getResourceDomain(), loc.getResourcePath());
    }

    @Override
    public InputStream getInputStream(ResourceLocation loc) throws IOException {
        String path = PREFIX + locationToName(loc);
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            throw new IOException("Resource not found: " + loc);
        }
        return is;
    }

    @Override
    public boolean resourceExists(ResourceLocation loc) {
        String path = PREFIX + locationToName(loc);
        return getClass().getResource(path) != null;
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("waystones");
    }

    @Override
    public IMetadataSection getPackMetadata(IMetadataSerializer serializer, String section) throws IOException {
        InputStream is = new ByteArrayInputStream(PACK_META_JSON.getBytes(Charsets.UTF_8));
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            JsonObject json = new JsonParser().parse(reader)
                .getAsJsonObject();
            return serializer.parseMetadataSection(section, json);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        InputStream is = getClass().getResourceAsStream(PREFIX + "pack.png");
        if (is == null) {
            is = getClass().getResourceAsStream("/assets/waystones/logo_small.png");
        }
        if (is == null) {
            throw new IOException("No pack image found");
        }
        return ImageIO.read(is);
    }

    @Override
    public String getPackName() {
        return PACK_NAME;
    }
}
