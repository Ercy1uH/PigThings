package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.NicePickaxeItem;
import com.liaoliao.flighthelmet.network.UpdateNicePickaxeSettingsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

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
        int centerX = this.width / 2;
        int top = this.height / 2 - 20;
        this.addRenderableWidget(Button.builder(this.preciseText(), button -> {
            this.preciseMode = !this.preciseMode;
            button.setMessage(this.preciseText());
        }).bounds(centerX - 100, top, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.pigthings.done"),
                button -> this.onClose()).bounds(centerX - 100, top + 24, 200, 20).build());
    }

    private Component preciseText() {
        return Component.translatable(this.preciseMode
                ? "screen.pigthings.nice_pickaxe.precise.on"
                : "screen.pigthings.nice_pickaxe.precise.off");
    }

    @Override
    public void onClose() {
        // Only the flag is applied locally; the server applies the matching Silk Touch enchantment.
        NicePickaxeItem.setPreciseMode(this.pickaxe, this.preciseMode);
        PacketDistributor.sendToServer(new UpdateNicePickaxeSettingsPacket(this.preciseMode));
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 45, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
