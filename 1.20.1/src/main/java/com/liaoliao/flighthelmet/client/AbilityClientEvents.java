package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.GhostAbilityHandler;
import com.liaoliao.flighthelmet.HelmetFluidVisionSettings;
import com.liaoliao.flighthelmet.network.AbilityNetwork;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Dist.CLIENT)
public final class AbilityClientEvents {
    private static boolean ghostTerrain;
    private static boolean endRequested;

    private AbilityClientEvents() { }

    // Used only by LevelRenderer.setupRender, without changing the player's game mode.
    public static boolean unrestrictedTerrain(boolean spectator) {
        Minecraft mc = Minecraft.getInstance();
        return spectator || (mc.player != null && mc.getCameraEntity() == mc.player
                && GhostAbilityHandler.isActive(mc.player));
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean active = unrestrictedTerrain(false);
        if (active != ghostTerrain) {
            ghostTerrain = active;
            Minecraft.getInstance().levelRenderer.needsUpdate();
        }
    }

    private static void requestEnd() {
        if (!endRequested) {
            endRequested = true;
            AbilityNetwork.CHANNEL.sendToServer(new AbilityNetwork.EndGhost());
        }
    }

    @SubscribeEvent
    public static void onInput(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !GhostAbilityHandler.isActive(player)) return;
        if (event.isUseItem() && player.getItemInHand(event.getHand()).is(FlightHelmetMod.TRAPPED_PIGLIN_REMAINS.get())) return;
        requestEnd();
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !GhostAbilityHandler.isActive(mc.player)) {
            endRequested = false;
        } else if (mc.screen == null && (mc.options.keyAttack.isDown() || mc.options.keyDrop.isDown()
                || mc.options.keyPickItem.isDown())) requestEnd();
    }

    @SubscribeEvent
    public static void onOverlay(RenderBlockScreenEffectEvent event) {
        if (GhostAbilityHandler.isActive(event.getPlayer())) {
            event.setCanceled(true);
        } else if (HelmetFluidVisionSettings.active(event.getPlayer())
                && (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.WATER
                || (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && event.getPlayer().isInLava()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        if (!(event.getCamera().getEntity() instanceof Player player)) return;
        if (event.getType() != FogType.WATER && event.getType() != FogType.LAVA) return;
        if (player.hasEffect(MobEffects.BLINDNESS) || player.hasEffect(MobEffects.DARKNESS)) return;
        if (!HelmetFluidVisionSettings.active(player) && !GhostAbilityHandler.isActive(player)) return;
        float distance = Math.max(128, Minecraft.getInstance().options.getEffectiveRenderDistance() * 16);
        event.setNearPlaneDistance(distance * 0.75f);
        event.setFarPlaneDistance(distance);
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }
}
