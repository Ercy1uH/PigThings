package com.liaoliao.flighthelmet.compat;

import net.minecraft.item.Item;

public final class EnderIoIntegration {
    private EnderIoIntegration() {
    }

    // Keep optional types out of the event subscriber's bytecode verification.
    public static Item createSaber() {
        return new EnderIoCarrotSaberItem();
    }
}
