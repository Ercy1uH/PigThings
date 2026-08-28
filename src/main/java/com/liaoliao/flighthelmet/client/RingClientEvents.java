package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.RingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "pigthings", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RingClientEvents {
    private RingClientEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != 1) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || event.getKey() != 71 || RingHelper.hasHelmet(player)) {
            return;
        }
        ItemStack ring = RingHelper.getEquippedRing(player);
        if (!ring.isEmpty()) {
            minecraft.setScreen(new RingSettingsScreen(ring));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || RingHelper.hasHelmet(player) || RingHelper.getEquippedRing(player).isEmpty()) {
            return;
        }
        ItemStack ring = RingHelper.getEquippedRing(player);
        if (!player.getAbilities().flying || !com.liaoliao.flighthelmet.FlightHelmetSettings.hasNoInertia(ring)) {
            return;
        }
        Vec3 movement = player.getDeltaMovement();
        boolean horizontalInput = Math.abs(player.input.leftImpulse) > 0.001f
                || Math.abs(player.input.forwardImpulse) > 0.001f;
        boolean verticalInput = player.input.jumping || player.input.shiftKeyDown;
        double x = horizontalInput ? movement.x : 0.0;
        double y = verticalInput ? movement.y : 0.0;
        double z = horizontalInput ? movement.z : 0.0;
        if (x != movement.x || y != movement.y || z != movement.z) {
            player.setDeltaMovement(x, y, z);
        }
    }
}
