package com.liaoliao.flighthelmet.client;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * 物品管理器悬停物品的反射适配。HEI（Had Enough Items）是 JEI 的分支，包名与 API 保持一致（mezz.jei），
 * 所以这里统一按 JEI 4.x 的公开接口取：全部反射调用，未安装时返回空，不做编译期依赖。
 */
final class ItemManagerHoverResolver {
    private ItemManagerHoverResolver() {
    }

    static ItemStack getHoveredItem() {
        Object overlay = getIngredientListOverlay();
        if (overlay == null) {
            return ItemStack.EMPTY;
        }
        try {
            Object ingredient = Class.forName("mezz.jei.api.gui.IIngredientListOverlay")
                    .getMethod("getIngredientUnderMouse")
                    .invoke(overlay);
            return toItemStack(ingredient);
        } catch (Throwable throwable) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * 物品管理器的搜索框是否正在输入。按 Y 是物理轮询（界面开着也要能用），
     * 所以要避免在它的搜索框里打字时误触发。
     */
    static boolean isSearchFieldFocused() {
        Object overlay = getIngredientListOverlay();
        if (overlay == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(Class.forName("mezz.jei.api.gui.IIngredientListOverlay")
                    .getMethod("hasKeyboardFocus")
                    .invoke(overlay));
        } catch (Throwable throwable) {
            return false;
        }
    }

    private static Object getIngredientListOverlay() {
        try {
            Object runtime = Class.forName("mezz.jei.Internal").getMethod("getRuntime").invoke(null);
            if (runtime == null) {
                return null;
            }
            return Class.forName("mezz.jei.api.IJeiRuntime")
                    .getMethod("getIngredientListOverlay")
                    .invoke(runtime);
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static ItemStack toItemStack(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            return ((ItemStack) ingredient).copy();
        }
        if (ingredient instanceof List) {
            for (Object entry : (List<?>) ingredient) {
                ItemStack stack = toItemStack(entry);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        if (ingredient instanceof Object[]) {
            for (Object entry : (Object[]) ingredient) {
                ItemStack stack = toItemStack(entry);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
