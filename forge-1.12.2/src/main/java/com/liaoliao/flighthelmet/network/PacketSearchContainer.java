package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.DoubleChestHelper;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PacketSearchContainer implements IMessage {
    private ItemStack target = ItemStack.EMPTY;
    private boolean openedContainer;

    public PacketSearchContainer() {
    }

    public PacketSearchContainer(ItemStack target, boolean openedContainer) {
        this.target = target;
        this.openedContainer = openedContainer;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.target = ByteBufUtils.readItemStack(buf);
        this.openedContainer = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.target);
        buf.writeBoolean(this.openedContainer);
    }

    public static class Handler implements IMessageHandler<PacketSearchContainer, IMessage> {
        @Override
        public IMessage onMessage(PacketSearchContainer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack target = message.target;
            boolean openedContainer = message.openedContainer;
            player.getServerWorld().addScheduledTask(() -> search(player, target, openedContainer));
            return null;
        }

        private static void search(EntityPlayerMP player, ItemStack target, boolean openedContainer) {
            if (target.isEmpty()) {
                return;
            }
            BlockPos origin = new BlockPos(player.posX, player.posY, player.posZ);
            int rangeX = FlightHelmetMod.SEARCH_RANGE_X;
            int rangeY = FlightHelmetMod.SEARCH_RANGE_Y;
            int rangeZ = FlightHelmetMod.SEARCH_RANGE_Z;
            Set<BlockPos> matches = new HashSet<>();

            for (TileEntity tileEntity : new ArrayList<>(player.world.loadedTileEntityList)) {
                if (tileEntity == null || tileEntity.isInvalid()) {
                    continue;
                }
                BlockPos pos = tileEntity.getPos();
                if (Math.abs(pos.getX() - origin.getX()) > rangeX
                        || Math.abs(pos.getY() - origin.getY()) > rangeY
                        || Math.abs(pos.getZ() - origin.getZ()) > rangeZ) {
                    continue;
                }
                if (contains(player, pos, target)) {
                    matches.add(getContainerOrigin(player, pos));
                }
            }

            List<BlockPos> sortedMatches = new ArrayList<>(matches);
            sortedMatches.sort(Comparator.comparingDouble(position -> position.distanceSq(origin)));
            ModNetwork.CHANNEL.sendTo(new PacketSearchContainerResult(sortedMatches, target, openedContainer), player);
        }

        private static boolean contains(EntityPlayerMP player, BlockPos pos, ItemStack target) {
            TileEntity tileEntity = player.world.getTileEntity(pos);
            if (tileEntity == null) {
                return false;
            }
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                    if (matchesExactly(inventory.getStackInSlot(slot), target)) {
                        return true;
                    }
                }
            }
            if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        if (matchesExactly(handler.getStackInSlot(slot), target)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private static BlockPos getContainerOrigin(EntityPlayerMP player, BlockPos pos) {
            BlockPos connectedPos = DoubleChestHelper.getConnectedPosition(player.world, pos);
            if (connectedPos == null) {
                return pos;
            }
            return pos.getY() <= connectedPos.getY() ? pos : connectedPos;
        }

        private static boolean matchesExactly(ItemStack stored, ItemStack target) {
            if (stored.isEmpty() || target.isEmpty() || stored.getItem() != target.getItem()) {
                return false;
            }
            NBTTagCompound storedTag = stored.getTagCompound();
            NBTTagCompound targetTag = target.getTagCompound();
            return storedTag == null ? targetTag == null : storedTag.equals(targetTag);
        }
    }
}
