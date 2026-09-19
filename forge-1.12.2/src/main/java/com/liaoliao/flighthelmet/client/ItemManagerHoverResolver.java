package com.liaoliao.flighthelmet.client;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * 物品管理器悬停物品的反射适配。HEI（Had Enough Items）是 JEI 的分支，包名与 API 保持一致（mezz.jei），
 * 所以这里统一按 JEI 4.x 的公开接口取：全部反射调用，未安装时返回空，不做编译期依赖。
 * 方法一律在运行时对象自己的类上查（见 callOverlay），接口换包也不会失效。
 */
final class ItemManagerHoverResolver {
    private ItemManagerHoverResolver() {
    }

    static ItemStack getHoveredItem() {
        try {
            return toItemStack(callOverlay("getIngredientUnderMouse"));
        } catch (Throwable throwable) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * 物品管理器的搜索框是否正在输入。按 Y 是物理轮询（界面开着也要能用），
     * 所以要避免在它的搜索框里打字时误触发。
     */
    static boolean isSearchFieldFocused() {
        try {
            return Boolean.TRUE.equals(callOverlay("hasKeyboardFocus"));
        } catch (Throwable throwable) {
            return false;
        }
    }

    /**
     * 取物品列表覆盖层对象，并在它自己的类上找方法。接口名不能写死：JEI 4.15 起
     * IIngredientListOverlay 从 mezz.jei.api.gui 搬到了 mezz.jei.api（HEI 4.32 在 mezz.jei.api），
     * 写死旧包名会让 Class.forName 抛异常，悬停取物品与搜索框焦点守卫会一起静默失效。
     */
    private static Object callOverlay(String methodName) throws Exception {
        Object runtime = Class.forName("mezz.jei.Internal").getMethod("getRuntime").invoke(null);
        if (runtime == null) {
            return null;
        }
        Object overlay = runtime.getClass().getMethod("getIngredientListOverlay").invoke(runtime);
        return overlay == null ? null : overlay.getClass().getMethod(methodName).invoke(overlay);
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
