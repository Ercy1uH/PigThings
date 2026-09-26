package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.network.SearchContainerPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static ItemStack lastTooltipItem = ItemStack.EMPTY;
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping("key.pigthings.open_settings",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.pigthings");
    public static final KeyMapping SEARCH_CONTAINER = new KeyMapping("key.pigthings.search_container_v2",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.pigthings");

    private ClientEvents() {
    }

    @EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Dist.CLIENT)
    public static final class GameBusEvents {
        private GameBusEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) {
                return;
            }
            ContainerSearchClient.tick();
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

            while (OPEN_SETTINGS.consumeClick()) {
                if (helmet.is(FlightHelmetMod.FLIGHT_HELMET.get()) && minecraft.screen == null) {
                    minecraft.setScreen(new FlightHelmetScreen(helmet));
                }
            }

            if (helmet.is(FlightHelmetMod.FLIGHT_HELMET.get())
                    && FlightHelmetSettings.hasNoInertia(helmet)
                    && player.getAbilities().flying) {
                Vec3 movement = player.getDeltaMovement();
                boolean hasHorizontalInput = Math.abs(player.input.leftImpulse) > 0.001F
                        || Math.abs(player.input.forwardImpulse) > 0.001F;
                boolean hasVerticalInput = player.input.jumping || player.input.shiftKeyDown;
                double x = hasHorizontalInput ? movement.x : 0.0;
                double y = hasVerticalInput ? movement.y : 0.0;
                double z = hasHorizontalInput ? movement.z : 0.0;
                if (x != movement.x || y != movement.y || z != movement.z) {
                    player.setDeltaMovement(x, y, z);
                }
            }
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            ContainerSearchClient.render(event);
        }

        @SubscribeEvent
        public static void onTooltip(RenderTooltipEvent.GatherComponents event) {
            if (!event.getItemStack().isEmpty()) {
                lastTooltipItem = event.getItemStack().copy();
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (event.getAction() != GLFW.GLFW_PRESS) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player != null
                    && !(minecraft.screen instanceof ChatScreen)
                    && SEARCH_CONTAINER.isActiveAndMatches(
                            InputConstants.getKey(event.getKey(), event.getScanCode()))) {
                startContainerSearch(minecraft, player);
            }
        }

        private static void startContainerSearch(Minecraft minecraft, LocalPlayer player) {
            ItemStack target = EmiHoverResolver.getHoveredItem();
            boolean hasContainerScreen = minecraft.screen instanceof AbstractContainerScreen;
            boolean openedContainer = hasContainerScreen && !(minecraft.screen instanceof InventoryScreen);
            if (target.isEmpty() && minecraft.screen instanceof AbstractContainerScreen<?> screen) {
                Slot slot = screen.getSlotUnderMouse();
                if (slot != null && slot.hasItem()) {
                    target = slot.getItem().copy();
                }
            } else if (target.isEmpty() && !lastTooltipItem.isEmpty()) {
                target = lastTooltipItem.copy();
            }

            if (target.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.pigthings.search_no_item"), true);
                return;
            }
            if (minecraft.screen != null) {
                minecraft.setScreen(null);
            }
            player.displayClientMessage(Component.translatable("message.pigthings.search_started"), true);
            PacketDistributor.sendToServer(new SearchContainerPacket(target, openedContainer));
        }
    }

    @EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_SETTINGS);
            event.register(SEARCH_CONTAINER);
        }
    }
}
