package com.liaoliao.flighthelmet;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DoubleChestHelper {
    private DoubleChestHelper() {
    }

    /**
     * 1.12.2 的双箱没有 SINGLE/LEFT/RIGHT 状态，只能按朝向推断：同方块、同朝向、位于朝向两侧的相邻箱子即为一对。
     */
    public static BlockPos getConnectedPosition(World world, BlockPos position) {
        IBlockState state = world.getBlockState(position);
        if (!(state.getBlock() instanceof BlockChest)) {
            return null;
        }
        EnumFacing facing = state.getValue(BlockChest.FACING);
        for (EnumFacing side : new EnumFacing[]{facing.rotateY(), facing.rotateYCCW()}) {
            BlockPos connectedPosition = position.offset(side);
            IBlockState connectedState = world.getBlockState(connectedPosition);
            if (connectedState.getBlock() == state.getBlock()
                    && connectedState.getValue(BlockChest.FACING) == facing) {
                return connectedPosition;
            }
        }
        return null;
    }
}
