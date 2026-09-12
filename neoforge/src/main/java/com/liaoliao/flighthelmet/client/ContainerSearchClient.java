package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.DoubleChestHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

public final class ContainerSearchClient {
    private static BlockPos selectedPosition;
    private static ItemStack previousTarget = ItemStack.EMPTY;
    private static List<BlockPos> previousPositions = List.of();
    private static int nextIndex;
    private static int remainingTicks;

    private ContainerSearchClient() {
    }

    public static void show(List<BlockPos> result, ItemStack target, boolean openedContainer) {
        Minecraft minecraft = Minecraft.getInstance();
        if (result.isEmpty()) {
            selectedPosition = null;
            previousPositions = List.of();
            nextIndex = 0;
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.pigthings.search_result", 0), true);
            }
            return;
        }

        if (!ItemStack.matches(previousTarget, target) || !previousPositions.equals(result)) {
            previousTarget = target.copy();
            previousPositions = new ArrayList<>(result);
            nextIndex = 0;
        }

        selectedPosition = previousPositions.get(nextIndex);
        nextIndex = (nextIndex + 1) % previousPositions.size();
        remainingTicks = 20 * (openedContainer ? 5 : 7);
        if (minecraft.player != null) {
            minecraft.player.lookAt(Anchor.EYES, Vec3.atCenterOf(selectedPosition));
            minecraft.player.displayClientMessage(Component.translatable("message.pigthings.search_target",
                    nextIndex == 0 ? previousPositions.size() : nextIndex, previousPositions.size()), true);
        }
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static void render(RenderLevelStageEvent event) {
        if (remainingTicks <= 0 || event.getStage() != Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float alpha = remainingTicks / 4 % 2 == 0 ? 1.0F : 0.12F;
        PoseStack poseStack = event.getPoseStack();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        BufferSource buffer = minecraft.renderBuffers().bufferSource();
        VertexConsumer lineBuffer = buffer.getBuffer(RenderType.lines());

        for (BlockPos position : previousPositions) {
            AABB box = getContainerBox(minecraft, position).inflate(0.03);
            LevelRenderer.renderLineBox(poseStack, lineBuffer, box, 1.0F, 0.05F, 0.05F, alpha);
        }

        buffer.endBatch();
        poseStack.popPose();
    }

    private static AABB getContainerBox(Minecraft minecraft, BlockPos position) {
        AABB box = new AABB(position);
        BlockPos connectedPos = DoubleChestHelper.getConnectedPosition(minecraft.level, position);
        return connectedPos != null ? box.minmax(new AABB(connectedPos)) : box;
    }
}
