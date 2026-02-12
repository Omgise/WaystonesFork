package net.blay09.mods.waystones.mixins.early.minecraft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.client.resource.WaystonesAlternateResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourcePackRepository.class)
public class MixinResourcePackRepository {

    @Shadow
    private List repositoryEntriesAll;

    @Shadow
    public IMetadataSerializer rprMetadataSerializer;

    @Shadow
    public IResourcePack rprDefaultResourcePack;

    @Unique
    private static final String WAYSTONES_ALT_PACK_NAME = "Waystones Alternate Textures";

    @Unique
    private static WaystonesAlternateResourcePack waystones$alternatePack;

    @SuppressWarnings("unchecked")
    @Inject(method = "updateRepositoryEntriesAll", at = @At("RETURN"))
    private void waystones$injectAlternatePack(CallbackInfo ci) {
        if (!WaystoneConfig.debugMode) {
            return;
        }

        // Check if already present
        for (Object obj : this.repositoryEntriesAll) {
            ResourcePackRepository.Entry entry = (ResourcePackRepository.Entry) obj;
            IResourcePack pack = entry.getResourcePack();
            if (pack != null && WAYSTONES_ALT_PACK_NAME.equals(pack.getPackName())) {
                return;
            }
        }

        try {
            if (waystones$alternatePack == null) {
                waystones$alternatePack = new WaystonesAlternateResourcePack();
            }

            // Create Entry via reflection (synthetic constructor is package-private)
            ResourcePackRepository self = (ResourcePackRepository) (Object) this;
            Constructor<?> ctor = ResourcePackRepository.Entry.class
                .getDeclaredConstructor(ResourcePackRepository.class, File.class, Object.class);
            ctor.setAccessible(true);
            ResourcePackRepository.Entry entry = (ResourcePackRepository.Entry) ctor
                .newInstance(self, new File("waystones_alternate_textures"), null);

            // Populate fields via accessor instead of calling updateResourcePack()
            AccessorResourcePackRepositoryEntry accessor = (AccessorResourcePackRepositoryEntry) entry;
            accessor.setReResourcePack(waystones$alternatePack);

            PackMetadataSection metadata = (PackMetadataSection) waystones$alternatePack
                .getPackMetadata(this.rprMetadataSerializer, "pack");
            accessor.setRePackMetadataSection(metadata);

            BufferedImage icon;
            try {
                icon = waystones$alternatePack.getPackImage();
            } catch (Exception e) {
                icon = this.rprDefaultResourcePack.getPackImage();
            }
            accessor.setTexturePackIcon(icon);

            this.repositoryEntriesAll.add(entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
