package dbrighthd.elytratrails;

import dbrighthd.elytratrails.network.RegisterPackets;
import dbrighthd.elytratrails.network.ServerPlayerConfigStore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ElytraTrails.MOD_ID)
public class ElytraTrails {
    public static final String MOD_ID = "elytratrails";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ElytraTrails(IEventBus bus, ModContainer container) {
        bus.addListener(RegisterPackets::register);
        ServerPlayerConfigStore.registerDisconnectCleanup();
        if (FMLEnvironment.dist == Dist.CLIENT) ElytraTrailsClient.register(bus, container);
    }
}
