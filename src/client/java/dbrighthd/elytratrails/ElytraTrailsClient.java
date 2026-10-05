package dbrighthd.elytratrails;

import dbrighthd.elytratrails.config.*;
import dbrighthd.elytratrails.config.pack.TrailPackConfigManager;
import dbrighthd.elytratrails.controller.ContinuousTwirlController;
import dbrighthd.elytratrails.controller.TwirlController;
import dbrighthd.elytratrails.handler.CommandHandler;
import dbrighthd.elytratrails.handler.ParticleHandler;
import dbrighthd.elytratrails.network.RegisterPacketsClient;
import dbrighthd.elytratrails.platform.ClientEvents;
import dbrighthd.elytratrails.rendering.TrailSystem;
import dbrighthd.elytratrails.rendering.TrailTextureRegistry;
import dbrighthd.elytratrails.util.ElytraTimeUtil;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import static dbrighthd.elytratrails.network.ClientPlayerConfigStore.refreshLocalConfigs;

public class ElytraTrailsClient {
    private static ModConfig modConfig;
    public static void register(IEventBus bus, ModContainer container) {
        ConfigManager.load();
        refreshConfig();
        ElytraTimeUtil.init();
        ElytraTrailsKeybind.init();
        TrailSystem.init();
        ParticleHandler.init();
        refreshLocalConfigs();
        RegisterPacketsClient.initClient();
        CommandHandler.init();
        ContinuousTwirlController.setDurations();
        TwirlController.setDurations();
        TrailPackConfigManager.setDefaultSampleSettings();
        ClientEvents.register(bus);
        bus.addListener((AddPackFindersEvent event) -> {
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath("elytratrails", "resourcepacks/arrowtrails"),
                PackType.CLIENT_RESOURCES, Component.literal("Arrow Trails"), PackSource.BUILT_IN, false, Pack.Position.TOP);
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath("elytratrails", "resourcepacks/allaytrails"),
                PackType.CLIENT_RESOURCES, Component.literal("Allay Trails"), PackSource.BUILT_IN, false, Pack.Position.TOP);
        });
        bus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener(
            (ResourceManagerReloadListener) manager -> {
                TrailTextureRegistry.reloadNow(manager);
                TrailPackConfigManager.reloadPresets(manager);
                TrailPackConfigManager.reload(manager);
                TrailSystem.getTrailManager().removeAllTrails();
                TrailSystem.getWingtipSampler().clearFrameCache();
                TrailSystem.getWingtipSampler().clearFrameSnapCache();
            }));
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) ->
            dbrighthd.elytratrails.compat.ModStatuses.CLOTH_LOADED
                ? ConfigScreenBuilder.buildConfigScreen(parent, getConfig())
                : new FallbackConfigMessageScreen(parent));
    }
    public static ModConfig getConfig() { return modConfig; }
    public static void setConfig(ModConfig config) { ConfigManager.save(config); }
    public static void refreshConfig() { modConfig = ConfigManager.getConfig(); }
}
