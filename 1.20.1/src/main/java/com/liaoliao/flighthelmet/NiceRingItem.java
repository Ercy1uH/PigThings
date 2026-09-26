package com.liaoliao.flighthelmet;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import net.minecraft.world.level.Level;

public final class NiceRingItem extends Item {
    public NiceRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        ICurio curio = new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public boolean canEquipFromUse(SlotContext slotContext) {
                return true;
            }
        };
        return CuriosApi.createCurioProvider(curio);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        return equipIntoFirstOpenCurioSlot(player, stack)
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.pass(stack);
    }

    private static boolean equipIntoFirstOpenCurioSlot(Player player, ItemStack stack) {
        LazyOptional<ICuriosItemHandler> inventory = CuriosApi.getCuriosInventory(player);
        if (!inventory.isPresent()) {
            return false;
        }
        ICuriosItemHandler handler = inventory.orElseThrow(IllegalStateException::new);
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
