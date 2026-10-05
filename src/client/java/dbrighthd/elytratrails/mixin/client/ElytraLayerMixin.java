package dbrighthd.elytratrails.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dbrighthd.elytratrails.accessor.ElytraLayerAccessor;
import dbrighthd.elytratrails.rendering.TrailSystem;
import dbrighthd.elytratrails.util.FrameCounterUtil;
import dbrighthd.elytratrails.util.ShaderChecksUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dbrighthd.elytratrails.config.ConfigManager.getConfig;
import static dbrighthd.elytratrails.util.ModelTransformationUtil.*;

@Mixin(net.minecraft.client.renderer.entity.layers.ElytraLayer.class)
public class ElytraLayerMixin {

    @Unique
    private static final Int2IntOpenHashMap lastCapturedFrameByEntityId = new Int2IntOpenHashMap();
    @Unique private static int elytratrails$cacheFrame = Integer.MIN_VALUE;

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getArmorFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;Z)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private void elytratrails$captureElytraWingTips(PoseStack poseStack, MultiBufferSource buffer, int packedLight, net.minecraft.world.entity.LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        if (ShaderChecksUtil.isShadowPass()) return;

        ElytraLayerAccessor accessor = (ElytraLayerAccessor) this;
        ElytraModel<?> elytraModel = accessor.elytratrails$getModel();
        EquipmentElytraModelAccessor elytraAccessor = (EquipmentElytraModelAccessor) elytraModel;
        ModelPart leftWingPart = elytraAccessor.elytratrails$getLeftWing();
        ModelPart rightWingPart = elytraAccessor.elytratrails$getRightWing();
        int entityId = entity.getId();
        int currentFrameId = FrameCounterUtil.frameId;
        if (elytratrails$cacheFrame != currentFrameId) {
            lastCapturedFrameByEntityId.clear();
            elytratrails$cacheFrame = currentFrameId;
        }
        int lastCapturedFrameId = lastCapturedFrameByEntityId.getOrDefault(entityId, Integer.MIN_VALUE);
        if (lastCapturedFrameId == currentFrameId) return;
        lastCapturedFrameByEntityId.put(entityId, currentFrameId);

        if (TrailSystem.getWingtipSampler().captureWingSpawners(entityId, poseStack, leftWingPart, rightWingPart)) return;
        float wingOpenness = computeWingOpenness(leftWingPart);

        Vec3 leftWingTipLocal = computeWingTipLocal(leftWingPart, true);
        Vec3 rightWingTipLocal = computeWingTipLocal(rightWingPart, false);

        var config = dbrighthd.elytratrails.network.ClientPlayerConfigStore.getOrDefault(entityId);
        float localTipXScale = (float) config.wingtipDepthPosition();
        if (config.trailMovesWithElytraAngle())
        {
            localTipXScale = Mth.lerp(wingOpenness, 0.33f, 1.0f);
        }

        double localTipYScale = config.wingtipHorizontalPosition();
        double localTipZScale = (1.0 + config.wingtipVerticalPosition() * 2.0) / 3.0;
        if (config.trailMovesWithAngleOfAttack()) {
            localTipZScale = Mth.lerp(getSignedElytraAoARadiansFast(entity), 0.33333f, 1.0f);
        }
        leftWingTipLocal = new Vec3(leftWingTipLocal.x * localTipXScale, leftWingTipLocal.y * localTipYScale, leftWingTipLocal.z * localTipZScale);
        rightWingTipLocal = new Vec3(rightWingTipLocal.x * localTipXScale, rightWingTipLocal.y * localTipYScale, rightWingTipLocal.z * localTipZScale);
        Vec3 leftWingTipView = transformLocalPointThroughPart(poseStack, leftWingPart, leftWingTipLocal);
        Vec3 rightWingTipView = transformLocalPointThroughPart(poseStack, rightWingPart, rightWingTipLocal);
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();

        Vec3 leftWingTipWorld = leftWingTipView.add(cameraPos); // I dont need the same pose-stack correction in 1.21.1
        Vec3 rightWingTipWorld = rightWingTipView.add(cameraPos);

        TrailSystem.getWingtipSampler().insertWingTips(entityId, leftWingTipWorld, rightWingTipWorld);
    }
}
