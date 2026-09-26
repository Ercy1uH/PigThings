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
        int centerX = width / 2;
        int top = height / 2 - 62;
        addRenderableWidget(Button.builder(magnetText(), button -> {
            magnetEnabled = !magnetEnabled;
            button.setMessage(magnetText());
        }).bounds(centerX - 100, top, 200, 20).build());
        addRangeSlider(top + 24, "screen.pigthings.magnet.range_x", () -> rangeX, value -> rangeX = value);
        addRangeSlider(top + 48, "screen.pigthings.magnet.range_y", () -> rangeY, value -> rangeY = value);
        addRangeSlider(top + 72, "screen.pigthings.magnet.range_z", () -> rangeZ, value -> rangeZ = value);
        addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.back"), button -> onClose())
                .bounds(centerX - 100, top + 96, 200, 20).build());
    }

    private void addRangeSlider(int y, String translationKey, IntSupplier getter, IntConsumer setter) {
        int centerX = width / 2;
        addRenderableWidget(new AbstractSliderButton(centerX - 100, y, 200, 20, Component.empty(),
                toSliderValue(getter.getAsInt())) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.translatable(translationKey, getter.getAsInt()));
            }

            @Override
            protected void applyValue() {
                setter.accept(toRange(value));
                updateMessage();
            }
        });
    }

    private Component magnetText() {
        return Component.translatable(magnetEnabled ? "screen.pigthings.magnet.on" : "screen.pigthings.magnet.off");
    }

    private static double toSliderValue(int range) {
        return (range - 1) / 11.0;
    }

    private static int toRange(double sliderValue) {
        return Mth.clamp((int) Math.round(1.0 + sliderValue * 11.0), 1, 12);
    }

    @Override
    public void onClose() {
        parent.updateMagnetSettings(magnetEnabled, rangeX, rangeY, rangeZ);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 87, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
