package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class TrappedPiglinRemainsItem extends Item {
    public static final String LORE = "\u8fd9\u4e0b\u4f60\ud83d\udc16\u4e0d\u5230\u6211\u4e86\u3002";

    public TrappedPiglinRemainsItem() {
        setRegistryName(FlightHelmetMod.MOD_ID, "trapped_piglin_remains");
        setTranslationKey(FlightHelmetMod.MOD_ID + ".trapped_piglin_remains");
        setCreativeTab(CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
        }
        if (!world.isRemote) {
            GhostAbilityHandler.activate(player);
            player.getCooldownTracker().setCooldown(this, 200);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing face,
            float hitX, float hitY, float hitZ, EnumHand hand) {
        return onItemRightClick(world, player, hand).getType();
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(LORE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return com.liaoliao.flighthelmet.client.RemainsFont.get();
    }
}
