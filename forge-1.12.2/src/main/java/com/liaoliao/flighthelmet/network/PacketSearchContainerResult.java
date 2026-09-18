package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.client.ContainerSearchClient;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSearchContainerResult implements IMessage {
    private List<BlockPos> positions = new ArrayList<>();
    private ItemStack target = ItemStack.EMPTY;
    private boolean openedContainer;

    public PacketSearchContainerResult() {
    }

    public PacketSearchContainerResult(List<BlockPos> positions, ItemStack target, boolean openedContainer) {
        this.positions = positions;
        this.target = target;
        this.openedContainer = openedContainer;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        int count = buffer.readVarInt();
        this.positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.positions.add(buffer.readBlockPos());
        }
        this.target = ByteBufUtils.readItemStack(buf);
        this.openedContainer = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarInt(this.positions.size());
        for (BlockPos position : this.positions) {
            buffer.writeBlockPos(position);
        }
        ByteBufUtils.writeItemStack(buf, this.target);
        buf.writeBoolean(this.openedContainer);
    }

    public static class Handler implements IMessageHandler<PacketSearchContainerResult, IMessage> {
        @Override
        public IMessage onMessage(PacketSearchContainerResult message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    ContainerSearchClient.show(message.positions, message.target, message.openedContainer));
            return null;
        }
    }
}
