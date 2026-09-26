package com.liaoliao.flighthelmet;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HelmetFluidVisionSettings {
    private static final String KEY = "FlightHelmetClearFluidVision";

    private HelmetFluidVisionSettings() { }

    public static boolean enabled(ItemStack stack) {
        return !stack.hasTag() || !stack.getTag().contains(KEY) || stack.getTag().getBoolean(KEY);
    }

    public static void set(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(KEY, enabled);
    }

    public static boolean active(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(FlightHelmetMod.FLIGHT_HELMET.get()) && enabled(helmet);
    }
}
