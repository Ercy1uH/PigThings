package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.DoubleChestHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ContainerSearchClient {
    private static ItemStack previousTarget = ItemStack.EMPTY;
    private static List<BlockPos> previousPositions = Collections.emptyList();
    private static int nextIndex;
    private static int remainingTicks;

    private ContainerSearchClient() {
    }

    public static void show(List<BlockPos> result, ItemStack target, boolean openedContainer) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (result.isEmpty()) {
            previousPositions = Collections.emptyList();
            nextIndex = 0;
            if (player != null) {
                player.sendStatusMessage(new TextComponentTranslation("message.pigthings.search_result", 0), true);
            }
            return;
        }

        if (!ItemStack.areItemStacksEqual(previousTarget, target) || !previousPositions.equals(result)) {
            previousTarget = target.copy();
            previousPositions = new ArrayList<>(result);
            nextIndex = 0;
        }

        BlockPos selectedPosition = previousPositions.get(nextIndex);
        nextIndex = (nextIndex + 1) % previousPositions.size();
        remainingTicks = 20 * (openedContainer ? 5 : 7);
        if (player != null) {
            facePosition(player, selectedPosition);
            player.sendStatusMessage(new TextComponentTranslation("message.pigthings.search_target",
                    nextIndex == 0 ? previousPositions.size() : nextIndex, previousPositions.size()), true);
        }
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static void render() {
        if (remainingTicks <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        if (world == null) {
            return;
        }
        float alpha = remainingTicks / 4 % 2 == 0 ? 1.0F : 0.12F;
        RenderManager renderManager = minecraft.getRenderManager();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        for (BlockPos position : previousPositions) {
            AxisAlignedBB box = getContainerBox(world, position).grow(0.03D);
            RenderGlobal.drawSelectionBoundingBox(box, 1.0F, 0.05F, 0.05F, alpha);
        }

        GlStateManager.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static AxisAlignedBB getContainerBox(World world, BlockPos position) {
        AxisAlignedBB box = new AxisAlignedBB(position);
        for (BlockPos connectedPos : DoubleChestHelper.getConnectedPositions(world, position)) {
            box = box.union(new AxisAlignedBB(connectedPos));
        }
        return box;
    }

    private static void facePosition(EntityPlayerSP player, BlockPos position) {
        double dx = position.getX() + 0.5D - player.posX;
        double dy = position.getY() + 0.5D - (player.posY + player.getEyeHeight());
        double dz = position.getZ() + 0.5D - player.posZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));
        player.rotationYaw = yaw;
        player.prevRotationYaw = yaw;
        player.rotationPitch = pitch;
        player.prevRotationPitch = pitch;
    }
}
