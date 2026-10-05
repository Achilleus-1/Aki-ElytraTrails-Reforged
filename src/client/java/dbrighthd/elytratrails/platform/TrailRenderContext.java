package dbrighthd.elytratrails.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public record TrailRenderContext(RenderLevelStageEvent event) {
    public PoseStack matrixStack() { return event.getPoseStack(); }
    public GameRenderer gameRenderer() { return Minecraft.getInstance().gameRenderer; }
    public MultiBufferSource consumers() { return Minecraft.getInstance().renderBuffers().bufferSource(); }
}
