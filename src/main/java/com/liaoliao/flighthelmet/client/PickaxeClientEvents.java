package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.NicePickaxeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "pigthings", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PickaxeClientEvents {
    private PickaxeClientEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != 1 || event.getKey() != 71) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            return;
        }
        ItemStack pickaxe = getHeldPickaxe(player);
        if (!pickaxe.isEmpty()) {
            minecraft.setScreen(new NicePickaxeSettingsScreen(pickaxe));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.options.keyUse.isDown()) {
            NicePickaxeItem.resetClientRightClick();
        }
    }

    private static ItemStack getHeldPickaxe(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof NicePickaxeItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof NicePickaxeItem ? offHand : ItemStack.EMPTY;
    }
}
