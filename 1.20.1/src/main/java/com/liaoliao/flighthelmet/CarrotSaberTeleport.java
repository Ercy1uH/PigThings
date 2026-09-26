package com.liaoliao.flighthelmet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.fml.ModList;

public final class CarrotSaberTeleport {
    private CarrotSaberTeleport() { }

    // Ender IO uses an exact item equality test; its HUD and range selection also need this hook.
    public static boolean canItemTeleport(boolean original, Player player) {
        return original || player.getMainHandItem().getItem() instanceof CarrotSaberItem
                || player.getOffhandItem().getItem() instanceof CarrotSaberItem;
    }

    public static boolean travel(Player player) {
        if (!(player instanceof ServerPlayer) || !player.isAlive() || player.isSpectator()
                || player.isPassenger() || player.isSleeping()) return false;
        if (ModList.get().isLoaded("enderio")) {
            return com.liaoliao.flighthelmet.compat.EnderIOTravel.travel(player);
        }
        if (!player.isShiftKeyDown() || !blink(player)) return false;
        player.getCooldowns().addCooldown(FlightHelmetMod.CARROT_SABER.get(), 10);
        return true;
    }

    public static boolean blink(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        Level level = player.level();
        Vec3 start = player.position();
        Vec3 look = player.getLookAngle().normalize();
        AABB current = player.getBoundingBox();
        AABB body = new AABB(current.minX, current.minY, current.minZ, current.maxX,
                current.minY + Math.max(1.8, player.getBbHeight()), current.maxZ);
        Vec3 destination = null;
        // Validate the entire swept body, including thin block collision shapes and liquids.
        for (double distance = 0.25; distance <= 16; distance += 0.25) {
            Vec3 offset = look.scale(distance);
            if (!clear(level, player, body.expandTowards(offset))) break;
            if (distance >= 1) destination = start.add(offset);
        }
        if (destination == null) return false;
        EntityTeleportEvent event = new EntityTeleportEvent(player, destination.x, destination.y, destination.z);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;
        destination = new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        if (!Double.isFinite(destination.x) || !Double.isFinite(destination.y) || !Double.isFinite(destination.z)
                || !clear(level, player, body.move(destination.subtract(start)))) return false;
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        serverPlayer.connection.teleport(destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        player.fallDistance = 0;
        player.setDeltaMovement(Vec3.ZERO);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        return true;
    }

    private static boolean clear(Level level, Player player, AABB box) {
        return box.minY >= level.getMinBuildHeight() && box.maxY <= level.getMaxBuildHeight()
                && level.getWorldBorder().isWithinBounds(box)
                && level.hasChunksAt(BlockPos.containing(box.minX, box.minY, box.minZ),
                        BlockPos.containing(box.maxX, box.maxY, box.maxZ))
                && level.noCollision(player, box) && !level.containsAnyLiquid(box);
    }
}
