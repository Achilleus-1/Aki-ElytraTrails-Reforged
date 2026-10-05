package dbrighthd.elytratrails.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Small local adapter keeping the upstream callback order on NeoForge. */
public final class ClientEvents {
    private static final List<Consumer<Minecraft>> ticks = new ArrayList<>();
    private static final List<Consumer<TrailRenderContext>> entities = new ArrayList<>();
    private static final List<Consumer<TrailRenderContext>> translucent = new ArrayList<>();
    private static final List<KeyMapping> keys = new ArrayList<>();
    public static void onTick(Consumer<Minecraft> callback) { ticks.add(callback); }
    public static void afterEntities(Consumer<TrailRenderContext> callback) { entities.add(callback); }
    public static void afterTranslucent(Consumer<TrailRenderContext> callback) { translucent.add(callback); }
    public static KeyMapping key(KeyMapping key) { keys.add(key); return key; }

    public static void register(IEventBus bus) {
        bus.addListener((RegisterKeyMappingsEvent event) -> keys.forEach(event::register));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            Minecraft mc = Minecraft.getInstance();
            for (var callback : ticks) callback.accept(mc);
        });
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            List<Consumer<TrailRenderContext>> callbacks;
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) callbacks = entities;
            else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) callbacks = translucent;
            else return;
            var context = new TrailRenderContext(event);
            for (var callback : callbacks) callback.accept(context);
        });
    }
}
