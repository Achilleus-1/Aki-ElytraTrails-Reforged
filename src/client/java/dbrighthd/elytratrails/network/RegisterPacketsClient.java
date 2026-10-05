package dbrighthd.elytratrails.network;
import dbrighthd.elytratrails.controller.EntityTwirlManager;
import dbrighthd.elytratrails.rendering.TrailSystem;
import dbrighthd.elytratrails.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import static dbrighthd.elytratrails.ElytraTrailsClient.getConfig;
import static dbrighthd.elytratrails.network.ClientPlayerConfigStore.*;

public class RegisterPacketsClient {
    public static boolean hasRecievedThisSession = false;
    private static int pendingConfigRequestTicks = -1;
    public static void receive(TwirlStateS2CPayload payload) {
        EntityTwirlManager.setEntityTwirlState(payload.entityId(), payload.twirlState());
    }
    public static void receive(PlayerConfigS2CPayload payload) {
        TrailSystem.getTrailManager().removeTrail(payload.entityId());
        ClientPlayerConfigStore.putSafeInitial(payload.entityId(), payload.configTag());
    }
    public static void receive(RemoveFromStoreS2CPayload payload) { CLIENT_PLAYER_CONFIGS.remove(payload.entityId()); }
    public static void receive(LegacyPlayerConfigS2CPayload payload) {
        if (!hasRecievedThisSession && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("The server uses an outdated Elytra Contrails protocol. Trail synchronization requires version 1.4.0+."), false);
        }
        hasRecievedThisSession = true;
    }
    public static void initClient() {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            pendingConfigRequestTicks = 10;
        });
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.Clone event) -> {
            pendingConfigRequestTicks = 10;
        });
        ClientEvents.onTick(client -> {
            if (pendingConfigRequestTicks > 0 && client.level != null && client.player != null && --pendingConfigRequestTicks == 0) {
                if (getConfig().shareTrail || !getConfig().showTrailToOtherPlayers) {
                    ClientNetworking.send(new PlayerConfigC2SPayload(getLocalPlayerConfigToSend().toTag()));
                }
                if (getConfig().syncWithServer) ClientNetworking.send(new GetAllRequestC2SPayload());
            }
        });
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            pendingConfigRequestTicks = -1;
            hasRecievedThisSession = false;
            CLIENT_PLAYER_CONFIGS.clear();
            TrailSystem.getTrailManager().removeAllTrails();
            TrailSystem.getWingtipSampler().clearFrameCache();
            TrailSystem.getWingtipSampler().clearFrameSnapCache();
            EntityTwirlManager.clearAll();
        });
    }
}
