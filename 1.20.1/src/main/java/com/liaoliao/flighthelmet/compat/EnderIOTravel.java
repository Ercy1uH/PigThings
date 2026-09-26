package com.liaoliao.flighthelmet.compat;

import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.handler.TravelHandler;
import com.liaoliao.flighthelmet.FlightHelmetMod;
import net.minecraft.world.entity.player.Player;

/** Loaded only after Ender IO has been detected. */
public final class EnderIOTravel {
    private EnderIOTravel() { }

    public static boolean travel(Player player) {
        boolean moved = player.isShiftKeyDown() ? TravelHandler.shortTeleport(player.level(), player)
                : TravelHandler.blockTeleport(player.level(), player);
        if (moved) {
            player.getCooldowns().addCooldown(FlightHelmetMod.CARROT_SABER.get(),
                    BaseConfig.COMMON.ITEMS.TRAVELLING_BLINK_DISABLED_TIME.get());
        }
        return moved;
    }
}
