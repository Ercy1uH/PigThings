package com.liaoliao.flighthelmet;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class RingHelper {
    public static final ResourceLocation RING_ID = ResourceLocation.fromNamespaceAndPath(FlightHelmetMod.MOD_ID, "nice_ring");

    private RingHelper() {
    }

    public static ItemStack getEquippedRing(LivingEntity entity) {
        Item ring = BuiltInRegistries.ITEM.get(RING_ID);
        if (ring == null) {
            return ItemStack.EMPTY;
        }
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(ring))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    public static boolean hasHelmet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(FlightHelmetMod.FLIGHT_HELMET.get());
    }
}
