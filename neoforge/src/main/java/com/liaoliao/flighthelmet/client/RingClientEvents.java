package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.RingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Dist.CLIENT)
public final class RingClientEvents {
    private RingClientEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null
                || event.getKey() != GLFW.GLFW_KEY_G || RingHelper.hasHelmet(player)) {
            return;
        }
        ItemStack ring = RingHelper.getEquippedRing(player);
        if (!ring.isEmpty()) {
            minecraft.setScreen(new RingSettingsScreen(ring));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || RingHelper.hasHelmet(player)) {
            return;
        }
        ItemStack ring = RingHelper.getEquippedRing(player);
        if (ring.isEmpty() || !player.getAbilities().flying || !FlightHelmetSettings.hasNoInertia(ring)) {
            return;
        }
        Vec3 movement = player.getDeltaMovement();
        boolean horizontalInput = Math.abs(player.input.leftImpulse) > 0.001F
                || Math.abs(player.input.forwardImpulse) > 0.001F;
        boolean verticalInput = player.input.jumping || player.input.shiftKeyDown;
        double x = horizontalInput ? movement.x : 0.0;
        double y = verticalInput ? movement.y : 0.0;
        double z = horizontalInput ? movement.z : 0.0;
        if (x != movement.x || y != movement.y || z != movement.z) {
            player.setDeltaMovement(x, y, z);
        }
    }
}
