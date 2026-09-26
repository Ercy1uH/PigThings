package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.TrappedPiglinRemainsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

/** The legacy font has no RGB formatting or supplementary Unicode glyph support. */
public final class RemainsFont extends FontRenderer {
    private static RemainsFont instance;
    private static final int[] COLORS = {0xfff6b7, 0xffe2ad, 0xffcea4, 0xffb99a,
            0xffa491, 0xff8f87, 0xfe787e, 0xfa5f75, 0xf6416c};
    private static final ResourceLocation PIG = new ResourceLocation("pigthings", "textures/gui/pig_glyph.png");

    private RemainsFont(Minecraft mc) {
        super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.getTextureManager(), false);
    }

    public static RemainsFont get() {
        if (instance == null) instance = new RemainsFont(Minecraft.getMinecraft());
        return instance;
    }

    private static boolean isLore(String text) {
        return TrappedPiglinRemainsItem.LORE.equals(TextFormatting.getTextWithoutFormattingCodes(text));
    }

    @Override
    public int getStringWidth(String text) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        return isLore(text) ? font.getStringWidth(TrappedPiglinRemainsItem.LORE.replace("\ud83d\udc16", "")) + 10
                : font.getStringWidth(text);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawString(text, (float) x, (float) y, color, false);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean shadow) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isLore(text)) return mc.fontRenderer.drawString(text, x, y, color, shadow);
        int[] glyphs = TrappedPiglinRemainsItem.LORE.codePoints().toArray();
        for (int i = 0; i < glyphs.length; i++) {
            if (glyphs[i] == 0x1f416) {
                mc.getTextureManager().bindTexture(PIG);
                GlStateManager.enableBlend();
                GlStateManager.color(1, 1, 1, 1);
                Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0, 0, 9, 9, 9, 9);
                x += 10;
            } else {
                String glyph = new String(Character.toChars(glyphs[i]));
                mc.fontRenderer.drawString(glyph, x, y, COLORS[i], shadow);
                x += mc.fontRenderer.getStringWidth(glyph);
            }
        }
        GlStateManager.color(1, 1, 1, 1);
        return (int) x;
    }
}
