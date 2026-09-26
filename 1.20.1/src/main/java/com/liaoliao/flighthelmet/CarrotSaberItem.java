package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;

public final class CarrotSaberItem extends Item {
    public CarrotSaberItem(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
            net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return net.minecraft.world.InteractionResultHolder.fail(stack);
        if (level.isClientSide || CarrotSaberTeleport.travel(player)) {
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return net.minecraft.world.InteractionResultHolder.pass(stack);
    }

    @Override
    public net.minecraft.world.InteractionResult onItemUseFirst(ItemStack stack,
            net.minecraft.world.item.context.UseOnContext context) {
        return context.getPlayer() == null ? net.minecraft.world.InteractionResult.PASS
                : use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty().withStyle(style -> style.withItalic(false))
                .append(Component.literal("\u751c").withStyle(style -> style.withColor(0xff0f7b)))
                .append(Component.literal("\u7684").withStyle(style -> style.withColor(0xfd6b60)))
                .append(Component.literal("\uff01").withStyle(style -> style.withColor(0xf89b29))));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        Entity target = entity instanceof PartEntity<?> part ? part.getParent() : entity;
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        if (!player.level().isClientSide && living.isAlive()
                && (!(living instanceof Player other) || player.canHarmPlayer(other))) {
            // Keep vanilla death, loot and experience processing, including player credit.
            DamageSource source = new DamageSource(player.damageSources().genericKill().typeHolder(), player);
            living.hurt(source, Float.MAX_VALUE);
        }
        return true;
    }
}
