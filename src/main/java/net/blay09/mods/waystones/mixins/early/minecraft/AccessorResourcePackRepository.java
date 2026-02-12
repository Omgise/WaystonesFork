package net.blay09.mods.waystones.mixins.early.minecraft;

import java.util.List;

import net.minecraft.client.resources.ResourcePackRepository;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourcePackRepository.class)
public interface AccessorResourcePackRepository {

    @Accessor
    List getRepositoryEntriesAll();
}
