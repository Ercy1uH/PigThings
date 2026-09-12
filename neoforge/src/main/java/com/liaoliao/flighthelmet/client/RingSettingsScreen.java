package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.network.UpdateRingSettingsPacket;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

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
        int centerX = this.width / 2;
        int top = this.height / 2 - 62;
        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, top, 200, 20, Component.empty(),
                (this.speedMultiplier - 1.0F) / 4.0F) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("screen.pigthings.speed",
                        String.format(Locale.ROOT, "%.1f", RingSettingsScreen.this.speedMultiplier)));
            }

            @Override
            protected void applyValue() {
                RingSettingsScreen.this.speedMultiplier =
                        Math.round((1.0F + (float) this.value * 4.0F) * 10.0F) / 10.0F;
                this.updateMessage();
            }
        });
        this.addRenderableWidget(Button.builder(this.noInertiaText(), button -> {
            this.noInertia = !this.noInertia;
            button.setMessage(this.noInertiaText());
        }).bounds(centerX - 100, top + 24, 200, 20).build());
        this.addRenderableWidget(Button.builder(this.nightVisionText(), button -> {
            this.nightVision = !this.nightVision;
            button.setMessage(this.nightVisionText());
        }).bounds(centerX - 100, top + 48, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.magnet"),
                button -> this.minecraft.setScreen(new RingMagnetSettingsScreen(this, this.magnetEnabled,
                        this.magnetRangeX, this.magnetRangeY, this.magnetRangeZ)))
                .bounds(centerX - 100, top + 72, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.done"),
                button -> this.onClose()).bounds(centerX - 100, top + 96, 200, 20).build());
    }

    private Component noInertiaText() {
        return Component.translatable(this.noInertia
                ? "screen.pigthings.no_inertia.on" : "screen.pigthings.no_inertia.off");
    }

    private Component nightVisionText() {
        return Component.translatable(this.nightVision
                ? "screen.pigthings.night_vision.on" : "screen.pigthings.night_vision.off");
    }

    void updateMagnetSettings(boolean enabled, int rangeX, int rangeY, int rangeZ) {
        this.magnetEnabled = enabled;
        this.magnetRangeX = rangeX;
        this.magnetRangeY = rangeY;
        this.magnetRangeZ = rangeZ;
    }

    @Override
    public void onClose() {
        FlightHelmetSettings.set(this.ring, this.speedMultiplier, this.noInertia, this.nightVision,
                this.magnetEnabled, this.magnetRangeX, this.magnetRangeY, this.magnetRangeZ);
        PacketDistributor.sendToServer(new UpdateRingSettingsPacket(this.speedMultiplier, this.noInertia,
                this.nightVision, this.magnetEnabled, this.magnetRangeX, this.magnetRangeY, this.magnetRangeZ));
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 87, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
