package dbrighthd.elytratrails.network;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPlayerConfigStore {
    public static final ConcurrentHashMap<Integer, CompoundTag> SERVER_PLAYER_CONFIGS = new ConcurrentHashMap<>();
    public static void registerDisconnectCleanup() {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            RegisterPackets.playersReceivedWarnings.remove(player.getUUID());
            SERVER_PLAYER_CONFIGS.remove(player.getId());
            for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
                RegisterPackets.send(other, new RemoveFromStoreS2CPayload(player.getId()));
            }
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            SERVER_PLAYER_CONFIGS.clear();
            RegisterPackets.playersReceivedWarnings.clear();
        });
    }
}
