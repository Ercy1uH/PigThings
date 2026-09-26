package com.liaoliao.flighthelmet;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class NicePickaxeItem extends PickaxeItem {
    public static final float FIXED_MINING_SPEED = 65035.0F;

    private static final String PRECISE_MODE_KEY = "NicePickaxePreciseMode";
    private static boolean clientRightClickHeld;

    public NicePickaxeItem(Item.Properties properties) {
        super(Tiers.NETHERITE, properties);
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
        if (state.isAir() || state.getDestroySpeed(level, context.getClickedPos()) < 0.0F) {
            return InteractionResult.FAIL;
        }
        if (!player.gameMode.destroyBlock(context.getClickedPos())) {
            return InteractionResult.FAIL;
        }
        SoundType sound = state.getSoundType(level, context.getClickedPos(), player);
        level.playSound(null, context.getClickedPos(), sound.getBreakSound(), SoundSource.BLOCKS,
                sound.getVolume(), sound.getPitch());
        return InteractionResult.CONSUME;
    }

    /**
     * Precise mode adds a real Silk Touch enchantment, which would make the item render through the
     * enchantment glint pass and stops the animated texture from playing. This tool never glints.
     */
    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return FIXED_MINING_SPEED;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(isPreciseMode(stack)
                ? "tooltip.pigthings.nice_pickaxe.precise.on"
                : "tooltip.pigthings.nice_pickaxe.precise.off"));
    }

    public static boolean isPreciseMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBoolean(PRECISE_MODE_KEY);
    }

    public static void setPreciseMode(ItemStack stack, boolean preciseMode) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(PRECISE_MODE_KEY, preciseMode);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Precise mode is implemented by adding or removing Silk Touch. Enchantments live in a datapack
     * registry in 1.21, so this needs the registry access of the current level.
     */
    public static void applySilkTouch(ItemStack stack, boolean preciseMode, RegistryAccess registries) {
        Holder<Enchantment> silkTouch = registries
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        EnchantmentHelper.updateEnchantments(stack, enchantments -> {
            if (preciseMode) {
                enchantments.set(silkTouch, 1);
            } else {
                enchantments.removeIf(holder -> holder.is(Enchantments.SILK_TOUCH));
            }
        });
    }

    public static void resetClientRightClick() {
        clientRightClickHeld = false;
    }

    public static boolean isClientRightClickHeld() {
        return clientRightClickHeld;
    }
}
