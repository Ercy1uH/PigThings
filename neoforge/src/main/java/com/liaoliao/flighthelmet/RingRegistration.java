package com.liaoliao.flighthelmet;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class RingRegistration {
    private RingRegistration() {
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CuriosApi.registerCurio(
                FlightHelmetMod.NICE_RING.get(),
                (ICurioItem) FlightHelmetMod.NICE_RING.get()));
    }
}
