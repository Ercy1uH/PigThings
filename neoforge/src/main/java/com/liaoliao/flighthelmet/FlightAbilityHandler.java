package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = FlightHelmetMod.MOD_ID)
public final class FlightAbilityHandler {
    private static final String ACTIVE_KEY = "FlightHelmetActive";
    private static final String GRANTED_FLIGHT_KEY = "FlightHelmetGrantedFlight";
    private static final String PREVIOUS_SPEED_KEY = "FlightHelmetPreviousFlyingSpeed";
    private static final String NIGHT_VISION_GRANTED_KEY = "FlightHelmetGrantedNightVision";

    private FlightAbilityHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack activeStack = getActiveStack(player);
        if (!activeStack.isEmpty()) {
            applyHelmetAbilities(player, activeStack);
        } else {
            restorePreviousAbilities(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack activeStack = getActiveStack(player);
        if (activeStack.isEmpty()) {
            restorePreviousAbilities(player);
            return;
        }
        applyHelmetAbilities(player, activeStack);
        player.onUpdateAbilities();
        if (FlightHelmetSettings.hasNightVision(activeStack)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false, false));
            player.getPersistentData().putBoolean(NIGHT_VISION_GRANTED_KEY, true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.getAbilities().flying || player.onGround()) {
            return;
        }
        if (!getActiveStack(player).isEmpty()) {
            event.setNewSpeed(event.getNewSpeed() * 5.0F);
        }
    }

    public static void applyHelmetAbilities(ServerPlayer player, ItemStack equipment) {
        RegistryAccess registries = player.level().registryAccess();
        FlightHelmetItem.ensureProtection(equipment, registries);
        Abilities abilities = player.getAbilities();
        CompoundTag data = player.getPersistentData();
        boolean changed = false;
        if (!data.getBoolean(ACTIVE_KEY)) {
            data.putBoolean(ACTIVE_KEY, true);
            data.putBoolean(GRANTED_FLIGHT_KEY, !abilities.mayfly);
            data.putFloat(PREVIOUS_SPEED_KEY, abilities.getFlyingSpeed());
        }
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            changed = true;
        }
        float desiredSpeed = FlightHelmetSettings.VANILLA_FLYING_SPEED
                * FlightHelmetSettings.getSpeedMultiplier(equipment);
        if (Float.compare(abilities.getFlyingSpeed(), desiredSpeed) != 0) {
            abilities.setFlyingSpeed(desiredSpeed);
            changed = true;
        }
        applyNightVision(player, equipment, data);
        applySaturation(player);
        collectNearbyItems(player, equipment);
        if (changed) {
            player.onUpdateAbilities();
        }
    }

    private static ItemStack getActiveStack(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.is(FlightHelmetMod.FLIGHT_HELMET.get())) {
            return helmet;
        }
        return RingHelper.getEquippedRing(player);
    }

    private static void applyNightVision(ServerPlayer player, ItemStack equipment, CompoundTag data) {
        if (!FlightHelmetSettings.hasNightVision(equipment)) {
            removeHelmetNightVision(player, data);
            return;
        }
        MobEffectInstance currentEffect = player.getEffect(MobEffects.NIGHT_VISION);
        if (!data.getBoolean(NIGHT_VISION_GRANTED_KEY) || currentEffect == null || currentEffect.getDuration() != -1) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false, false));
            data.putBoolean(NIGHT_VISION_GRANTED_KEY, true);
        }
    }

    private static void removeHelmetNightVision(ServerPlayer player, CompoundTag data) {
        if (data.getBoolean(NIGHT_VISION_GRANTED_KEY)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            data.remove(NIGHT_VISION_GRANTED_KEY);
        }
    }

    private static void applySaturation(ServerPlayer player) {
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
    }

    private static void collectNearbyItems(ServerPlayer player, ItemStack equipment) {
        if (!FlightHelmetSettings.hasMagnet(equipment)) {
            return;
        }
        int rangeX = FlightHelmetSettings.getMagnetRangeX(equipment);
        int rangeY = FlightHelmetSettings.getMagnetRangeY(equipment);
        int rangeZ = FlightHelmetSettings.getMagnetRangeZ(equipment);
        AABB magnetArea = new AABB(player.getX() - rangeX, player.getY() - rangeY, player.getZ() - rangeZ,
                player.getX() + rangeX, player.getY() + rangeY, player.getZ() + rangeZ);
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, magnetArea, Entity::isAlive);
        for (ItemEntity item : items) {
            item.setPos(player.getX(), player.getY(), player.getZ());
            if (!item.isNoGravity()) {
                item.playerTouch(player);
            }
        }
    }

    private static void restorePreviousAbilities(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(ACTIVE_KEY)) {
            return;
        }
        removeHelmetNightVision(player, data);
        Abilities abilities = player.getAbilities();
        abilities.setFlyingSpeed(data.getFloat(PREVIOUS_SPEED_KEY));
        if (data.getBoolean(GRANTED_FLIGHT_KEY) && !player.isCreative() && !player.isSpectator()) {
            abilities.mayfly = false;
            abilities.flying = false;
        }
        data.remove(ACTIVE_KEY);
        data.remove(GRANTED_FLIGHT_KEY);
        data.remove(PREVIOUS_SPEED_KEY);
        player.onUpdateAbilities();
    }
}
