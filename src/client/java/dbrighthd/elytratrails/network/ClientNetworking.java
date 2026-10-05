package dbrighthd.elytratrails.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientNetworking {
    public static void send(CustomPacketPayload payload) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null && connection.hasChannel(payload.type().id())) {
            PacketDistributor.sendToServer(payload);
        }
    }
}
