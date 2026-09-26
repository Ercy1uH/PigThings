package com.liaoliao.flighthelmet;

import com.liaoliao.flighthelmet.network.AbilityNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID)
public final class GhostAbilityHandler {
    private static final String STATE = "PigThingsGhostState";

    private GhostAbilityHandler() { }

    public static boolean isActive(Player player) {
        return player.hasEffect(FlightHelmetMod.GHOST.get());
    }

    public static boolean ownsAbilities(Player player) {
        return isActive(player) || player.getPersistentData().contains(STATE);
    }

    public static void activate(Player player) {
        snapshot(player);
        player.stopRiding();
        player.stopFallFlying();
        player.addEffect(new MobEffectInstance(FlightHelmetMod.GHOST.get(), 600, 0, false, false, true));
        player.setDeltaMovement(Vec3.ZERO);
        enforce(player);
        player.onUpdateAbilities();
    }

    private static void snapshot(Player player) {
        if (player.getPersistentData().contains(STATE)) return;
        CompoundTag state = new CompoundTag();
        state.putBoolean("MayFly", player.getAbilities().mayfly);
        state.putBoolean("Flying", player.getAbilities().flying);
        state.putBoolean("Invulnerable", player.getAbilities().invulnerable);
        player.getPersistentData().put(STATE, state);
    }

    private static void enforce(Player player) {
        player.noPhysics = true;
        boolean changed = !player.getAbilities().mayfly || !player.getAbilities().flying
                || !player.getAbilities().invulnerable;
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        if (!player.level().isClientSide) player.getAbilities().invulnerable = true;
        player.setOnGround(false);
        player.fallDistance = 0;
        player.clearFire();
        player.setAirSupply(player.getMaxAirSupply());
        double bottom = player.level().getMinBuildHeight() + 1;
        if (player.getY() < bottom) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(player.getX(), bottom, player.getZ(), player.getYRot(), player.getXRot());
            } else player.setPos(player.getX(), bottom, player.getZ());
        }
        if (changed && player instanceof ServerPlayer) player.onUpdateAbilities();
    }

    // Player.tick resets noPhysics after START; LivingTick runs before movement.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player && isActive(player)) {
            if (!player.level().isClientSide) snapshot(player);
            enforce(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (isActive(player)) {
            if (!player.level().isClientSide) snapshot(player);
            enforce(player);
        } else if (!player.level().isClientSide && ownsAbilities(player)) end(player);
    }

    public static void end(Player player) {
        if (player.level().isClientSide) return;
        player.removeEffect(FlightHelmetMod.GHOST.get());
        if (!player.getPersistentData().contains(STATE)) return;
        CompoundTag state = player.getPersistentData().getCompound(STATE);
        player.noPhysics = player.isSpectator();
        player.getAbilities().mayfly = player.isCreative() || player.isSpectator() || state.getBoolean("MayFly");
        player.getAbilities().flying = player.isSpectator()
                || (player.getAbilities().mayfly && state.getBoolean("Flying"));
        player.getAbilities().invulnerable = player.isCreative() || player.isSpectator() || state.getBoolean("Invulnerable");
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        player.getPersistentData().remove(STATE);
        player.onUpdateAbilities();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isActive(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && isActive(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof Player player && isActive(player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isActive(event.getEntity())) {
            endFromAction(event.getEntity());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent event) {
        if (!isActive(event.getEntity())) return;
        boolean refresh = (event instanceof PlayerInteractEvent.RightClickItem
                || event instanceof PlayerInteractEvent.RightClickBlock
                || event instanceof PlayerInteractEvent.RightClickEmpty)
                && event.getItemStack().is(FlightHelmetMod.TRAPPED_PIGLIN_REMAINS.get());
        if (refresh) return;
        endFromAction(event.getEntity());
        if (event.isCancelable()) event.setCanceled(true);
    }

    private static void endFromAction(Player player) {
        if (player.level().isClientSide) AbilityNetwork.CHANNEL.sendToServer(new AbilityNetwork.EndGhost());
        else end(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (isActive(event.getPlayer())) {
            end(event.getPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { end(event.getEntity()); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) { end(event.getEntity()); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) { end(event.getEntity()); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(STATE)) {
            event.getEntity().getPersistentData().put(STATE, event.getOriginal().getPersistentData().getCompound(STATE).copy());
            end(event.getEntity());
        }
    }
}
