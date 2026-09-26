package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Side.CLIENT)
public final class HelmetFluidVision {
    private HelmetFluidVision() { }

    public static boolean isEnabled(Entity viewer) {
        if (!(viewer instanceof EntityPlayer)) return false;
        ItemStack helmet = ((EntityPlayer) viewer).getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return helmet.getItem() == FlightHelmetMod.FLIGHT_HELMET
                && FlightHelmetSettings.hasClearFluidVision(helmet);
    }

    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        Material material = event.getState().getMaterial();
        if ((material != Material.WATER && material != Material.LAVA) || !isEnabled(event.getEntity())
                || ((EntityPlayer) event.getEntity()).isPotionActive(MobEffects.BLINDNESS)) return;
        // Forge cancellation bypasses vanilla water/lava fog setup; set the mode explicitly.
        GlStateManager.setFog(GlStateManager.FogMode.EXP);
        event.setDensity(0.002F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onOverlay(RenderBlockOverlayEvent event) {
        if (!isEnabled(event.getPlayer())) return;
        if (event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER
                || (event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.FIRE
                    && event.getPlayer().isInLava())) {
            event.setCanceled(true);
        }
    }
}
