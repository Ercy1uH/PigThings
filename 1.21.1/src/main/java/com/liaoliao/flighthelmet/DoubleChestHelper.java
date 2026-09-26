package com.liaoliao.flighthelmet;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class DoubleChestHelper {
    private static final String SOPHISTICATED_CHEST_BLOCK = "net.p3pp3rf1y.sophisticatedstorage.block.ChestBlock";

    private DoubleChestHelper() {
    }

    public static BlockPos getConnectedPosition(LevelAccessor level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        Direction direction = getConnectedDirection(state);
        if (direction == null) {
            return null;
        }
        BlockPos connectedPosition = position.relative(direction);
        BlockState connectedState = level.getBlockState(connectedPosition);
        return connectedState.getBlock() == state.getBlock() ? connectedPosition : null;
    }

    private static Direction getConnectedDirection(BlockState state) {
        if (state.getBlock() instanceof ChestBlock) {
            return state.getValue(ChestBlock.TYPE) == ChestType.SINGLE ? null : ChestBlock.getConnectedDirection(state);
        } else if (!state.getBlock().getClass().getName().equals(SOPHISTICATED_CHEST_BLOCK)) {
            return null;
        }
        try {
            Method method = state.getBlock().getClass().getMethod("getConnectedDirection", BlockState.class);
            return method.invoke(null, state) instanceof Direction direction ? direction : null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
