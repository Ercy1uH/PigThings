package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.UpdateRingSettingsPacket;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RingSettingsScreen extends Screen {
    private final ItemStack ring;
    private float speedMultiplier;
    private boolean noInertia;
    private boolean nightVision;
    private boolean magnetEnabled;
    private int magnetRangeX;
    private int magnetRangeY;
    private int magnetRangeZ;

    public RingSettingsScreen(ItemStack ring) {
        super(Component.translatable("screen.pigthings.ring.title"));
        this.ring = ring;
        this.speedMultiplier = FlightHelmetSettings.getSpeedMultiplier(ring);
        this.noInertia = FlightHelmetSettings.hasNoInertia(ring);
        this.nightVision = FlightHelmetSettings.hasNightVision(ring);
        this.magnetEnabled = FlightHelmetSettings.hasMagnet(ring);
        this.magnetRangeX = FlightHelmetSettings.getMagnetRangeX(ring);
        this.magnetRangeY = FlightHelmetSettings.getMagnetRangeY(ring);
        this.magnetRangeZ = FlightHelmetSettings.getMagnetRangeZ(ring);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 62;
        addRenderableWidget(new AbstractSliderButton(centerX - 100, top, 200, 20, Component.empty(),
                (speedMultiplier - 1.0f) / 4.0f) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("screen.pigthings.speed",
                        String.format(Locale.ROOT, "%.1f", speedMultiplier)));
            }

            @Override
            protected void applyValue() {
                speedMultiplier = Math.round((1.0f + (float) value * 4.0f) * 10.0f) / 10.0f;
                updateMessage();
            }
        });
        Button noInertiaButton = Button.builder(noInertiaText(), button -> {
            noInertia = !noInertia;
            button.setMessage(noInertiaText());
        }).bounds(centerX - 100, top + 24, 200, 20).build();
        addRenderableWidget(noInertiaButton);
        Button nightVisionButton = Button.builder(nightVisionText(), button -> {
            nightVision = !nightVision;
            button.setMessage(nightVisionText());
        }).bounds(centerX - 100, top + 48, 200, 20).build();
        addRenderableWidget(nightVisionButton);
        addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.magnet"), button ->
                minecraft.setScreen(new RingMagnetSettingsScreen(this, magnetEnabled, magnetRangeX, magnetRangeY,
                        magnetRangeZ))).bounds(centerX - 100, top + 72, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.done"), button -> onClose())
                .bounds(centerX - 100, top + 96, 200, 20).build());
    }

    private Component noInertiaText() {
        return Component.translatable(noInertia ? "screen.pigthings.no_inertia.on" : "screen.pigthings.no_inertia.off");
    }

    private Component nightVisionText() {
        return Component.translatable(nightVision ? "screen.pigthings.night_vision.on" : "screen.pigthings.night_vision.off");
    }

    void updateMagnetSettings(boolean enabled, int rangeX, int rangeY, int rangeZ) {
        magnetEnabled = enabled;
        magnetRangeX = rangeX;
        magnetRangeY = rangeY;
        magnetRangeZ = rangeZ;
    }

    @Override
    public void onClose() {
        FlightHelmetSettings.set(ring, speedMultiplier, noInertia, nightVision, magnetEnabled,
                magnetRangeX, magnetRangeY, magnetRangeZ);
        ModNetwork.CHANNEL.sendToServer(new UpdateRingSettingsPacket(speedMultiplier, noInertia, nightVision,
                magnetEnabled, magnetRangeX, magnetRangeY, magnetRangeZ));
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 87, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
