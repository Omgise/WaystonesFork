package net.blay09.mods.waystones.mixins.early.minecraft;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface AccessorGuiScreen {

    @Invoker
    void callFunc_146283_a(List<String> textLines, int x, int y);
}
