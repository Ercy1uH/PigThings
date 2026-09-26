package com.liaoliao.flighthelmet.client;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class RingMagnetSettingsScreen extends Screen {
    private final RingSettingsScreen parent;
    private boolean magnetEnabled;
    private int rangeX;
    private int rangeY;
    private int rangeZ;

    public RingMagnetSettingsScreen(RingSettingsScreen parent, boolean magnetEnabled, int rangeX, int rangeY, int rangeZ) {
        super(Component.translatable("screen.pigthings.magnet.title"));
        this.parent = parent;
        this.magnetEnabled = magnetEnabled;
        this.rangeX = rangeX;
        this.rangeY = rangeY;
        this.rangeZ = rangeZ;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 2 - 62;
        this.addRenderableWidget(Button.builder(this.magnetText(), button -> {
            this.magnetEnabled = !this.magnetEnabled;
            button.setMessage(this.magnetText());
        }).bounds(centerX - 100, top, 200, 20).build());
        this.addRangeSlider(top + 24, "screen.pigthings.magnet.range_x", () -> this.rangeX, value -> this.rangeX = value);
        this.addRangeSlider(top + 48, "screen.pigthings.magnet.range_y", () -> this.rangeY, value -> this.rangeY = value);
        this.addRangeSlider(top + 72, "screen.pigthings.magnet.range_z", () -> this.rangeZ, value -> this.rangeZ = value);
        this.addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.back"),
                button -> this.onClose()).bounds(centerX - 100, top + 96, 200, 20).build());
    }

    private void addRangeSlider(int y, String translationKey, IntSupplier getter, IntConsumer setter) {
        int centerX = this.width / 2;
        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, y, 200, 20, Component.empty(),
                toSliderValue(getter.getAsInt())) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable(translationKey, getter.getAsInt()));
            }

            @Override
            protected void applyValue() {
                setter.accept(toRange(this.value));
                this.updateMessage();
            }
        });
    }

    private Component magnetText() {
        return Component.translatable(this.magnetEnabled
                ? "screen.pigthings.magnet.on" : "screen.pigthings.magnet.off");
    }

    private static double toSliderValue(int range) {
        return (range - 1) / 11.0;
    }

    private static int toRange(double sliderValue) {
        return Mth.clamp((int) Math.round(1.0 + sliderValue * 11.0), 1, 12);
    }

    @Override
    public void onClose() {
        this.parent.updateMagnetSettings(this.magnetEnabled, this.rangeX, this.rangeY, this.rangeZ);
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 87, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
