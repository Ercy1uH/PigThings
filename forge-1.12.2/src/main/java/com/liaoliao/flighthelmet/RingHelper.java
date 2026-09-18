package com.liaoliao.flighthelmet;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public final class RingHelper {
    private RingHelper() {
    }

    public static ItemStack getEquippedRing(EntityPlayer player) {
        if (FlightHelmetMod.NICE_RING == null) {
            return ItemStack.EMPTY;
        }
        int slot = BaublesApi.isBaubleEquipped(player, FlightHelmetMod.NICE_RING);
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }

    public static boolean hasHelmet(EntityPlayer player) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == FlightHelmetMod.FLIGHT_HELMET;
    }
}
