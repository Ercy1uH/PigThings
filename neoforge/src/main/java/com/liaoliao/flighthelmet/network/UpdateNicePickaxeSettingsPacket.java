package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.NicePickaxeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateNicePickaxeSettingsPacket(boolean preciseMode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateNicePickaxeSettingsPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FlightHelmetMod.MOD_ID, "update_nice_pickaxe_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNicePickaxeSettingsPacket> STREAM_CODEC =
            StreamCodec.of(UpdateNicePickaxeSettingsPacket::encode, UpdateNicePickaxeSettingsPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, UpdateNicePickaxeSettingsPacket packet) {
        buffer.writeBoolean(packet.preciseMode);
    }

    private static UpdateNicePickaxeSettingsPacket decode(RegistryFriendlyByteBuf buffer) {
        return new UpdateNicePickaxeSettingsPacket(buffer.readBoolean());
    }

    public static void handle(UpdateNicePickaxeSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack pickaxe = getHeldPickaxe(player);
            if (pickaxe.isEmpty()) {
                return;
            }
            NicePickaxeItem.setPreciseMode(pickaxe, packet.preciseMode);
            NicePickaxeItem.applySilkTouch(pickaxe, packet.preciseMode, player.level().registryAccess());
            player.getInventory().setChanged();
        });
    }

    private static ItemStack getHeldPickaxe(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof NicePickaxeItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof NicePickaxeItem ? offHand : ItemStack.EMPTY;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
