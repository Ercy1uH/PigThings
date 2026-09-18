package com.liaoliao.flighthelmet;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public final class NiceRingItem extends Item implements IBauble {
    public NiceRingItem() {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setRegistryName(FlightHelmetMod.MOD_ID, "nice_ring");
        this.setTranslationKey(FlightHelmetMod.MOD_ID + ".nice_ring");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.RING;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return equipIntoFirstOpenBaubleSlot(player, stack)
                ? new ActionResult<>(EnumActionResult.SUCCESS, stack)
                : new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private static boolean equipIntoFirstOpenBaubleSlot(EntityPlayer player, ItemStack stack) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        if (handler == null) {
            return false;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty() || !handler.isItemValidForSlot(slot, stack, player)) {
                continue;
            }
            ItemStack equipped = stack.copy();
            equipped.setCount(1);
            handler.setStackInSlot(slot, equipped);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return true;
        }
        return false;
    }
}
