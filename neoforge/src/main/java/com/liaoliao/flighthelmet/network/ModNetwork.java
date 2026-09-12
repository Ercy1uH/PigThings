package com.liaoliao.flighthelmet.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all payloads. The 1.20.1 SimpleChannel (plus its separate late registration stage in
 * RingRegistration) is replaced by a single payload registrar.
 */
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(UpdateHelmetSettingsPacket.TYPE, UpdateHelmetSettingsPacket.STREAM_CODEC,
                UpdateHelmetSettingsPacket::handle);
        registrar.playToServer(UpdateRingSettingsPacket.TYPE, UpdateRingSettingsPacket.STREAM_CODEC,
                UpdateRingSettingsPacket::handle);
        registrar.playToServer(UpdateNicePickaxeSettingsPacket.TYPE, UpdateNicePickaxeSettingsPacket.STREAM_CODEC,
                UpdateNicePickaxeSettingsPacket::handle);
        registrar.playToServer(SearchContainerPacket.TYPE, SearchContainerPacket.STREAM_CODEC,
                SearchContainerPacket::handle);
        registrar.playToClient(SearchContainerResultPacket.TYPE, SearchContainerResultPacket.STREAM_CODEC,
                SearchContainerResultPacket::handle);
    }
}
