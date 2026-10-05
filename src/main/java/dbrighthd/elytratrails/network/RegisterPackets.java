package dbrighthd.elytratrails.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class RegisterPackets {
    public static Set<UUID> playersReceivedWarnings = new HashSet<>();
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToClient(TwirlStateS2CPayload.ID, TwirlStateS2CPayload.CODEC,
            (payload, context) -> RegisterPacketsClient.receive(payload));
        registrar.playToClient(PlayerConfigS2CPayload.ID, PlayerConfigS2CPayload.CODEC,
            (payload, context) -> RegisterPacketsClient.receive(payload));
        registrar.playToClient(RemoveFromStoreS2CPayload.ID, RemoveFromStoreS2CPayload.CODEC,
            (payload, context) -> RegisterPacketsClient.receive(payload));
        registrar.playToClient(LegacyPlayerConfigS2CPayload.ID, LegacyPlayerConfigS2CPayload.CODEC,
            (payload, context) -> RegisterPacketsClient.receive(payload));
        registrar.playToServer(TwirlStateC2SPayload.ID, TwirlStateC2SPayload.CODEC, (payload, context) -> {
            Entity entity = context.player();
            TwirlStateS2CPayload serverPayload = new TwirlStateS2CPayload(entity.getId(), payload.twirlState());
            for (ServerPlayer player : context.player().getServer().getPlayerList().getPlayers()) {
                send(player, serverPayload);
            }
        });
        registrar.playToServer(PlayerConfigC2SPayload.ID, PlayerConfigC2SPayload.CODEC, (payload, context) -> {
            Entity entity = context.player();
            ServerPlayerConfigStore.SERVER_PLAYER_CONFIGS.put(entity.getId(),payload.configTag());
            PlayerConfigS2CPayload serverPayload = new PlayerConfigS2CPayload(entity.getId(), payload.configTag());
            for (ServerPlayer player : context.player().getServer().getPlayerList().getPlayers()) {
                send(player, serverPayload);
            }
        });
        registrar.playToServer(LegacyPlayerConfigC2SPayload.ID, LegacyPlayerConfigC2SPayload.CODEC, (payload, context) -> {
            if(!playersReceivedWarnings.contains(context.player().getUUID()))
            {
                context.player().displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§cYou are using an outdated version of Elytra Contrails. To sync with this server, you must update to Elytra Contrails 1.4.0+"),
                        false
                );
            }
            playersReceivedWarnings.add(context.player().getUUID());
        });
        registrar.playToServer(GetAllRequestC2SPayload.ID, GetAllRequestC2SPayload.CODEC, (payload, context) -> {
            for(Map.Entry<Integer, CompoundTag> configPair : ServerPlayerConfigStore.SERVER_PLAYER_CONFIGS.entrySet())
            {
                PlayerConfigS2CPayload serverPayload = new PlayerConfigS2CPayload(configPair.getKey(), configPair.getValue());
                send((ServerPlayer) context.player(), serverPayload);
            }
        });
        registrar.playToServer(RemoveFromStoreC2SPayload.ID, RemoveFromStoreC2SPayload.CODEC, (payload, context) -> {
            ServerPlayerConfigStore.SERVER_PLAYER_CONFIGS.remove(context.player().getId());
            for (ServerPlayer player : context.player().getServer().getPlayerList().getPlayers())
            {
                RemoveFromStoreS2CPayload serverPayload = new RemoveFromStoreS2CPayload(context.player().getId());
                send(player, serverPayload);
            }
        });
    }
    public static void send(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (player.connection.hasChannel(payload.type().id())) PacketDistributor.sendToPlayer(player, payload);
    }
}
