package com.liaoliao.flighthelmet.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 障碍破坏者右键破坏一格：由客户端在「首次按下」时发出，服务端据此破坏方块。
 * 替换掉 1.20.1 里靠 onItemUseFirst 标志实现的方案——1.12.2 客户端按住右键每 4 tick 都会重发包，
 * 服务端拿不到按下/松开状态，只能由客户端决定何时破坏。
 */
public class PacketBreakBlock implements IMessage {
    private BlockPos pos = BlockPos.ORIGIN;

    public PacketBreakBlock() {
    }

    public PacketBreakBlock(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new PacketBuffer(buf).readBlockPos();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeBlockPos(this.pos);
    }

    public static class Handler implements IMessageHandler<PacketBreakBlock, IMessage> {
        @Override
        public IMessage onMessage(PacketBreakBlock message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            BlockPos pos = message.pos;
            player.getServerWorld().addScheduledTask(() -> breakBlock(player, pos));
            return null;
        }

        private static void breakBlock(EntityPlayerMP player, BlockPos pos) {
            if (com.liaoliao.flighthelmet.GhostAbilityHandler.isActive(player)) {
                com.liaoliao.flighthelmet.GhostAbilityHandler.end(player);
                return;
            }
            if (player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            World world = player.world;
            IBlockState state = world.getBlockState(pos);
            if (world.isAirBlock(pos) || state.getBlockHardness(world, pos) < 0.0F) {
                return;
            }
            // 自带破坏音效与粒子（playEvent 2001），并会走 ForgeHooks.onBlockBreakEvent。
            if (player.interactionManager.tryHarvestBlock(pos)) {
                // tryHarvestBlock 的 playEvent(player, 2001, ...) 用 sendToAllNearExcept 把破坏者排除在外，
                // 原版左键挖掘时音效是本机客户端自己播的；这条链路没人补，所以单独给破坏者发一次特效包。
                player.connection.sendPacket(new SPacketEffect(2001, pos, Block.getStateId(state), false));
            }
        }
    }
}
