package com.liaoliao.flighthelmet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID)
public final class GhostAbilityHandler {
    private static final String STATE = "PigThingsGhostState";

    private GhostAbilityHandler() { }

    public static boolean isActive(EntityPlayer player) {
        return player.isPotionActive(FlightHelmetMod.GHOST);
    }

    public static boolean ownsAbilities(EntityPlayer player) {
        return isActive(player) || player.getEntityData().hasKey(STATE);
    }

    public static void activate(EntityPlayer player) {
        snapshot(player);
        player.addPotionEffect(new PotionEffect(FlightHelmetMod.GHOST, 600, 0, false, true));
        player.motionX = player.motionY = player.motionZ = 0;
        enforce(player);
        player.sendPlayerAbilities();
    }

    private static void snapshot(EntityPlayer player) {
        if (!player.getEntityData().hasKey(STATE)) {
            NBTTagCompound state = new NBTTagCompound();
            state.setBoolean("AllowFlying", player.capabilities.allowFlying);
            state.setBoolean("Flying", player.capabilities.isFlying);
            state.setBoolean("Invulnerable", player.capabilities.disableDamage);
            player.getEntityData().setTag(STATE, state);
        }
    }

    private static void enforce(EntityPlayer player) {
        player.noClip = true;
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        if (!player.world.isRemote) player.capabilities.disableDamage = true;
        player.onGround = false;
        player.fallDistance = 0;
        player.extinguish();
        if (player.posY < 1) {
            player.motionY = 0;
            if (player instanceof EntityPlayerMP) player.setPositionAndUpdate(player.posX, 1, player.posZ);
            else player.setPosition(player.posX, 1, player.posZ);
        }
    }

    // Player.onUpdate resets noClip AFTER START tick. LivingUpdate fires before movement.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && isActive((EntityPlayer) event.getEntityLiving())) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (!player.world.isRemote) snapshot(player);
            enforce(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (isActive(player)) {
            if (!player.world.isRemote) snapshot(player);
            enforce(player);
        } else if (!player.world.isRemote && player.getEntityData().hasKey(STATE)) {
            end(player);
        }
    }

    public static void end(EntityPlayer player) {
        if (player.world.isRemote) return;
        player.removePotionEffect(FlightHelmetMod.GHOST);
        if (!player.getEntityData().hasKey(STATE)) return;
        NBTTagCompound state = player.getEntityData().getCompoundTag(STATE);
        player.noClip = player.isSpectator();
        player.capabilities.allowFlying = player.isCreative() || player.isSpectator() || state.getBoolean("AllowFlying");
        player.capabilities.isFlying = player.isSpectator() || (player.capabilities.allowFlying && state.getBoolean("Flying"));
        player.capabilities.disableDamage = player.isCreative() || player.isSpectator() || state.getBoolean("Invulnerable");
        player.motionX = player.motionY = player.motionZ = 0;
        player.fallDistance = 0;
        player.getEntityData().removeTag(STATE);
        player.sendPlayerAbilities();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && isActive((EntityPlayer) event.getEntityLiving())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && isActive((EntityPlayer) event.getEntityLiving())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && isActive((EntityPlayer) event.getEntityLiving())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isActive(event.getEntityPlayer())) {
            endFromAction(event.getEntityPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteract(PlayerInteractEvent event) {
        if (!isActive(event.getEntityPlayer())) return;
        boolean refresh = (event instanceof PlayerInteractEvent.RightClickItem
                || event instanceof PlayerInteractEvent.RightClickBlock
                || event instanceof PlayerInteractEvent.RightClickEmpty)
                && event.getItemStack().getItem() == FlightHelmetMod.TRAPPED_PIGLIN_REMAINS;
        if (refresh) return;
        endFromAction(event.getEntityPlayer());
        if (event.isCancelable()) event.setCanceled(true);
    }

    private static void endFromAction(EntityPlayer player) {
        if (player.world.isRemote) com.liaoliao.flighthelmet.client.GhostClientEvents.requestEnd();
        else end(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (isActive(event.getPlayer())) {
            end(event.getPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { end(event.player); }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) { end(event.player); }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) { end(event.player); }
}
