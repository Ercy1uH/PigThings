package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ModNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(FlightHelmetMod.MOD_ID);

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(PacketUpdateHelmetSettings.Handler.class, PacketUpdateHelmetSettings.class, 0, Side.SERVER);
        CHANNEL.registerMessage(PacketUpdateRingSettings.Handler.class, PacketUpdateRingSettings.class, 1, Side.SERVER);
        CHANNEL.registerMessage(PacketUpdatePickaxeSettings.Handler.class, PacketUpdatePickaxeSettings.class, 2, Side.SERVER);
        CHANNEL.registerMessage(PacketSearchContainer.Handler.class, PacketSearchContainer.class, 3, Side.SERVER);
        CHANNEL.registerMessage(PacketSearchContainerResult.Handler.class, PacketSearchContainerResult.class, 4, Side.CLIENT);
        CHANNEL.registerMessage(PacketBreakBlock.Handler.class, PacketBreakBlock.class, 5, Side.SERVER);
        CHANNEL.registerMessage(PacketEndGhost.Handler.class, PacketEndGhost.class, 6, Side.SERVER);
    }
}
