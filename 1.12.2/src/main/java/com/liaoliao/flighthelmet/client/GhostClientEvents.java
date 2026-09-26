package com.liaoliao.flighthelmet.client;

import com.liaoliao.flighthelmet.FlightHelmetMod;
import com.liaoliao.flighthelmet.GhostAbilityHandler;
import com.liaoliao.flighthelmet.network.ModNetwork;
import com.liaoliao.flighthelmet.network.PacketEndGhost;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = FlightHelmetMod.MOD_ID, value = Side.CLIENT)
public final class GhostClientEvents {
    private static boolean endRequested;
    private static boolean ghostTerrain;

    // Called only for RenderGlobal.setupTerrain's visibility calculation.
    public static boolean useUnrestrictedTerrain(boolean spectator) {
        Minecraft mc = Minecraft.getMinecraft();
        return spectator || (mc.player != null && mc.getRenderViewEntity() == mc.player
                && GhostAbilityHandler.isActive(mc.player));
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean active = useUnrestrictedTerrain(false);
        if (active != ghostTerrain) {
            ghostTerrain = active;
            Minecraft.getMinecraft().renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    public static void requestEnd() {
        if (!endRequested) {
            endRequested = true;
            ModNetwork.CHANNEL.sendToServer(new PacketEndGhost());
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !GhostAbilityHandler.isActive(mc.player)) {
            endRequested = false;
            return;
        }
        boolean usingOtherItem = mc.gameSettings.keyBindUseItem.isKeyDown()
                && mc.player.getHeldItemMainhand().getItem() != FlightHelmetMod.TRAPPED_PIGLIN_REMAINS
                && mc.player.getHeldItemOffhand().getItem() != FlightHelmetMod.TRAPPED_PIGLIN_REMAINS;
        if (!endRequested && (mc.gameSettings.keyBindAttack.isKeyDown() || usingOtherItem
                || mc.gameSettings.keyBindDrop.isKeyDown() || mc.gameSettings.keyBindPickBlock.isKeyDown())) {
            requestEnd();
        }
    }

    @SubscribeEvent
    public static void onPush(PlayerSPPushOutOfBlocksEvent event) {
        if (GhostAbilityHandler.isActive(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onOverlay(RenderBlockOverlayEvent event) {
        if (GhostAbilityHandler.isActive(event.getPlayer())) event.setCanceled(true);
    }
}
