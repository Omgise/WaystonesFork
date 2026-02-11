package net.blay09.mods.waystones.mixins.early.minecraft;

import net.minecraft.world.gen.structure.StructureComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureComponent.class)
public interface AccessorStructureComponent {

    @Invoker
    int callGetXWithOffset(int x, int z);

    @Invoker
    int callGetYWithOffset(int y);

    @Invoker
    int callGetZWithOffset(int x, int z);
}
