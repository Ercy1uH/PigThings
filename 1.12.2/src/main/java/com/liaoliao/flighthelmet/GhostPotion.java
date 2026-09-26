package com.liaoliao.flighthelmet;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class GhostPotion extends Potion {
    public GhostPotion() {
        super(false, 0x65e2d7);
        setRegistryName(FlightHelmetMod.MOD_ID, "ghost");
        setPotionName("effect.pigthings.ghost");
        setBeneficial();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        drawIcon(mc, x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        drawIcon(mc, x + 3, y + 3, alpha);
    }

    @SideOnly(Side.CLIENT)
    private static void drawIcon(Minecraft mc, int x, int y, float alpha) {
        mc.getTextureManager().bindTexture(new ResourceLocation(FlightHelmetMod.MOD_ID,
                "textures/item/trapped_piglin_remains.png"));
        GlStateManager.color(1, 1, 1, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 18, 18);
        GlStateManager.color(1, 1, 1, 1);
    }
}
