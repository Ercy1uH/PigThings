package com.liaoliao.flighthelmet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class RingHelper {
    public static final ResourceLocation RING_ID = new ResourceLocation(FlightHelmetMod.MOD_ID, "nice_ring");

    private RingHelper() {
    }

    public static ItemStack getEquippedRing(LivingEntity entity) {
        Item ring = ForgeRegistries.ITEMS.getValue(RING_ID);
        if (ring == null) {
            return ItemStack.EMPTY;
        }
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, ring)
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    public static boolean hasHelmet(Player player) {
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)
                .is(FlightHelmetMod.FLIGHT_HELMET.get());
    }
}
