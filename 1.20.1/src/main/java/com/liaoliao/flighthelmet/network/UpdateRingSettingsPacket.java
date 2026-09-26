package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightAbilityHandler;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.RingHelper;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record UpdateRingSettingsPacket(float speedMultiplier, boolean noInertia, boolean nightVision,
                                       boolean magnetEnabled, int magnetRangeX, int magnetRangeY,
                                       int magnetRangeZ) {
    public static void encode(UpdateRingSettingsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.speedMultiplier);
        buffer.writeBoolean(packet.noInertia);
        buffer.writeBoolean(packet.nightVision);
        buffer.writeBoolean(packet.magnetEnabled);
        buffer.writeInt(packet.magnetRangeX);
        buffer.writeInt(packet.magnetRangeY);
        buffer.writeInt(packet.magnetRangeZ);
    }

    public static UpdateRingSettingsPacket decode(FriendlyByteBuf buffer) {
        return new UpdateRingSettingsPacket(buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(UpdateRingSettingsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || RingHelper.hasHelmet(player)) {
                return;
            }
            ItemStack ring = RingHelper.getEquippedRing(player);
            if (ring.isEmpty()) {
                return;
            }
            FlightHelmetSettings.set(ring, packet.speedMultiplier, packet.noInertia, packet.nightVision,
                    packet.magnetEnabled, packet.magnetRangeX, packet.magnetRangeY, packet.magnetRangeZ);
            player.getInventory().setChanged();
            FlightAbilityHandler.applyHelmetAbilities(player, ring);
        });
        context.setPacketHandled(true);
    }
}
