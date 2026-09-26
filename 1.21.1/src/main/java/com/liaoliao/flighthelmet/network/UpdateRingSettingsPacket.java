package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightAbilityHandler;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.RingHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateRingSettingsPacket(float speedMultiplier, boolean noInertia, boolean nightVision,
                                       boolean magnetEnabled, int magnetRangeX, int magnetRangeY, int magnetRangeZ)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateRingSettingsPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FlightHelmetMod.MOD_ID, "update_ring_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRingSettingsPacket> STREAM_CODEC =
            StreamCodec.of(UpdateRingSettingsPacket::encode, UpdateRingSettingsPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, UpdateRingSettingsPacket packet) {
        buffer.writeFloat(packet.speedMultiplier);
        buffer.writeBoolean(packet.noInertia);
        buffer.writeBoolean(packet.nightVision);
        buffer.writeBoolean(packet.magnetEnabled);
        buffer.writeVarInt(packet.magnetRangeX);
        buffer.writeVarInt(packet.magnetRangeY);
        buffer.writeVarInt(packet.magnetRangeZ);
    }

    private static UpdateRingSettingsPacket decode(RegistryFriendlyByteBuf buffer) {
        return new UpdateRingSettingsPacket(buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(UpdateRingSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || RingHelper.hasHelmet(player)) {
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
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
