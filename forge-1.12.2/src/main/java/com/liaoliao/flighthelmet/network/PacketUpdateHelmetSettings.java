package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightAbilityHandler;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateHelmetSettings implements IMessage {
    private float speedMultiplier;
    private boolean noInertia;
    private boolean nightVision;
    private boolean magnetEnabled;
    private int magnetRangeX;
    private int magnetRangeY;
    private int magnetRangeZ;

    public PacketUpdateHelmetSettings() {
    }

    public PacketUpdateHelmetSettings(float speedMultiplier, boolean noInertia, boolean nightVision,
                                      boolean magnetEnabled, int magnetRangeX, int magnetRangeY, int magnetRangeZ) {
        this.speedMultiplier = speedMultiplier;
        this.noInertia = noInertia;
        this.nightVision = nightVision;
        this.magnetEnabled = magnetEnabled;
        this.magnetRangeX = magnetRangeX;
        this.magnetRangeY = magnetRangeY;
        this.magnetRangeZ = magnetRangeZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.speedMultiplier = buf.readFloat();
        this.noInertia = buf.readBoolean();
        this.nightVision = buf.readBoolean();
        this.magnetEnabled = buf.readBoolean();
        this.magnetRangeX = buf.readInt();
        this.magnetRangeY = buf.readInt();
        this.magnetRangeZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(this.speedMultiplier);
        buf.writeBoolean(this.noInertia);
        buf.writeBoolean(this.nightVision);
        buf.writeBoolean(this.magnetEnabled);
        buf.writeInt(this.magnetRangeX);
        buf.writeInt(this.magnetRangeY);
        buf.writeInt(this.magnetRangeZ);
    }

    public static class Handler implements IMessageHandler<PacketUpdateHelmetSettings, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateHelmetSettings message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                if (helmet.getItem() != FlightHelmetMod.FLIGHT_HELMET) {
                    return;
                }
                FlightHelmetSettings.set(helmet, message.speedMultiplier, message.noInertia, message.nightVision,
                        message.magnetEnabled, message.magnetRangeX, message.magnetRangeY, message.magnetRangeZ);
                player.inventory.markDirty();
                FlightAbilityHandler.applyHelmetAbilities(player, helmet);
            });
            return null;
        }
    }
}
