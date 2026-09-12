package com.liaoliao.flighthelmet;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;

@EventBusSubscriber(modid = FlightHelmetMod.MOD_ID)
public final class RingEquipHandler {
    private RingEquipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void allowRingInAnyCurioSlot(CurioCanEquipEvent event) {
        if (event.getStack().getItem() instanceof NiceRingItem) {
            event.setEquipResult(TriState.TRUE);
        }
    }
}
