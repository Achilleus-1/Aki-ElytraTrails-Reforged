package dbrighthd.elytratrails.handler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
//import dbrighthd.elytratrails.config.pack.TrailPackConfigManager;
//import dbrighthd.elytratrails.network.GetAllRequestC2SPayload;
import dbrighthd.elytratrails.rendering.TrailSystem;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.minecraft.commands.CommandSourceStack;
import dbrighthd.elytratrails.network.ClientNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import static dbrighthd.elytratrails.network.ClientPlayerConfigStore.CLIENT_PLAYER_CONFIGS;

//import static dbrighthd.elytratrails.network.ClientPlayerConfigStore.CLIENT_PLAYER_CONFIGS;

/**
 * Sets up some handy client commands
 */
public class CommandHandler {
    public static void init() {
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("elytratrails").then(Commands.literal("debug")
                        .executes(CommandHandler::debugCommand))
        ));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("elytratrails").then(Commands.literal("debugmoverrides")
                        .executes(CommandHandler::debugOverridesCommand))
        ));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("elytratrails").then(Commands.literal("debugmodels")
                        .executes(CommandHandler::debugModelsCommand))
        ));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("elytratrails")
                        .then(Commands.literal("clear")
                                .executes(CommandHandler::clearCommand))
        ));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                Commands.literal("elytratrails")
                        .then(Commands.literal("getconfigs")
                                .executes(CommandHandler::requestTrailConfigs))
        ));
    }

    private static int clearCommand(CommandContext<CommandSourceStack> context) {
        int trailcount = TrailSystem.getTrailManager().trailsNumber();
        int activetrailcount = TrailSystem.getTrailManager().activeTrailsNumber();
        TrailSystem.getTrailManager().removeAllTrails();

        net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(
                Component.literal("Cleared " + trailcount + " trails, " + activetrailcount + " of which were active.")
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int debugCommand(CommandContext<CommandSourceStack> context) {
        int trailcount = TrailSystem.getTrailManager().trailsNumber();
        int activetrailcount = TrailSystem.getTrailManager().activeTrailsNumber();
        net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Stored Configs: " + CLIENT_PLAYER_CONFIGS.size()));
        for (var pair : CLIENT_PLAYER_CONFIGS.entrySet()) {
            net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(Component.literal(pair.getValue().playerName()));
        }
        net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Current Trails: " + trailcount));
        net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Active Trails:  " + activetrailcount));
        return Command.SINGLE_SUCCESS;
    }

    private static int debugOverridesCommand(CommandContext<CommandSourceStack> context) {
        var chat = net.minecraft.client.Minecraft.getInstance().gui.getChat();
        chat.addMessage(Component.literal("Current Model Overrides:"));
        for (String model : dbrighthd.elytratrails.config.pack.TrailPackConfigManager.getModelStrings()) {
            chat.addMessage(Component.literal(model));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int debugModelsCommand(CommandContext<CommandSourceStack> context) {
        var chat = net.minecraft.client.Minecraft.getInstance().gui.getChat();
        chat.addMessage(Component.literal("Current Models With Trails Defined:"));
        for (var type : dbrighthd.elytratrails.config.pack.TrailPackConfigManager.entitiesWithTrails) {
            chat.addMessage(Component.literal(type.toShortString()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int requestTrailConfigs(CommandContext<CommandSourceStack> context) {
        ClientNetworking.send(new dbrighthd.elytratrails.network.GetAllRequestC2SPayload());
        return Command.SINGLE_SUCCESS;
    }
}
