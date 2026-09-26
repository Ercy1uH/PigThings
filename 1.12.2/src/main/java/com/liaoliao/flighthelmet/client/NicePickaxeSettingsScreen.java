package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.NicePickaxeItem;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.PacketUpdatePickaxeSettings;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class NicePickaxeSettingsScreen extends GuiScreen {
    private final ItemStack pickaxe;
    private final String screenTitle;
    private boolean preciseMode;

    public NicePickaxeSettingsScreen(ItemStack pickaxe) {
        this.pickaxe = pickaxe;
        this.screenTitle = I18n.format("screen.pigthings.nice_pickaxe.title");
        this.preciseMode = NicePickaxeItem.isPreciseMode(pickaxe);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int top = this.height / 2 - 20;
        this.buttonList.add(new GuiButton(0, centerX - 100, top, 200, 20, this.preciseText()));
        this.buttonList.add(new GuiButton(1, centerX - 100, top + 24, 200, 20, I18n.format("screen.pigthings.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.preciseMode = !this.preciseMode;
            button.displayString = this.preciseText();
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void onGuiClosed() {
        // Only the flag is applied locally; the server applies the matching Silk Touch enchantment.
        NicePickaxeItem.setPreciseMode(this.pickaxe, this.preciseMode);
        ModNetwork.CHANNEL.sendToServer(new PacketUpdatePickaxeSettings(this.preciseMode));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, this.height / 2 - 45, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String preciseText() {
        return I18n.format(this.preciseMode
                ? "screen.pigthings.nice_pickaxe.precise.on"
                : "screen.pigthings.nice_pickaxe.precise.off");
    }
}
