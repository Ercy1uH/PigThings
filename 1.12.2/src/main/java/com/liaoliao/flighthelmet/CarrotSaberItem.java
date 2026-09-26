package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CarrotSaberItem extends Item {
    public CarrotSaberItem() {
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        boolean moved = CarrotSaberTeleport.blink(player);
        if (moved) {
            player.getCooldownTracker().setCooldown(this, 10);
        }
        return new ActionResult<>(moved ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        return onItemRightClick(world, player, hand).getType();
    }

    @Override
    public int getMetadata(ItemStack stack) {
        // Old sword stacks can retain damage values; all use the same model.
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        // Legacy Minecraft only supports its sixteen predefined text colors.
        tooltip.add(TextFormatting.LIGHT_PURPLE + "\u751c" + TextFormatting.RED + "\u7684"
                + TextFormatting.GOLD + "\uff01" + TextFormatting.RESET);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        Entity target = entity;
        if (entity instanceof MultiPartEntityPart
                && ((MultiPartEntityPart) entity).parent instanceof EntityLivingBase) {
            target = (EntityLivingBase) ((MultiPartEntityPart) entity).parent;
        }
        if (!(target instanceof EntityLivingBase)) {
            return false;
        }
        EntityLivingBase living = (EntityLivingBase) target;
        if (!player.world.isRemote && living.isEntityAlive()
                && (!(living instanceof EntityPlayer) || player.canAttackPlayer((EntityPlayer) living))) {
            // Let the damage pipeline set player credit and produce normal loot and XP.
            DamageSource source = new EntityDamageSource("player", player)
                    .setDamageBypassesArmor().setDamageIsAbsolute().setDamageAllowedInCreativeMode();
            living.hurtResistantTime = 0;
            entity.attackEntityFrom(source, Float.MAX_VALUE);
        }
        return true;
    }
}
