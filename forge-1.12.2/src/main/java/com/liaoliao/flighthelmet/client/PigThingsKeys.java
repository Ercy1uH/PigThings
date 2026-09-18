package com.liaoliao.flighthelmet.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class PigThingsKeys {
    public static final KeyBinding OPEN_SETTINGS = new KeyBinding("key.pigthings.open_settings",
            Keyboard.KEY_G, "key.categories.pigthings");
    public static final KeyBinding SEARCH_CONTAINER = new KeyBinding("key.pigthings.search_container",
            Keyboard.KEY_Y, "key.categories.pigthings");

    private PigThingsKeys() {
    }

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_SETTINGS);
        ClientRegistry.registerKeyBinding(SEARCH_CONTAINER);
    }
}
