package com.liaoliao.flighthelmet;

import java.util.function.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class CarrotSaberTeleport {
    private static final double RANGE = 16.0;
    private static final double STEP = 0.25;

    private CarrotSaberTeleport() {
    }

    public static boolean blink(EntityPlayer player) {
        World world = player.world;
        if (world.isRemote || !player.isEntityAlive() || player.isSpectator()
                || player.isRiding() || player.isPlayerSleeping()) {
            return false;
        }
        Vec3d start = player.getPositionVector();
        AxisAlignedBB current = player.getEntityBoundingBox();
        // Reserve standing headroom even if the player is currently crouching.
        AxisAlignedBB body = new AxisAlignedBB(current.minX, current.minY, current.minZ,
                current.maxX, current.minY + Math.max(1.8, player.height), current.maxZ);
        Vec3d destination = findDestination(start, player.getLookVec(), point -> {
            AxisAlignedBB box = body.offset(point.x - start.x, point.y - start.y, point.z - start.z);
            return box.minY >= 0 && box.maxY <= world.getHeight()
                    && world.getWorldBorder().contains(box)
                    && world.isAreaLoaded(new BlockPos(box.minX, box.minY, box.minZ),
                            new BlockPos(box.maxX, box.maxY, box.maxZ))
                    && world.getCollisionBoxes(player, box).isEmpty()
                    && !world.containsAnyLiquid(box);
        });
        if (destination == null) {
            return false;
        }
        world.playSound(null, start.x, start.y, start.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.setPositionAndUpdate(destination.x, destination.y, destination.z);
        player.fallDistance = 0.0F;
        player.motionX = player.motionY = player.motionZ = 0.0;
        world.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    static Vec3d findDestination(Vec3d start, Vec3d direction, Predicate<Vec3d> isClear) {
        Vec3d look = direction.normalize();
        Vec3d destination = null;
        // Check the entire route, so a clear endpoint never permits crossing a wall.
        for (double distance = STEP; distance <= RANGE; distance += STEP) {
            Vec3d candidate = start.add(look.scale(distance));
            if (!isClear.test(candidate)) {
                break;
            }
            if (distance >= 1.0) {
                destination = candidate;
            }
        }
        return destination;
    }
}
