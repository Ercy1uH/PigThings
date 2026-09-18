package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.FlightHelmetSettings;
import com.liaoliao.flighthelmet.NicePickaxeItem;
import com.liaoliao.flighthelmet.RingHelper;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.PacketSearchContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovementInput;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Side.CLIENT)
public final class ClientEvents {
    private static ItemStack lastTooltipItem = ItemStack.EMPTY;
    private static boolean settingsKeyDown;
    private static boolean searchKeyDown;

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (player == null) {
            return;
        }
        ContainerSearchClient.tick();
        if (!Mouse.isButtonDown(1)) {
            NicePickaxeItem.resetClientRightClick();
        }

        boolean settingsDown = PigThingsKeys.OPEN_SETTINGS.isKeyDown();
        if (settingsDown && !settingsKeyDown) {
            openSettingsScreen(minecraft, player);
        }
        settingsKeyDown = settingsDown;

        boolean searchDown = PigThingsKeys.SEARCH_CONTAINER.isKeyDown();
        if (searchDown && !searchKeyDown) {
            startContainerSearch(minecraft, player);
        }
        searchKeyDown = searchDown;

        applyNoInertia(player);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().isEmpty()) {
            lastTooltipItem = event.getItemStack().copy();
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        ContainerSearchClient.render();
    }

    private static void openSettingsScreen(Minecraft minecraft, EntityPlayerSP player) {
        if (minecraft.currentScreen != null) {
            return;
        }
        ItemStack pickaxe = getHeldPickaxe(player);
        if (!pickaxe.isEmpty()) {
            minecraft.displayGuiScreen(new NicePickaxeSettingsScreen(pickaxe));
            return;
        }
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.getItem() == FlightHelmetMod.FLIGHT_HELMET) {
            minecraft.displayGuiScreen(new FlightHelmetScreen(helmet));
            return;
        }
        ItemStack ring = RingHelper.getEquippedRing(player);
        if (!ring.isEmpty()) {
            minecraft.displayGuiScreen(new RingSettingsScreen(ring));
        }
    }

    private static void startContainerSearch(Minecraft minecraft, EntityPlayerSP player) {
        if (minecraft.currentScreen instanceof GuiChat) {
            return;
        }
        ItemStack target = ItemStack.EMPTY;
        boolean openedContainer = false;
        if (minecraft.currentScreen instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) minecraft.currentScreen;
            openedContainer = !(container instanceof GuiInventory);
            Slot slot = container.getSlotUnderMouse();
            if (slot != null && slot.getHasStack()) {
                target = slot.getStack().copy();
            }
        }
        if (target.isEmpty() && !lastTooltipItem.isEmpty()) {
            target = lastTooltipItem.copy();
        }
        if (target.isEmpty()) {
            player.sendStatusMessage(new TextComponentTranslation("message.pigthings.search_no_item"), true);
            return;
        }
        if (minecraft.currentScreen != null) {
            minecraft.displayGuiScreen(null);
        }
        player.sendStatusMessage(new TextComponentTranslation("message.pigthings.search_started"), true);
        ModNetwork.CHANNEL.sendToServer(new PacketSearchContainer(target, openedContainer));
    }

    private static ItemStack getHeldPickaxe(EntityPlayerSP player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.getItem() instanceof NicePickaxeItem) {
            return mainHand;
        }
        ItemStack offHand = player.getHeldItemOffhand();
        return offHand.getItem() instanceof NicePickaxeItem ? offHand : ItemStack.EMPTY;
    }

    private static void applyNoInertia(EntityPlayerSP player) {
        if (!player.capabilities.isFlying) {
            return;
        }
        // 与 Forge 版一致：戴着头盔时只认头盔的设置，戒指整套被忽略（不回落）。
        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack equipment = helmet.getItem() == FlightHelmetMod.FLIGHT_HELMET
                ? helmet
                : RingHelper.getEquippedRing(player);
        if (equipment.isEmpty() || !FlightHelmetSettings.hasNoInertia(equipment)) {
            return;
        }
        MovementInput input = player.movementInput;
        boolean hasHorizontalInput = Math.abs(input.moveStrafe) > 0.001F || Math.abs(input.moveForward) > 0.001F;
        boolean hasVerticalInput = input.jump || input.sneak;
        if (!hasHorizontalInput) {
            player.motionX = 0.0D;
            player.motionZ = 0.0D;
        }
        if (!hasVerticalInput) {
            player.motionY = 0.0D;
        }
    }
}
