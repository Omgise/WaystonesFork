package net.blay09.mods.waystones.mixins.early.minecraft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

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
    private static final String WAYSTONES_ALT_PACK_NAME = "Waystones Modernity Textures";

    @Unique
    private static WaystonesAlternateResourcePack waystones$alternatePack;

    @Unique
    private static ResourcePackRepository.Entry waystones$createEntry(ResourcePackRepository self, File file)
        throws Exception {
        Constructor<?>[] constructors = ResourcePackRepository.Entry.class.getDeclaredConstructors();
        for (Constructor<?> ctor : constructors) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length < 2) {
                continue;
            }
            if (!ResourcePackRepository.class.isAssignableFrom(params[0])) {
                continue;
            }
            if (!File.class.isAssignableFrom(params[1])) {
                continue;
            }

            Object[] args = new Object[params.length];
            args[0] = self;
            args[1] = file;
            for (int i = 2; i < params.length; i++) {
                args[i] = waystones$defaultArg(params[i]);
            }

            ctor.setAccessible(true);
            return (ResourcePackRepository.Entry) ctor.newInstance(args);
        }
        throw new NoSuchMethodException(
            "No compatible ResourcePackRepository.Entry constructor found in "
                + ResourcePackRepository.Entry.class.getName());
    }

    @Unique
    private static Object waystones$defaultArg(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "updateRepositoryEntriesAll", at = @At("RETURN"))
    private void waystones$injectAlternatePack(CallbackInfo ci) {
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

            // Constructor shape differs between dev/prod bytecode (synthetic bridge may be absent/present).
            ResourcePackRepository self = (ResourcePackRepository) (Object) this;
            ResourcePackRepository.Entry entry = waystones$createEntry(self, new File("waystones_modernity"));

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
