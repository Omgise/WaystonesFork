package net.blay09.mods.waystones.mixins.early.minecraft;

import java.awt.image.BufferedImage;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.PackMetadataSection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourcePackRepository.Entry.class)
public interface AccessorResourcePackRepositoryEntry {

    @Accessor
    void setReResourcePack(IResourcePack pack);

    @Accessor
    void setRePackMetadataSection(PackMetadataSection section);

    @Accessor
    void setTexturePackIcon(BufferedImage icon);
}
