package com.liaoliao.flighthelmet;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class FlightHelmetSettings {
    public static final float MIN_SPEED_MULTIPLIER = 1.0F;
    public static final float MAX_SPEED_MULTIPLIER = 5.0F;
    public static final float VANILLA_FLYING_SPEED = 0.05F;
    public static final int MIN_MAGNET_RANGE = 1;
    public static final int MAX_MAGNET_RANGE = 12;
    public static final int DEFAULT_MAGNET_RANGE = 12;
    private static final String SPEED_KEY = "FlightHelmetSpeedMultiplier";
    private static final String NO_INERTIA_KEY = "FlightHelmetNoInertia";
    private static final String NIGHT_VISION_KEY = "FlightHelmetNightVision";
    private static final String MAGNET_ENABLED_KEY = "FlightHelmetMagnetEnabled";
    private static final String MAGNET_RANGE_X_KEY = "FlightHelmetMagnetRangeX";
    private static final String MAGNET_RANGE_Y_KEY = "FlightHelmetMagnetRangeY";
    private static final String MAGNET_RANGE_Z_KEY = "FlightHelmetMagnetRangeZ";

    private FlightHelmetSettings() {
    }

    private static CompoundTag readTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void updateTag(ItemStack stack, Consumer<CompoundTag> action) {
        CompoundTag tag = readTag(stack);
        action.accept(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static float getSpeedMultiplier(ItemStack stack) {
        CompoundTag tag = readTag(stack);
        return tag.contains(SPEED_KEY)
                ? Mth.clamp(tag.getFloat(SPEED_KEY), MIN_SPEED_MULTIPLIER, MAX_SPEED_MULTIPLIER)
                : 1.0F;
    }

    public static boolean hasNoInertia(ItemStack stack) {
        return readTag(stack).getBoolean(NO_INERTIA_KEY);
    }

    public static boolean hasNightVision(ItemStack stack) {
        return readTag(stack).getBoolean(NIGHT_VISION_KEY);
    }

    public static boolean hasMagnet(ItemStack stack) {
        return readTag(stack).getBoolean(MAGNET_ENABLED_KEY);
    }

    public static int getMagnetRangeX(ItemStack stack) {
        return getMagnetRange(stack, MAGNET_RANGE_X_KEY);
    }

    public static int getMagnetRangeY(ItemStack stack) {
        return getMagnetRange(stack, MAGNET_RANGE_Y_KEY);
    }

    public static int getMagnetRangeZ(ItemStack stack) {
        return getMagnetRange(stack, MAGNET_RANGE_Z_KEY);
    }

    public static void set(ItemStack stack,
                           float speedMultiplier,
                           boolean noInertia,
                           boolean nightVision,
                           boolean magnetEnabled,
                           int magnetRangeX,
                           int magnetRangeY,
                           int magnetRangeZ) {
        updateTag(stack, tag -> {
            tag.putFloat(SPEED_KEY, Mth.clamp(speedMultiplier, MIN_SPEED_MULTIPLIER, MAX_SPEED_MULTIPLIER));
            tag.putBoolean(NO_INERTIA_KEY, noInertia);
            tag.putBoolean(NIGHT_VISION_KEY, nightVision);
            tag.putBoolean(MAGNET_ENABLED_KEY, magnetEnabled);
            tag.putInt(MAGNET_RANGE_X_KEY, clampMagnetRange(magnetRangeX));
            tag.putInt(MAGNET_RANGE_Y_KEY, clampMagnetRange(magnetRangeY));
            tag.putInt(MAGNET_RANGE_Z_KEY, clampMagnetRange(magnetRangeZ));
        });
    }

    private static int getMagnetRange(ItemStack stack, String key) {
        CompoundTag tag = readTag(stack);
        return tag.contains(key) ? clampMagnetRange(tag.getInt(key)) : DEFAULT_MAGNET_RANGE;
    }

    private static int clampMagnetRange(int range) {
        return Mth.clamp(range, MIN_MAGNET_RANGE, MAX_MAGNET_RANGE);
    }
}
