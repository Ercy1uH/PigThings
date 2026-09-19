package com.liaoliao.flighthelmet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.BlockChest;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 判断两个箱子是否真的被游戏当成一个大箱子，只按这一点合并，绝不按"相邻"合并。
 *
 * 1.12.2 原版没有 SINGLE/LEFT/RIGHT 状态，相邻的同类型箱子一定是大箱子；但整合包常装
 * 1.13 式的箱子模组（如 Friendly Chests / Quark 的箱子），它们会给 BlockChest 加一个
 * "type" 状态：SINGLE = 独立箱子（可以紧挨着放但互不相通），LEFT/RIGHT 才是合体的一半。
 * 所以这里先看状态：有 type 就按 type 判断；没有（纯原版）才退回"同类箱子横向相邻即合体"。
 */
public final class DoubleChestHelper {
    private static final EnumFacing[] HORIZONTALS = new EnumFacing[]{
            EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH};

    private DoubleChestHelper() {
    }

    /** 返回与 position 合体成同一个容器的其它箱子位置（不含自身）；独立箱子返回空表。 */
    public static List<BlockPos> getConnectedPositions(World world, BlockPos position) {
        List<BlockPos> connected = new ArrayList<>();
        IBlockState state = getChestState(world, position);
        if (state == null) {
            return connected;
        }

        String chestType = getChestTypeName(state);
        if ("single".equals(chestType)) {
            // 1.13 式箱子：显式标记为独立，紧挨着也不算同一个容器。
            return connected;
        }

        BlockChest.Type vanillaType = ((BlockChest) state.getBlock()).chestType;
        EnumFacing facing = state.getValue(BlockChest.FACING);
        for (EnumFacing side : HORIZONTALS) {
            BlockPos neighborPos = position.offset(side).toImmutable();
            IBlockState neighborState = getChestState(world, neighborPos);
            if (neighborState == null || neighborState.getBlock() != state.getBlock()) {
                // 普通箱子与陷阱箱各自成对（原版 BlockChest#getContainer 的规则）。
                continue;
            }
            if (((BlockChest) neighborState.getBlock()).chestType != vanillaType) {
                continue;
            }
            if (chestType != null) {
                String neighborType = getChestTypeName(neighborState);
                if (!isOppositeChestType(chestType, neighborType)) {
                    continue;
                }
                // 合体的两半朝向一致（Friendly Chests 放置时会把另一半点成同一朝向）。
                if (neighborState.getValue(BlockChest.FACING) != facing) {
                    continue;
                }
            }
            connected.add(neighborPos);
        }
        return connected;
    }

    /**
     * 把同一个大箱子里的任意一半规范化成同一个代表位置（x 最小、其次 z 最小），
     * 这样大箱子被扫描到两次时只会留下一条搜索结果，而独立箱子各自留一条。
     */
    public static BlockPos getContainerOrigin(World world, BlockPos position) {
        BlockPos origin = position;
        for (BlockPos connected : getConnectedPositions(world, position)) {
            if (connected.getX() < origin.getX()
                    || (connected.getX() == origin.getX() && connected.getZ() < origin.getZ())) {
                origin = connected;
            }
        }
        return origin;
    }

    private static IBlockState getChestState(World world, BlockPos position) {
        if (!world.isBlockLoaded(position)) {
            return null;
        }
        IBlockState state = world.getBlockState(position);
        return state.getBlock() instanceof BlockChest ? state : null;
    }

    /**
     * 读取"是否合体"的状态值（Friendly Chests 的 ChestType、Quark 的同名状态都叫 single/left/right）。
     * 值按枚举常量名比较，所以不依赖这些模组的类，纯原版返回 null。
     */
    private static String getChestTypeName(IBlockState state) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            @SuppressWarnings("unchecked")
            Object value = state.getValue((IProperty) property);
            if (!(value instanceof Enum)) {
                continue;
            }
            String name = ((Enum<?>) value).name().toLowerCase(Locale.ROOT);
            if ("single".equals(name) || "left".equals(name) || "right".equals(name)) {
                return name;
            }
        }
        return null;
    }

    private static boolean isOppositeChestType(String chestType, String neighborType) {
        return ("left".equals(chestType) && "right".equals(neighborType))
                || ("right".equals(chestType) && "left".equals(neighborType));
    }
}
