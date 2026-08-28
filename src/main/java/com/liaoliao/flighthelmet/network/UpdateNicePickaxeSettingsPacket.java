package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.NicePickaxeItem;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record UpdateNicePickaxeSettingsPacket(boolean preciseMode) {
    public static void encode(UpdateNicePickaxeSettingsPacket packet, net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.preciseMode);
    }

    public static UpdateNicePickaxeSettingsPacket decode(net.minecraft.network.FriendlyByteBuf buffer) {
        return new UpdateNicePickaxeSettingsPacket(buffer.readBoolean());
    }

    public static void handle(UpdateNicePickaxeSettingsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack pickaxe = getHeldPickaxe(player);
            if (pickaxe.isEmpty()) {
                return;
            }
            NicePickaxeItem.setSettings(pickaxe, packet.preciseMode());
            player.getInventory().setChanged();
        });
        context.setPacketHandled(true);
    }

    private static ItemStack getHeldPickaxe(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof NicePickaxeItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof NicePickaxeItem ? offHand : ItemStack.EMPTY;
    }
}
