package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class TrappedPiglinRemainsItem extends Item {
    public TrappedPiglinRemainsItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator() || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            GhostAbilityHandler.activate(player);
            player.getCooldowns().addCooldown(this, 200);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return context.getPlayer() == null ? InteractionResult.PASS
                : use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String[] letters = {"\u8fd9", "\u4e0b", "\u4f60", "\ud83d\udc16", "\u4e0d", "\u5230", "\u6211", "\u4e86", "\u3002"};
        int[] colors = {0xfff6b7, 0xffe2ad, 0xffcea4, 0xffb99a, 0xffa491, 0xff8f87, 0xfe787e, 0xfa5f75, 0xf6416c};
        MutableComponent lore = Component.empty().withStyle(style -> style.withItalic(false));
        for (int i = 0; i < letters.length; i++) {
            int color = colors[i];
            var glyph = Component.literal(letters[i]).withStyle(style -> style.withColor(color));
            if (i == 3) glyph.withStyle(style -> style.withFont(new net.minecraft.resources.ResourceLocation("pigthings", "remains")));
            lore.append(glyph);
        }
        tooltip.add(lore);
    }
}
