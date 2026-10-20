package net.logosnumen.nightmareevolved.client.colossus;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.client.anim.ClientPoses;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.colossus.ColossusAttack;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.entity.colossus.GravewyrmEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Draws every colossus: its variant's body, built and painted the first time it is seen and kept while it is near,
 * posed from how it moves and what attack it is in, with its glowing parts at full brightness (twice over in a rage).
 * A Gravewyrm's body is strung out in segments along the path its head has taken. The ground shakes for anyone near a
 * colossus's footfalls and blows.
 */
public class ColossusRenderer extends EntityRenderer<ColossusEntity> {
    private static final int CACHE_SIZE = 12;
    private static final Map<ColossusKind, ColossusModel> MODELS = new EnumMap<>(ColossusKind.class);
    private static final Map<String, Rig.Baked> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Rig.Baked> eldest) {
            if (this.size() > CACHE_SIZE) {
                Minecraft.getInstance().getTextureManager().release(eldest.getValue().skin());
                Minecraft.getInstance().getTextureManager().release(eldest.getValue().glow());
                return true;
            }

            return false;
        }
    };
    /** Per colossus: its attack progress and footfall count last frame, to catch the moments a blow or step lands. */
    private static final Map<Integer, float[]> LAST = new HashMap<>();

    static {
        MODELS.put(ColossusKind.CAIRN, new CairnModel());
        MODELS.put(ColossusKind.OSSUARY, new OssuaryModel());
        MODELS.put(ColossusKind.GRAVEWYRM, new GravewyrmModel());
        MODELS.put(ColossusKind.CHAINBOUND, new ChainboundModel());
    }

    public ColossusRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.5F;
        this.shadowStrength = 0.8F;
    }

    private static Rig.Baked baked(ColossusEntity entity) {
        ColossusVariant variant = entity.variant();
        ColossusModel model = MODELS.get(entity.kind());
        String key = entity.kind().id + "_" + Integer.toHexString(variant.seed());
        return CACHE.computeIfAbsent(key, k -> model.build(variant).bake(model.look(variant), "colossus/" + k));
    }

    @Override
    public ResourceLocation getTextureLocation(ColossusEntity entity) {
        return baked(entity).skin();
    }

    private static ColossusPose pose(ColossusEntity entity, float partial) {
        ColossusAttack attack = entity.clientAction();
        float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.wrapDegrees(Mth.rotLerp(partial, entity.yHeadRotO, entity.yHeadRot) - bodyYaw);
        float death = entity.deathTime > 0 ? Math.min(1.0F, (entity.deathTime + partial) / ColossusEntity.DEATH_TICKS) : 0.0F;
        return new ColossusPose(
            entity.tickCount + partial,
            entity.walkAnimation.position(partial),
            Math.min(1.0F, entity.walkAnimation.speed(partial)),
            headYaw,
            entity.getViewXRot(partial),
            attack != null ? attack.id() : null,
            entity.actionProgress(partial),
            death,
            entity.enraged()
        );
    }

    private static void draw(ModelPart part, Rig.Baked body, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, boolean enraged) {
        part.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(body.skin())), light, overlay);
        VertexConsumer glow = buffers.getBuffer(RenderType.eyes(body.glow()));
        part.render(poseStack, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        if (enraged) {
            part.render(poseStack, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public void render(ColossusEntity entity, float entityYaw, float partial, PoseStack poseStack, MultiBufferSource buffers, int light) {
        ColossusModel model = MODELS.get(entity.kind());
        ColossusVariant variant = entity.variant();
        Rig.Baked body = baked(entity);
        ColossusPose pose = pose(entity, partial);
        for (ModelPart part : body.root().getAllParts().toList()) {
            part.resetPose();
            part.xScale = 1.0F;
            part.yScale = 1.0F;
            part.zScale = 1.0F;
            part.visible = true;
        }

        model.animate(body, pose, variant);
        float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
        float scale = model.renderScale() * entity.getScale();
        int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(entity.hurtTime > 0));
        float sink = entity instanceof GravewyrmEntity wyrm ? sink(wyrm, pose) : 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.0F, -sink, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-scale, -scale, scale);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        draw(body.root(), body, poseStack, buffers, light, overlay, entity.enraged());
        poseStack.popPose();
        if (entity instanceof GravewyrmEntity wyrm) {
            this.coils(wyrm, body, pose, partial, scale, sink, poseStack, buffers, light, overlay);
        }

        this.tremors(entity, pose);
    }

    /** How far below the ground a Gravewyrm is drawn, in blocks: diving, under, rising again, or sinking in death. */
    private static float sink(GravewyrmEntity wyrm, ColossusPose pose) {
        float depth = 3.5F * wyrm.getScale();
        float under = pose.key("burrow", 0.0F, 0.0F, 0.08F, 0.0F, 0.22F, 1.0F, 0.74F, 1.0F, 0.86F, 0.0F, 1.0F, 0.0F);
        if (wyrm.burrowed() && !"burrow".equals(pose.action())) {
            under = 1.0F;
        }

        return depth * Math.max(under, pose.fall(0.2F, 1.0F) * 0.7F);
    }

    /** The Gravewyrm's body: one segment every so far back along the head's trail, tapering to the tail, swaying. */
    private void coils(GravewyrmEntity wyrm, Rig.Baked body, ColossusPose pose, float partial, float scale, float sink, PoseStack poseStack,
        MultiBufferSource buffers, int light, int overlay) {
        ModelPart segment = body.part("segment");
        ModelPart tail = body.part("tail");
        if (segment == null || tail == null || wyrm.trail.isEmpty()) {
            return;
        }

        Vec3 origin = wyrm.getPosition(partial);
        int count = wyrm.segments();
        double spacing = GravewyrmModel.SEGMENT / 16.0 * scale;
        List<Vec3> points = along(origin, new ArrayList<>(wyrm.trail), spacing, count + 1);
        segment.visible = true;
        tail.visible = true;
        for (int i = 1; i < points.size(); i++) {
            Vec3 here = points.get(i);
            Vec3 ahead = points.get(i - 1);
            Vec3 dir = ahead.subtract(here);
            double flat = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
            float yaw = flat < 1.0E-4 ? wyrm.getYRot() : (float)(Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0F;
            float pitch = (float)(Mth.atan2(dir.y, Math.max(flat, 1.0E-4)) * Mth.RAD_TO_DEG);
            Vec3 side = flat < 1.0E-4 ? Vec3.ZERO : new Vec3(dir.z / flat, 0.0, -dir.x / flat);
            float sway = GravewyrmModel.sway(pose, i) * wyrm.getScale();
            float taper = 1.0F - 0.45F * i / (count + 1.0F);
            poseStack.pushPose();
            poseStack.translate(here.x - origin.x + side.x * sway, here.y - origin.y - sink, here.z - origin.z + side.z * sway);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            float s = scale * taper;
            poseStack.scale(-s, -s, s);
            poseStack.translate(0.0F, -1.501F + 1.5F, 0.0F);
            draw(i == points.size() - 1 ? tail : segment, body, poseStack, buffers, light, overlay, wyrm.enraged());
            poseStack.popPose();
        }
    }

    /** Points every {@code spacing} blocks back along a path that starts at {@code start} and runs through {@code path}. */
    private static List<Vec3> along(Vec3 start, List<Vec3> path, double spacing, int count) {
        List<Vec3> out = new ArrayList<>(count + 1);
        out.add(start);
        Vec3 from = start;
        double travelled = 0.0;
        double next = spacing;
        int index = 0;
        while (out.size() <= count) {
            Vec3 to = index < path.size() ? path.get(index) : from.add(from.subtract(out.get(Math.max(0, out.size() - 2))).normalize().scale(spacing));
            double length = from.distanceTo(to);
            if (travelled + length >= next && length > 1.0E-6) {
                double t = (next - travelled) / length;
                Vec3 point = from.add(to.subtract(from).scale(t));
                out.add(point);
                travelled = next;
                from = point;
                next += spacing;
            } else {
                travelled += length;
                from = to;
                index++;
                if (index > path.size() + count * 4) {
                    break;
                }
            }
        }

        return out;
    }

    /** Shakes the view of a nearby player when a blow lands or, for the walkers, when a foot comes down. */
    private void tremors(ColossusEntity entity, ColossusPose pose) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (LAST.size() > 64) {
            LAST.clear();
        }

        float near = 1.0F - (float)Math.min(1.0, mc.player.distanceTo(entity) / 40.0);
        float[] last = LAST.computeIfAbsent(entity.getId(), id -> new float[]{-1.0F, 0.0F});
        ColossusAttack attack = entity.clientAction();
        float progress = pose.progress();
        if (attack != null && progress >= 0.0F && near > 0.0F) {
            for (int impact : attack.impacts()) {
                float at = impact / (float)attack.duration();
                if (last[0] < at && progress >= at) {
                    ClientPoses.quake(0.9F * near * entity.getScale());
                }
            }
        }

        last[0] = progress;
        if (entity.kind() != ColossusKind.GRAVEWYRM) {
            float steps = (float)Math.floor(pose.walk() * 0.45F / Mth.PI);
            if (steps != last[1] && pose.stride() > 0.25F && near > 0.0F) {
                ClientPoses.quake(0.22F * near);
            }

            last[1] = steps;
        }
    }
}
