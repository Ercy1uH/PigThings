package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.NicePickaxeItem;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.UpdateNicePickaxeSettingsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class NicePickaxeSettingsScreen extends Screen {
    private final ItemStack pickaxe;
    private boolean preciseMode;

    public NicePickaxeSettingsScreen(ItemStack pickaxe) {
        super(Component.translatable("screen.pigthings.nice_pickaxe.title"));
        this.pickaxe = pickaxe;
        this.preciseMode = NicePickaxeItem.isPreciseMode(pickaxe);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 20;
        Button preciseButton = Button.builder(preciseText(), button -> {
            preciseMode = !preciseMode;
            button.setMessage(preciseText());
        }).bounds(centerX - 100, top, 200, 20).build();
        addRenderableWidget(preciseButton);
        addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.done"), button -> onClose())
                .bounds(centerX - 100, top + 24, 200, 20).build());
    }

    private Component preciseText() {
        return Component.translatable(preciseMode
                ? "screen.pigthings.nice_pickaxe.precise.on"
                : "screen.pigthings.nice_pickaxe.precise.off");
    }

    @Override
    public void onClose() {
        NicePickaxeItem.setSettings(pickaxe, preciseMode);
        ModNetwork.CHANNEL.sendToServer(new UpdateNicePickaxeSettingsPacket(preciseMode));
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 45, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
