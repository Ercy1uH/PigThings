package com.liaoliao.flighthelmet;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public final class NiceRingItem extends Item implements ICurioItem {
    public NiceRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        return equipIntoFirstOpenCurioSlot(player, stack)
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.pass(stack);
    }

    private static boolean equipIntoFirstOpenCurioSlot(Player player, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler == null) {
            return false;
        }
        for (ICurioStacksHandler slotHandler : handler.getCurios().values()) {
            IDynamicStackHandler stacks = slotHandler.getStacks();
            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                if (!stacks.getStackInSlot(slot).isEmpty()) {
                    continue;
                }
                ItemStack equipped = stack.copy();
                equipped.setCount(1);
                stacks.setStackInSlot(slot, equipped);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }
}
