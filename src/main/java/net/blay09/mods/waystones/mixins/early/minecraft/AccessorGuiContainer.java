package net.blay09.mods.waystones.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiContainer.class)
public interface AccessorGuiContainer {

    @Accessor
    int getGuiLeft();

    @Accessor
    int getGuiTop();
}
