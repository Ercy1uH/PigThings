package com.liaoliao.flighthelmet.client;

import java.util.Locale;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 带可翻译标签的滑块：GuiSlider 自己的标签是「前缀 + 数值 + 后缀」拼接的，无法直接套用带 %s 的翻译键。
 * GuiSlider 在拖动时会调用 updateSlider() 用原始数值重写 displayString，所以标签必须在 updateSlider() 之后再设置。
 */
@SideOnly(Side.CLIENT)
public final class LabelSlider extends GuiSlider {
    private final String labelKey;
    private final boolean decimal;

    public LabelSlider(int id, int x, int y, int width, int height, String labelKey, boolean decimal,
                       double minValue, double maxValue, double currentValue) {
        super(id, x, y, width, height, "", "", minValue, maxValue, currentValue, decimal, true);
        this.labelKey = labelKey;
        this.decimal = decimal;
        updateLabel();
    }

    @Override
    public void updateSlider() {
        super.updateSlider();
        updateLabel();
    }

    private void updateLabel() {
        this.displayString = I18n.format(this.labelKey, this.decimal
                ? String.format(Locale.ROOT, "%.1f", getValue())
                : Integer.toString(getValueInt()));
    }

    public float getValueFloat() {
        return (float) getValue();
    }
}
