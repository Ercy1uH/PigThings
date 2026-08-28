package com.liaoliao.flighthelmet;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RingEquipHandler {
    private RingEquipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void allowRingInAnyCurioSlot(CurioEquipEvent event) {
        if (event.getStack().getItem() instanceof NiceRingItem) {
            event.setResult(Event.Result.ALLOW);
        }
    }
}
