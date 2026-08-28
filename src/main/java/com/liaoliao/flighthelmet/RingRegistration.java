package com.liaoliao.flighthelmet;

import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.UpdateNicePickaxeSettingsPacket;
import com.liaoliao.flighthelmet.network.UpdateRingSettingsPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RingRegistration {
    private RingRegistration() {
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.CHANNEL.registerMessage(4, UpdateRingSettingsPacket.class,
                    UpdateRingSettingsPacket::encode, UpdateRingSettingsPacket::decode,
                    UpdateRingSettingsPacket::handle);
            ModNetwork.CHANNEL.registerMessage(5, UpdateNicePickaxeSettingsPacket.class,
                    UpdateNicePickaxeSettingsPacket::encode, UpdateNicePickaxeSettingsPacket::decode,
                    UpdateNicePickaxeSettingsPacket::handle);
        });
    }

}
