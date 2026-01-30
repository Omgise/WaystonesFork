package net.blay09.mods.waystones.client.gui;

import net.blay09.mods.waystones.util.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSimpleYesNo extends GuiScreen {

    public interface Callback {

        void onResult(boolean yes);
    }

    private final GuiScreen parent;
    private final boolean yesCloseParent;
    private final boolean noCloseParent;
    private final String question;
    private final Callback callback;

    private GuiButton yesButton;
    private GuiButton noButton;

    public GuiSimpleYesNo(GuiScreen parent, boolean yesCloseParent, boolean noCloseParent, String question,
        Callback callback) {
        this.parent = parent;
        this.yesCloseParent = yesCloseParent;
        this.noCloseParent = noCloseParent;
        this.question = question;
        this.callback = callback;
    }

    @Override
    public void initGui() {
        int y = this.height / 2 + 10;

        this.yesButton = new GuiButton(0, this.width / 2 - 55, y, 50, 20, I18n.format("gui.waystones:dialogYes"));
        this.buttonList.add(yesButton);

        this.noButton = new GuiButton(1, this.width / 2 + 5, y, 50, 20, I18n.format("gui.waystones:dialogNo"));
        this.buttonList.add(noButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (callback != null) {
            callback.onResult(button.id == 0);
        }

        if (this.yesCloseParent && button.id == 0) {
            this.mc.displayGuiScreen(null);
        } else if (this.noCloseParent && button.id == 1) {
            this.mc.displayGuiScreen(null);
        } else {
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // NO background draw call here on purpose

        ClientUtil.drawSolidRect(
            Tessellator.instance,
            0,
            0,
            Minecraft.getMinecraft().displayWidth,
            Minecraft.getMinecraft().displayHeight,
            0xB2000000);

        int padding = 15;
        ClientUtil.drawBorderedBox(
            (double) this.width / 2 - (double) this.fontRendererObj.getStringWidth(this.question) / 2 - padding,
            (double) this.height / 2 - 10 - padding,
            this.fontRendererObj.getStringWidth(this.question) + 2 * padding,
            (yesButton.yPosition + yesButton.height + padding) - ((double) this.height / 2 - 10 - padding),
            1,
            0xD1000000,
            0xFFAAAAAA);

        this.drawCenteredString(this.fontRendererObj, this.question, this.width / 2, this.height / 2 - 10, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
