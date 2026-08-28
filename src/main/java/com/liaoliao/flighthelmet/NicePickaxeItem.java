package com.liaoliao.flighthelmet;

import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class NicePickaxeItem extends PickaxeItem {
    public static final float FIXED_MINING_SPEED = 65035.0f;

    private static final String PRECISE_MODE_KEY = "NicePickaxePreciseMode";
    private static boolean clientRightClickHeld;

    public NicePickaxeItem(Item.Properties properties) {
        super(Tiers.NETHERITE, 8, 20.0f, properties);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        return stack;
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        super.verifyTagAfterLoad(tag);
        tag.putBoolean("Unbreakable", true);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            if (clientRightClickHeld) {
                return InteractionResult.FAIL;
            }
            clientRightClickHeld = true;
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (state.isAir() || state.getDestroySpeed(level, context.getClickedPos()) < 0.0f) {
            return InteractionResult.FAIL;
        }
        if (!player.gameMode.destroyBlock(context.getClickedPos())) {
            return InteractionResult.FAIL;
        }
        SoundType sound = state.getSoundType();
        level.playSound(null, context.getClickedPos(), sound.getBreakSound(), SoundSource.BLOCKS,
                sound.getVolume(), sound.getPitch());
        return InteractionResult.CONSUME;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return FIXED_MINING_SPEED;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(isPreciseMode(stack)
                ? "tooltip.pigthings.nice_pickaxe.precise.on"
                : "tooltip.pigthings.nice_pickaxe.precise.off"));
    }

    public static boolean isPreciseMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(PRECISE_MODE_KEY);
    }

    public static void setSettings(ItemStack stack, boolean preciseMode) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(PRECISE_MODE_KEY, preciseMode);

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (preciseMode) {
            enchantments.put(Enchantments.SILK_TOUCH, 1);
        } else {
            enchantments.remove(Enchantments.SILK_TOUCH);
        }
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    public static void resetClientRightClick() {
        clientRightClickHeld = false;
    }
}
