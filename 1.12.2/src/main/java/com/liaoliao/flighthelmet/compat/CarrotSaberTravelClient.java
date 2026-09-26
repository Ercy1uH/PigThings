package com.liaoliao.flighthelmet.compat;

import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.base.config.config.TeleportConfig;
import crazypants.enderio.base.teleport.TravelController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CarrotSaberTravelClient {
    private CarrotSaberTravelClient() {
    }

    public static void travel(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (TeleportConfig.enableBlink.get() && !player.getCooldownTracker().hasCooldown(stack.getItem())
                    && TravelController.doBlink(stack, hand, player)) {
                player.getCooldownTracker().setCooldown(stack.getItem(), Math.max(1, TeleportConfig.blinkDelay.get()));
                player.swingArm(hand);
            }
        } else if (TravelController.activateTravelAccessable(stack, hand, player.world, player, TravelSource.STAFF)) {
            player.swingArm(hand);
        }
    }
}
