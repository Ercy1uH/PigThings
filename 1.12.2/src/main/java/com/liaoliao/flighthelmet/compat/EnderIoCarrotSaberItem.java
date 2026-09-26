package com.liaoliao.flighthelmet.compat;

import com.liaoliao.flighthelmet.CarrotSaberItem;
import crazypants.enderio.api.teleport.IItemOfTravel;
import info.loenwind.autoconfig.factory.IValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EnderIoCarrotSaberItem extends CarrotSaberItem implements IItemOfTravel {
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            CarrotSaberTravelClient.travel(player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, EnumHand hand) {
        // Handle nearby blocks before their GUI interaction consumes the right click.
        return onItemRightClick(world, player, hand).getType();
    }

    @Override
    public boolean isActive(EntityPlayer player, ItemStack stack) {
        return true;
    }

    @Override
    public int getEnergyStored(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void extractInternal(ItemStack stack, int amount) {
        // The saber's travel ability does not consume energy.
    }

    @Override
    public void extractInternal(ItemStack stack, IValue<Integer> amount) {
        // Ender IO also calls this overload when draining a travel item.
    }
}
