package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.GhostAbilityHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketEndGhost implements IMessage {
    @Override public void fromBytes(ByteBuf buf) { }
    @Override public void toBytes(ByteBuf buf) { }

    public static final class Handler implements IMessageHandler<PacketEndGhost, IMessage> {
        @Override
        public IMessage onMessage(PacketEndGhost message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> GhostAbilityHandler.end(player));
            return null;
        }
    }
}
