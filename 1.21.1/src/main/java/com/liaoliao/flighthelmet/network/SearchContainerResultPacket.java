package com.liaoliao.flighthelmet.network;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.client.ContainerSearchClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SearchContainerResultPacket(List<BlockPos> positions, ItemStack target, boolean openedContainer)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SearchContainerResultPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FlightHelmetMod.MOD_ID, "search_container_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SearchContainerResultPacket> STREAM_CODEC =
            StreamCodec.of(SearchContainerResultPacket::encode, SearchContainerResultPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, SearchContainerResultPacket packet) {
        buffer.writeVarInt(packet.positions.size());
        for (BlockPos position : packet.positions) {
            buffer.writeBlockPos(position);
        }
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.target);
        buffer.writeBoolean(packet.openedContainer);
    }

    private static SearchContainerResultPacket decode(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<BlockPos> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(buffer.readBlockPos());
        }
        ItemStack target = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return new SearchContainerResultPacket(positions, target, buffer.readBoolean());
    }

    public static void handle(SearchContainerResultPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                ContainerSearchClient.show(packet.positions, packet.target, packet.openedContainer));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
