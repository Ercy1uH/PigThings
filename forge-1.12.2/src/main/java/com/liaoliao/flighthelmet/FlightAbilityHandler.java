package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID)
public final class FlightAbilityHandler {
    private static final String ACTIVE_KEY = "FlightHelmetActive";
    private static final String GRANTED_FLIGHT_KEY = "FlightHelmetGrantedFlight";
    private static final String PREVIOUS_SPEED_KEY = "FlightHelmetPreviousFlyingSpeed";
    private static final String NIGHT_VISION_GRANTED_KEY = "FlightHelmetGrantedNightVision";

    private FlightAbilityHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        ItemStack activeStack = getActiveStack(player);
        if (!activeStack.isEmpty()) {
            applyHelmetAbilities(player, activeStack);
        } else {
            restorePreviousAbilities(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        ItemStack activeStack = getActiveStack(player);
        if (activeStack.isEmpty()) {
            restorePreviousAbilities(player);
            return;
        }
        applyHelmetAbilities(player, activeStack);
        player.sendPlayerAbilities();
        if (FlightHelmetSettings.hasNightVision(activeStack)) {
            player.removePotionEffect(MobEffects.NIGHT_VISION);
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
            player.getEntityData().setBoolean(NIGHT_VISION_GRANTED_KEY, true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.capabilities.isFlying || player.onGround) {
            return;
        }
        if (!getActiveStack(player).isEmpty()) {
            event.setNewSpeed(event.getNewSpeed() * 5.0F);
        }
    }

    public static void applyHelmetAbilities(EntityPlayer player, ItemStack equipment) {
        FlightHelmetItem.ensureProtection(equipment);
        PlayerCapabilities capabilities = player.capabilities;
        NBTTagCompound data = player.getEntityData();
        boolean changed = false;
        if (!data.getBoolean(ACTIVE_KEY)) {
            data.setBoolean(ACTIVE_KEY, true);
            data.setBoolean(GRANTED_FLIGHT_KEY, !capabilities.allowFlying);
            data.setFloat(PREVIOUS_SPEED_KEY, capabilities.getFlySpeed());
        }
        if (!capabilities.allowFlying) {
            capabilities.allowFlying = true;
            changed = true;
        }
        float desiredSpeed = FlightHelmetSettings.VANILLA_FLYING_SPEED
                * FlightHelmetSettings.getSpeedMultiplier(equipment);
        if (Float.compare(capabilities.getFlySpeed(), desiredSpeed) != 0) {
            capabilities.setFlySpeed(desiredSpeed);
            changed = true;
        }
        applyNightVision(player, equipment, data);
        applySaturation(player);
        collectNearbyItems(player, equipment);
        if (changed) {
            player.sendPlayerAbilities();
        }
    }

    private static ItemStack getActiveStack(EntityPlayer player) {
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.getItem() == FlightHelmetMod.FLIGHT_HELMET) {
            return helmet;
        }
        return RingHelper.getEquippedRing(player);
    }

    private static void applyNightVision(EntityPlayer player, ItemStack equipment, NBTTagCompound data) {
        if (!FlightHelmetSettings.hasNightVision(equipment)) {
            removeHelmetNightVision(player, data);
            return;
        }
        if (!data.getBoolean(NIGHT_VISION_GRANTED_KEY) || player.getActivePotionEffect(MobEffects.NIGHT_VISION) == null) {
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
            data.setBoolean(NIGHT_VISION_GRANTED_KEY, true);
        }
    }

    private static void removeHelmetNightVision(EntityPlayer player, NBTTagCompound data) {
        if (data.getBoolean(NIGHT_VISION_GRANTED_KEY)) {
            player.removePotionEffect(MobEffects.NIGHT_VISION);
            data.removeTag(NIGHT_VISION_GRANTED_KEY);
        }
    }

    private static void applySaturation(EntityPlayer player) {
        player.getFoodStats().setFoodLevel(20);
        player.getFoodStats().setFoodSaturationLevel(20.0F);
    }

    private static void collectNearbyItems(EntityPlayer player, ItemStack equipment) {
        if (!FlightHelmetSettings.hasMagnet(equipment)) {
            return;
        }
        int rangeX = FlightHelmetSettings.getMagnetRangeX(equipment);
        int rangeY = FlightHelmetSettings.getMagnetRangeY(equipment);
        int rangeZ = FlightHelmetSettings.getMagnetRangeZ(equipment);
        AxisAlignedBB magnetArea = new AxisAlignedBB(player.posX - rangeX, player.posY - rangeY, player.posZ - rangeZ,
                player.posX + rangeX, player.posY + rangeY, player.posZ + rangeZ);
        List<EntityItem> items = player.world.getEntitiesWithinAABB(EntityItem.class, magnetArea, Entity::isEntityAlive);
        for (EntityItem item : items) {
            item.setPosition(player.posX, player.posY, player.posZ);
            if (!item.hasNoGravity()) {
                item.onCollideWithPlayer(player);
            }
        }
    }

    private static void restorePreviousAbilities(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.getBoolean(ACTIVE_KEY)) {
            return;
        }
        removeHelmetNightVision(player, data);
        PlayerCapabilities capabilities = player.capabilities;
        capabilities.setFlySpeed(data.getFloat(PREVIOUS_SPEED_KEY));
        if (data.getBoolean(GRANTED_FLIGHT_KEY) && !player.isCreative() && !player.isSpectator()) {
            capabilities.allowFlying = false;
            capabilities.isFlying = false;
        }
        data.removeTag(ACTIVE_KEY);
        data.removeTag(GRANTED_FLIGHT_KEY);
        data.removeTag(PREVIOUS_SPEED_KEY);
        player.sendPlayerAbilities();
    }
}
