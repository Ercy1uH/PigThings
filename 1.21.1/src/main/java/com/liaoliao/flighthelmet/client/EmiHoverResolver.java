package com.liaoliao.flighthelmet.client;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Optional EMI integration. Everything is resolved reflectively, so EMI is not a compile or runtime
 * dependency and the method degrades to an empty stack when EMI is absent.
 */
final class EmiHoverResolver {
    private EmiHoverResolver() {
    }

    static ItemStack getHoveredItem() {
        try {
            Class<?> emiApi = Class.forName("dev.emi.emi.api.EmiApi");
            Object interaction = emiApi.getMethod("getHoveredStack", boolean.class).invoke(null, false);
            if (interaction == null || (Boolean) interaction.getClass().getMethod("isEmpty").invoke(interaction)) {
                return ItemStack.EMPTY;
            }
            Object ingredient = interaction.getClass().getMethod("getStack").invoke(interaction);
            if (ingredient == null) {
                return ItemStack.EMPTY;
            }
            if (ingredient.getClass().getMethod("getEmiStacks").invoke(ingredient) instanceof List<?> emiStacks
                    && !emiStacks.isEmpty()) {
                Object emiStack = emiStacks.get(0);
                if (emiStack.getClass().getMethod("getItemStack").invoke(emiStack) instanceof ItemStack stack
                        && !stack.isEmpty()) {
                    return stack.copy();
                }
            }
            return ItemStack.EMPTY;
        } catch (ReflectiveOperationException exception) {
            return ItemStack.EMPTY;
        }
    }
}
