package dbrighthd.elytratrails.rendering;

import dbrighthd.elytratrails.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import dbrighthd.elytratrails.config.pack.ResolvedSampleSettings;
import dbrighthd.elytratrails.config.pack.TrailPackConfigManager;
import dbrighthd.elytratrails.mixin.client.ModelPartChildrenAccessor;
import dbrighthd.elytratrails.util.ModelTransformationUtil;
import dbrighthd.elytratrails.util.ShaderChecksUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.ExperienceOrb;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import static dbrighthd.elytratrails.ElytraTrailsClient.getConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//in 1.20.1/1.21.1 the sampling happens in a kind of backwards way where the sampler is called to return the sampled wingtip from the render mixin
public class WingTipSampler {
    public Map<Integer, List<Emitter>> gatheredTrailsThisFrame = new HashMap<>();
    public Map<Integer, List<Emitter>> gatherdTrailsThisFrameSnapCache = new HashMap<>();
    private final Map<Integer, List<Emitter>> entityEmitters = new HashMap<>();
    public record EntityEmitters(List<Emitter> emitters, boolean changedModelVariant) {}

    public void clearFrameCache() {
        gatheredTrailsThisFrame.clear();
        entityEmitters.clear();
    }
    public void clearFrameSnapCache() {
        gatherdTrailsThisFrameSnapCache.clear();
    }

    public void insertWingTips(int eid, Vec3 positionLeft, Vec3 positionRight)
    {
        gatheredTrailsThisFrame.put(eid, List.of(new Emitter(positionLeft, true, "elytra", "/leftWingTip", true),new Emitter(positionRight, false, "elytra", "/rightWingTip", true)));
    }

    public List<Emitter> getPlayerTrailEmitterPositions(Player player, float partialTick, ModConfig modConfig)
    {
        var output = new ArrayList<>(gatheredTrailsThisFrame.getOrDefault(player.getId(), List.of()));
        if (modConfig.emfSupport) output.addAll(entityEmitters.getOrDefault(player.getId(), List.of()));
        gatherdTrailsThisFrameSnapCache.put(player.getId(), output);
        return output;
    }

    public EntityEmitters getEntityTrailEmitterPositions(Entity entity, float partialTick, ResolvedSampleSettings settings) {
        List<Emitter> output = entityEmitters.getOrDefault(entity.getId(), List.of());
        if (settings.useWithoutEmf() && (entity instanceof ThrowableItemProjectile || entity instanceof FireworkRocketEntity || entity instanceof ExperienceOrb)) {
            output = List.of(new Emitter(entity.getPosition(partialTick).add(settings.xOffset(), settings.yOffset(), settings.zOffset()),
                false, entity.getType().toShortString(), "/trailSpawner", true));
        }
        gatherdTrailsThisFrameSnapCache.put(entity.getId(), output);
        return new EntityEmitters(output, false);
    }

    public boolean captureWingSpawners(int eid, PoseStack pose, ModelPart left, ModelPart right) {
        if (!getConfig().emfSupport) return false;
        var output = new ArrayList<Emitter>();
        scan(pose, left, "", "elytra", true, true, output, identitySet());
        scan(pose, right, "", "elytra", false, true, output, identitySet());
        if (output.isEmpty()) return false;
        gatheredTrailsThisFrame.put(eid, output);
        return true;
    }

    public void captureEntity(Entity entity, PoseStack pose, EntityModel<?> model, boolean arrow) {
        if (Minecraft.getInstance().level == null || ShaderChecksUtil.isShadowPass()) return;
        var config = getConfig();
        if (!config.enableAllTrails || !config.extendedEmfSupport) return;
        String modelName = entity.getType().toShortString();
        var output = new ArrayList<Emitter>();
        if (model != null && config.emfSupport && dbrighthd.elytratrails.compat.ModStatuses.EMF_LOADED) {
            var seen = identitySet();
            for (ModelPart root : roots(model)) scan(pose, root, "", modelName, true, false, output, seen);
        }
        var settings = TrailPackConfigManager.getDefaultEntitySettings(entity);
        if (output.isEmpty() && !(entity instanceof Player) && config.tryWithoutEmf && settings.useWithoutEmf()) {
            double scale = arrow ? 1.0 : 1.0 / 16.0;
            Vec3 point = ModelTransformationUtil.transformPoint(pose.last().pose(),
                new Vec3(-settings.xOffset() * scale, -settings.yOffset() * scale, -settings.zOffset() * scale));
            output.add(new Emitter(point.add(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()),
                true, modelName, "/trailSpawner", true));
        }
        if (!output.isEmpty()) {
            entityEmitters.put(entity.getId(), output);
            if (!settings.useWithoutEmf()) TrailPackConfigManager.entitiesWithTrails.add(entity.getType());
        }
    }

    private static Set<ModelPart> identitySet() { return Collections.newSetFromMap(new IdentityHashMap<>()); }
    private static List<ModelPart> roots(EntityModel<?> model) {
        if (model instanceof HierarchicalModel<?> hierarchical) return List.of(hierarchical.root());
        Set<ModelPart> parts = identitySet();
        for (Class<?> type = model.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !ModelPart.class.isAssignableFrom(field.getType())) continue;
                try { field.setAccessible(true); if (field.get(model) instanceof ModelPart part) parts.add(part); }
                catch (ReflectiveOperationException | RuntimeException ignored) {}
            }
        }
        Set<ModelPart> children = identitySet();
        for (ModelPart part : parts) children.addAll(((ModelPartChildrenAccessor)(Object)part).elytratrails$getChildren().values());
        parts.removeAll(children);
        return List.copyOf(parts);
    }
    private static void scan(PoseStack pose, ModelPart part, String path, String model, boolean left,
                             boolean wing, List<Emitter> output, Set<ModelPart> seen) {
        if (!seen.add(part)) return;
        pose.pushPose();
        try {
            part.translateAndRotate(pose);
            var children = ((ModelPartChildrenAccessor)(Object)part).elytratrails$getChildren();
            for (var entry : children.entrySet()) {
                String key = entry.getKey();
                String childPath = path.isEmpty() ? key : path + "/" + key;
                String lower = key.toLowerCase(Locale.ROOT);
                boolean fresh = wing && getConfig().hardCodedFreshAnimationsPlayerWingtips &&
                    (lower.contains("left_wing2") || lower.contains("right_wing2"));
                if (lower.contains("wingtip") || lower.contains("trailspawner") || fresh) {
                    pose.pushPose();
                    entry.getValue().translateAndRotate(pose);
                    Vec3 offset = fresh ? (left ? ModelTransformationUtil.FRESH_ANIMATIONS_LEFT_WINGTIP_OFFSET
                        : ModelTransformationUtil.FRESH_ANIMATIONS_RIGHT_WINGTIP_OFFSET) : Vec3.ZERO;
                    Vec3 point = ModelTransformationUtil.transformPoint(pose.last().pose(), offset)
                        .add(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
                    output.add(new Emitter(point, left, model, childPath, part.visible && entry.getValue().visible));
                    pose.popPose();
                }
                scan(pose, entry.getValue(), childPath, model, left, wing, output, seen);
            }
        } finally { pose.popPose(); }
    }
}
