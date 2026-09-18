package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.NicePickaxeItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdatePickaxeSettings implements IMessage {
    private boolean preciseMode;

    public PacketUpdatePickaxeSettings() {
    }

    public PacketUpdatePickaxeSettings(boolean preciseMode) {
        this.preciseMode = preciseMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.preciseMode = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.preciseMode);
    }

    public static class Handler implements IMessageHandler<PacketUpdatePickaxeSettings, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdatePickaxeSettings message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack pickaxe = getHeldPickaxe(player);
                if (pickaxe.isEmpty()) {
                    return;
                }
                NicePickaxeItem.setPreciseMode(pickaxe, message.preciseMode);
                NicePickaxeItem.applySilkTouch(pickaxe, message.preciseMode);
                player.inventory.markDirty();
            });
            return null;
        }

        private static ItemStack getHeldPickaxe(EntityPlayerMP player) {
            ItemStack mainHand = player.getHeldItemMainhand();
            if (mainHand.getItem() instanceof NicePickaxeItem) {
                return mainHand;
            }
            ItemStack offHand = player.getHeldItemOffhand();
            return offHand.getItem() instanceof NicePickaxeItem ? offHand : ItemStack.EMPTY;
        }
    }
}
