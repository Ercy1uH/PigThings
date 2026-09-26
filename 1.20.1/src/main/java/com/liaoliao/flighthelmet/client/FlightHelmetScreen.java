package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.HelmetFluidVisionSettings;
import com.liaoliao.flighthelmet.network.AbilityNetwork;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.UpdateHelmetSettingsPacket;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class FlightHelmetScreen extends Screen {
    private final ItemStack helmet;
    private float speedMultiplier;
    private boolean noInertia;
    private boolean nightVision;
    private boolean clearFluidVision;
    private boolean magnetEnabled;
    private int magnetRangeX;
    private int magnetRangeY;
    private int magnetRangeZ;

    public FlightHelmetScreen(ItemStack helmet) {
        super(Component.translatable("screen.pigthings.title"));
        this.helmet = helmet;
        speedMultiplier = FlightHelmetSettings.getSpeedMultiplier(helmet);
        noInertia = FlightHelmetSettings.hasNoInertia(helmet);
        nightVision = FlightHelmetSettings.hasNightVision(helmet);
        clearFluidVision = HelmetFluidVisionSettings.enabled(helmet);
        magnetEnabled = FlightHelmetSettings.hasMagnet(helmet);
        magnetRangeX = FlightHelmetSettings.getMagnetRangeX(helmet);
        magnetRangeY = FlightHelmetSettings.getMagnetRangeY(helmet);
        magnetRangeZ = FlightHelmetSettings.getMagnetRangeZ(helmet);
    }

    @Override
    protected void init() {
        int left = width / 2 - 100;
        int top = height / 2 - 74;
        addRenderableWidget(new AbstractSliderButton(left, top, 200, 20, Component.empty(),
                (speedMultiplier - 1.0F) / 4.0F) {
            { updateMessage(); }

            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("screen.pigthings.speed",
                        String.format(Locale.ROOT, "%.1f", FlightHelmetScreen.this.speedMultiplier)));
            }

            @Override
            protected void applyValue() {
                FlightHelmetScreen.this.speedMultiplier = Math.round((1.0F + (float) value * 4.0F) * 10.0F) / 10.0F;
                updateMessage();
            }
        });
        addRenderableWidget(Button.builder(toggleText("no_inertia", noInertia), button -> {
            noInertia = !noInertia;
            button.setMessage(toggleText("no_inertia", noInertia));
        }).bounds(left, top + 24, 200, 20).build());
        addRenderableWidget(Button.builder(toggleText("night_vision", nightVision), button -> {
            nightVision = !nightVision;
            button.setMessage(toggleText("night_vision", nightVision));
        }).bounds(left, top + 48, 200, 20).build());
        addRenderableWidget(Button.builder(toggleText("clear_fluid_vision", clearFluidVision), button -> {
            clearFluidVision = !clearFluidVision;
            button.setMessage(toggleText("clear_fluid_vision", clearFluidVision));
        }).bounds(left, top + 72, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.magnet"),
                button -> minecraft.setScreen(new MagnetSettingsScreen(this, magnetEnabled,
                        magnetRangeX, magnetRangeY, magnetRangeZ))).bounds(left, top + 96, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.done"),
                button -> onClose()).bounds(left, top + 120, 200, 20).build());
    }

    private static Component toggleText(String name, boolean enabled) {
        return Component.translatable("screen.pigthings." + name + (enabled ? ".on" : ".off"));
    }

    void updateMagnetSettings(boolean enabled, int rangeX, int rangeY, int rangeZ) {
        magnetEnabled = enabled;
        magnetRangeX = rangeX;
        magnetRangeY = rangeY;
        magnetRangeZ = rangeZ;
    }

    @Override
    public void onClose() {
        FlightHelmetSettings.set(helmet, speedMultiplier, noInertia, nightVision, magnetEnabled,
                magnetRangeX, magnetRangeY, magnetRangeZ);
        HelmetFluidVisionSettings.set(helmet, clearFluidVision);
        ModNetwork.CHANNEL.sendToServer(new UpdateHelmetSettingsPacket(speedMultiplier, noInertia,
                nightVision, magnetEnabled, magnetRangeX, magnetRangeY, magnetRangeZ));
        AbilityNetwork.CHANNEL.sendToServer(new AbilityNetwork.FluidVision(clearFluidVision));
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 99, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
