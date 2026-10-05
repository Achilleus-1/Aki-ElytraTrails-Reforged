package dbrighthd.elytratrails.util;

import dbrighthd.elytratrails.platform.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.TimeUtil;

public class ElytraTimeUtil {

    private static long tickCounter = 0;

    public static void init() {
        ClientEvents.onTick(client -> {
            if (client.isPaused()) {
                return;
            }
            tickCounter++;
        });
    }

    public static long currentMillis() {
        return (long) (tickCounter * 1000 + (Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false) * 1000)) / 20;
    }

    public static long currentNanos() {
        return TimeUtil.NANOSECONDS_PER_MILLISECOND * currentMillis();
    }
}