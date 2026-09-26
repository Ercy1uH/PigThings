package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.PacketUpdateHelmetSettings;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class FlightHelmetScreen extends GuiScreen {
    private final ItemStack helmet;
    private final String screenTitle;
    private float speedMultiplier;
    private boolean noInertia;
    private boolean nightVision;
    private boolean clearFluidVision;
    private boolean magnetEnabled;
    private int magnetRangeX;
    private int magnetRangeY;
    private int magnetRangeZ;
    private LabelSlider speedSlider;
    private boolean openingChild;

    public FlightHelmetScreen(ItemStack helmet) {
        this.helmet = helmet;
        this.screenTitle = I18n.format("screen.pigthings.title");
        this.speedMultiplier = FlightHelmetSettings.getSpeedMultiplier(helmet);
        this.noInertia = FlightHelmetSettings.hasNoInertia(helmet);
        this.nightVision = FlightHelmetSettings.hasNightVision(helmet);
        this.clearFluidVision = FlightHelmetSettings.hasClearFluidVision(helmet);
        this.magnetEnabled = FlightHelmetSettings.hasMagnet(helmet);
        this.magnetRangeX = FlightHelmetSettings.getMagnetRangeX(helmet);
        this.magnetRangeY = FlightHelmetSettings.getMagnetRangeY(helmet);
        this.magnetRangeZ = FlightHelmetSettings.getMagnetRangeZ(helmet);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int top = this.height / 2 - 74;
        this.speedSlider = new LabelSlider(0, centerX - 100, top, 200, 20, "screen.pigthings.speed", true,
                FlightHelmetSettings.MIN_SPEED_MULTIPLIER, FlightHelmetSettings.MAX_SPEED_MULTIPLIER,
                this.speedMultiplier);
        this.buttonList.add(this.speedSlider);
        this.buttonList.add(new GuiButton(1, centerX - 100, top + 24, 200, 20, this.noInertiaText()));
        this.buttonList.add(new GuiButton(2, centerX - 100, top + 48, 200, 20, this.nightVisionText()));
        this.buttonList.add(new GuiButton(5, centerX - 100, top + 72, 200, 20, this.clearFluidVisionText()));
        this.buttonList.add(new GuiButton(3, centerX - 100, top + 96, 200, 20, I18n.format("screen.pigthings.magnet")));
        this.buttonList.add(new GuiButton(4, centerX - 100, top + 120, 200, 20, I18n.format("screen.pigthings.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                this.noInertia = !this.noInertia;
                button.displayString = this.noInertiaText();
                break;
            case 2:
                this.nightVision = !this.nightVision;
                button.displayString = this.nightVisionText();
                break;
            case 3:
                this.openingChild = true;
                this.mc.displayGuiScreen(new MagnetSettingsScreen(this, this.magnetEnabled,
                        this.magnetRangeX, this.magnetRangeY, this.magnetRangeZ));
                break;
            case 4:
                this.mc.displayGuiScreen(null);
                break;
            case 5:
                this.clearFluidVision = !this.clearFluidVision;
                button.displayString = this.clearFluidVisionText();
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        // 进入磁铁子界面时 1.12.2 也会调用 onGuiClosed，此时不能提前保存。
        if (this.openingChild) {
            this.openingChild = false;
            return;
        }
        this.speedMultiplier = MathHelper.clamp(Math.round(this.speedSlider.getValueFloat() * 10.0F) / 10.0F,
                FlightHelmetSettings.MIN_SPEED_MULTIPLIER, FlightHelmetSettings.MAX_SPEED_MULTIPLIER);
        FlightHelmetSettings.set(this.helmet, this.speedMultiplier, this.noInertia, this.nightVision,
                this.magnetEnabled, this.magnetRangeX, this.magnetRangeY, this.magnetRangeZ);
        FlightHelmetSettings.setClearFluidVision(this.helmet, this.clearFluidVision);
        ModNetwork.CHANNEL.sendToServer(new PacketUpdateHelmetSettings(this.speedMultiplier, this.noInertia,
                this.nightVision, this.magnetEnabled, this.magnetRangeX, this.magnetRangeY, this.magnetRangeZ,
                this.clearFluidVision));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, this.height / 2 - 99, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    void updateMagnetSettings(boolean enabled, int rangeX, int rangeY, int rangeZ) {
        this.magnetEnabled = enabled;
        this.magnetRangeX = rangeX;
        this.magnetRangeY = rangeY;
        this.magnetRangeZ = rangeZ;
    }

    private String noInertiaText() {
        return I18n.format(this.noInertia
                ? "screen.pigthings.no_inertia.on" : "screen.pigthings.no_inertia.off");
    }

    private String nightVisionText() {
        return I18n.format(this.nightVision
                ? "screen.pigthings.night_vision.on" : "screen.pigthings.night_vision.off");
    }

    private String clearFluidVisionText() {
        return I18n.format(this.clearFluidVision
                ? "screen.pigthings.clear_fluid_vision.on" : "screen.pigthings.clear_fluid_vision.off");
    }
}
