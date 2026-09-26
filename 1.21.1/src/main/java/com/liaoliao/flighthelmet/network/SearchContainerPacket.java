package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.DoubleChestHelper;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SearchContainerPacket(ItemStack target, boolean openedContainer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SearchContainerPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FlightHelmetMod.MOD_ID, "search_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SearchContainerPacket> STREAM_CODEC =
            StreamCodec.of(SearchContainerPacket::encode, SearchContainerPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, SearchContainerPacket packet) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.target);
        buffer.writeBoolean(packet.openedContainer);
    }

    private static SearchContainerPacket decode(RegistryFriendlyByteBuf buffer) {
        ItemStack target = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return new SearchContainerPacket(target, buffer.readBoolean());
    }

    public static void handle(SearchContainerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || packet.target.isEmpty()) {
                return;
            }
            int rangeX = FlightHelmetMod.SEARCH_RANGE_X.get();
            int rangeY = FlightHelmetMod.SEARCH_RANGE_Y.get();
            int rangeZ = FlightHelmetMod.SEARCH_RANGE_Z.get();
            BlockPos origin = player.blockPosition();
            Set<BlockPos> matches = new HashSet<>();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for (int x = -rangeX; x <= rangeX; x++) {
                for (int y = -rangeY; y <= rangeY; y++) {
                    for (int z = -rangeZ; z <= rangeZ; z++) {
                        cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                        BlockEntity blockEntity = player.level().getBlockEntity(cursor);
                        if (blockEntity != null && contains(player, cursor, packet.target)) {
                            matches.add(getContainerOrigin(player, cursor));
                        }
                    }
                }
            }

            List<BlockPos> sortedMatches = new ArrayList<>(matches);
            sortedMatches.sort(Comparator.comparingDouble(position -> position.distSqr(origin)));
            PacketDistributor.sendToPlayer(player,
                    new SearchContainerResultPacket(sortedMatches, packet.target, packet.openedContainer));
        });
    }

    private static boolean contains(ServerPlayer player, BlockPos pos, ItemStack target) {
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof Container container && containsContainer(container, target)) {
            return true;
        }
        IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        return handler != null && containsHandler(handler, target);
    }

    private static BlockPos getContainerOrigin(ServerPlayer player, BlockPos pos) {
        BlockPos connectedPos = DoubleChestHelper.getConnectedPosition(player.level(), pos);
        if (connectedPos == null) {
            return pos.immutable();
        }
        return pos.getY() <= connectedPos.getY() ? pos.immutable() : connectedPos.immutable();
    }

    private static boolean containsContainer(Container container, ItemStack target) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (matchesExactly(container.getItem(i), target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsHandler(IItemHandler handler, ItemStack target) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (matchesExactly(handler.getStackInSlot(i), target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesExactly(ItemStack stored, ItemStack target) {
        if (stored.isEmpty() || target.isEmpty() || stored.getItem() != target.getItem()) {
            return false;
        }
        return Objects.equals(customData(stored), customData(target));
    }

    private static Object customData(ItemStack stack) {
        return stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
