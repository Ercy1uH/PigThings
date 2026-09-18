package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightAbilityHandler;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.RingHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateRingSettings implements IMessage {
    private float speedMultiplier;
    private boolean noInertia;
    private boolean nightVision;
    private boolean magnetEnabled;
    private int magnetRangeX;
    private int magnetRangeY;
    private int magnetRangeZ;

    public PacketUpdateRingSettings() {
    }

    public PacketUpdateRingSettings(float speedMultiplier, boolean noInertia, boolean nightVision,
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

    public static class Handler implements IMessageHandler<PacketUpdateRingSettings, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateRingSettings message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (RingHelper.hasHelmet(player)) {
                    return;
                }
                ItemStack ring = RingHelper.getEquippedRing(player);
                if (ring.isEmpty()) {
                    return;
                }
                FlightHelmetSettings.set(ring, message.speedMultiplier, message.noInertia, message.nightVision,
                        message.magnetEnabled, message.magnetRangeX, message.magnetRangeY, message.magnetRangeZ);
                FlightAbilityHandler.applyHelmetAbilities(player, ring);
            });
            return null;
        }
    }
}
