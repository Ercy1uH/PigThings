package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.GhostAbilityHandler;
import com.liaoliao.flighthelmet.HelmetFluidVisionSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class AbilityNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FlightHelmetMod.MOD_ID, "abilities"), () -> "1", "1"::equals, "1"::equals);

    private AbilityNetwork() { }

    public record EndGhost() { }
    public record FluidVision(boolean enabled) { }

    public static void register() {
        CHANNEL.messageBuilder(EndGhost.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((packet, buffer) -> { })
                .decoder(buffer -> new EndGhost())
                .consumerMainThread((packet, context) -> {
                    if (context.get().getSender() != null) GhostAbilityHandler.end(context.get().getSender());
                    context.get().setPacketHandled(true);
                }).add();
        CHANNEL.messageBuilder(FluidVision.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder((packet, buffer) -> buffer.writeBoolean(packet.enabled()))
                .decoder(buffer -> new FluidVision(buffer.readBoolean()))
                .consumerMainThread((packet, context) -> {
                    var player = context.get().getSender();
                    if (player != null) {
                        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                        if (helmet.is(FlightHelmetMod.FLIGHT_HELMET.get())) {
                            HelmetFluidVisionSettings.set(helmet, packet.enabled());
                            player.getInventory().setChanged();
                            player.inventoryMenu.broadcastChanges();
                        }
                    }
                    context.get().setPacketHandled(true);
                }).add();
    }
}
