package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightAbilityHandler;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateHelmetSettingsPacket(float speedMultiplier, boolean noInertia, boolean nightVision,
                                         boolean magnetEnabled, int magnetRangeX, int magnetRangeY, int magnetRangeZ)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateHelmetSettingsPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FlightHelmetMod.MOD_ID, "update_helmet_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHelmetSettingsPacket> STREAM_CODEC =
            StreamCodec.of(UpdateHelmetSettingsPacket::encode, UpdateHelmetSettingsPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, UpdateHelmetSettingsPacket packet) {
        buffer.writeFloat(packet.speedMultiplier);
        buffer.writeBoolean(packet.noInertia);
        buffer.writeBoolean(packet.nightVision);
        buffer.writeBoolean(packet.magnetEnabled);
        buffer.writeVarInt(packet.magnetRangeX);
        buffer.writeVarInt(packet.magnetRangeY);
        buffer.writeVarInt(packet.magnetRangeZ);
    }

    private static UpdateHelmetSettingsPacket decode(RegistryFriendlyByteBuf buffer) {
        return new UpdateHelmetSettingsPacket(buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(UpdateHelmetSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(FlightHelmetMod.FLIGHT_HELMET.get())) {
                FlightHelmetSettings.set(helmet, packet.speedMultiplier, packet.noInertia, packet.nightVision,
                        packet.magnetEnabled, packet.magnetRangeX, packet.magnetRangeY, packet.magnetRangeZ);
                player.getInventory().setChanged();
                FlightAbilityHandler.applyHelmetAbilities(player, helmet);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
