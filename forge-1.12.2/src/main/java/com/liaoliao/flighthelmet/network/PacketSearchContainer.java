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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
        private static final int RESULT_NO_STORAGE = 0;
        private static final int RESULT_NO_MATCH = 1;
        private static final int RESULT_MATCH = 2;

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
            int inspected = 0;
            int withStorage = 0;

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
                inspected++;
                int result = inspect(player, pos, target);
                if (result != RESULT_NO_STORAGE) {
                    withStorage++;
                }
                if (result == RESULT_MATCH) {
                    matches.add(getContainerOrigin(player, pos));
                }
            }

            List<BlockPos> sortedMatches = new ArrayList<>(matches);
            sortedMatches.sort(Comparator.comparingDouble(position -> position.distanceSq(origin)));
            FlightHelmetMod.LOGGER.info(
                    "Container search finished: {} matches for {} x{} (scanned {} tile entities in range, {} with item storage)",
                    sortedMatches.size(), target.getDisplayName(), target.getCount(), inspected, withStorage);
            ModNetwork.CHANNEL.sendTo(new PacketSearchContainerResult(sortedMatches, target, openedContainer), player);
        }

        /**
         * 扫描一个方块实体：0 = 没有物品存储，1 = 有存储但没找到，2 = 命中。
         * 除了原版 IInventory，还查 Forge 能力接口，并且六个面都试一遍：
         * GregTech 的机器（板条箱就是 MetaTileEntityCrate）只对特定面暴露物品栏，
         * 只查 null 面会整台机器漏掉。
         */
        private static int inspect(EntityPlayerMP player, BlockPos pos, ItemStack target) {
            TileEntity tileEntity = player.world.getTileEntity(pos);
            if (tileEntity == null) {
                return RESULT_NO_STORAGE;
            }
            boolean hasStorage = false;
            if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                hasStorage = true;
                for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                    if (matchesExactly(inventory.getStackInSlot(slot), target)) {
                        return RESULT_MATCH;
                    }
                }
            }
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                hasStorage = true;
                if (handlerContains(handler, target)) {
                    return RESULT_MATCH;
                }
            }
            for (EnumFacing side : EnumFacing.values()) {
                IItemHandler sidedHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                if (sidedHandler == null || sidedHandler == handler) {
                    continue;
                }
                hasStorage = true;
                if (handlerContains(sidedHandler, target)) {
                    return RESULT_MATCH;
                }
            }
            return hasStorage ? RESULT_NO_MATCH : RESULT_NO_STORAGE;
        }

        private static boolean handlerContains(IItemHandler handler, ItemStack target) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (matchesExactly(handler.getStackInSlot(slot), target)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 连体容器（双箱、陷阱双箱等）会被扫描到多次，必须规范化成同一个代表位置，
         * 否则一个双箱会占两条结果、提示出现“2/2”。
         */
        private static BlockPos getContainerOrigin(EntityPlayerMP player, BlockPos pos) {
            return DoubleChestHelper.getContainerOrigin(player.world, pos);
        }

        /**
         * 比对物品 + 损伤值(metadata) + NBT，忽略堆叠数量。
         * 不能用 ItemStack.areItemStacksEqual：1.12.2 里它转调 isItemStackEqual，把 stackSize 也比进去，
         * 于是只有"堆叠数量和搜索目标完全相同"的容器才会命中。
         * 1.12.2 的物品变体走 Damage（GregTech 的 meta_ingot 等），所以不能只比 NBT。
         */
        private static boolean matchesExactly(ItemStack stored, ItemStack target) {
            return !stored.isEmpty() && !target.isEmpty()
                    && stored.getItem() == target.getItem()
                    && stored.getItemDamage() == target.getItemDamage()
                    && ItemStack.areItemStackTagsEqual(stored, target);
        }
    }
}
