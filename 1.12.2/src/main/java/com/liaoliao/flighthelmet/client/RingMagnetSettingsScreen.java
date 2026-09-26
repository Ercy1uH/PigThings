package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetSettings;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RingMagnetSettingsScreen extends GuiScreen {
    private final RingSettingsScreen parent;
    private final String screenTitle;
    private boolean magnetEnabled;
    private int rangeX;
    private int rangeY;
    private int rangeZ;
    private LabelSlider sliderX;
    private LabelSlider sliderY;
    private LabelSlider sliderZ;

    public RingMagnetSettingsScreen(RingSettingsScreen parent, boolean magnetEnabled, int rangeX, int rangeY, int rangeZ) {
        this.parent = parent;
        this.screenTitle = I18n.format("screen.pigthings.magnet.title");
        this.magnetEnabled = magnetEnabled;
        this.rangeX = rangeX;
        this.rangeY = rangeY;
        this.rangeZ = rangeZ;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int top = this.height / 2 - 62;
        this.sliderX = new LabelSlider(1, centerX - 100, top + 24, 200, 20, "screen.pigthings.magnet.range_x",
                false, FlightHelmetSettings.MIN_MAGNET_RANGE, FlightHelmetSettings.MAX_MAGNET_RANGE, this.rangeX);
        this.sliderY = new LabelSlider(2, centerX - 100, top + 48, 200, 20, "screen.pigthings.magnet.range_y",
                false, FlightHelmetSettings.MIN_MAGNET_RANGE, FlightHelmetSettings.MAX_MAGNET_RANGE, this.rangeY);
        this.sliderZ = new LabelSlider(3, centerX - 100, top + 72, 200, 20, "screen.pigthings.magnet.range_z",
                false, FlightHelmetSettings.MIN_MAGNET_RANGE, FlightHelmetSettings.MAX_MAGNET_RANGE, this.rangeZ);
        this.buttonList.add(new GuiButton(0, centerX - 100, top, 200, 20, this.magnetText()));
        this.buttonList.add(this.sliderX);
        this.buttonList.add(this.sliderY);
        this.buttonList.add(this.sliderZ);
        this.buttonList.add(new GuiButton(4, centerX - 100, top + 96, 200, 20, I18n.format("screen.pigthings.back")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.magnetEnabled = !this.magnetEnabled;
                button.displayString = this.magnetText();
                break;
            case 4:
                this.mc.displayGuiScreen(this.parent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        this.parent.updateMagnetSettings(this.magnetEnabled, this.sliderX.getValueInt(),
                this.sliderY.getValueInt(), this.sliderZ.getValueInt());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, this.height / 2 - 87, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String magnetText() {
        return I18n.format(this.magnetEnabled
                ? "screen.pigthings.magnet.on" : "screen.pigthings.magnet.off");
    }
}
