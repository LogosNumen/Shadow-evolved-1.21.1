package net.logosnumen.nightmareevolved.client.creature;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.logosnumen.nightmareevolved.entity.creature.MirrorBeast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws every creature of the Dream Realm: its kind's body in the look it wears, built and painted the first time it is
 * seen and kept while it is near, posed from how it moves and the move it is in, with its glowing parts at full
 * brightness.
 */
public class CreatureRenderer extends EntityRenderer<DreamCreature> {
    private static final int CACHE_SIZE = 48;
    private static final Map<String, Rig.Baked> CACHE = new LinkedHashMap<>(64, 0.75F, true) {
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

    /** The shapes a Mirror Beast takes when it becomes a player. */
    private final PlayerModel<DreamCreature> wide;
    private final PlayerModel<DreamCreature> slim;

    public CreatureRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowStrength = 0.7F;
        this.wide = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    private static Rig.Baked baked(DreamCreature creature) {
        Kind kind = creature.kind();
        CreatureModel model = CreatureModels.of(kind);
        int look = creature.look();
        return CACHE.computeIfAbsent(kind.id + "_" + look, key -> model.build(look).bake(model.look(look), "creature/" + key));
    }

    @Override
    public ResourceLocation getTextureLocation(DreamCreature creature) {
        return baked(creature).skin();
    }

    private static Pose pose(DreamCreature creature, float partial) {
        Move move = creature.clientAction();
        float bodyYaw = Mth.rotLerp(partial, creature.yBodyRotO, creature.yBodyRot);
        float headYaw = Mth.wrapDegrees(Mth.rotLerp(partial, creature.yHeadRotO, creature.yHeadRot) - bodyYaw);
        float death = creature.deathTime > 0 ? Math.min(1.0F, (creature.deathTime + partial) / 20.0F) : 0.0F;
        return new Pose(
            creature.tickCount + partial + creature.getId() * 7,
            creature.walkAnimation.position(partial),
            Math.min(1.0F, creature.walkAnimation.speed(partial)),
            headYaw,
            creature.getViewXRot(partial),
            move != null ? move.id() : null,
            creature.actionProgress(partial),
            death,
            creature.roused()
        );
    }

    @Override
    public void render(DreamCreature creature, float entityYaw, float partial, PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (creature instanceof MirrorBeast mirror && mirror.mirrored().isPresent() && this.renderMirrored(mirror, mirror.mirrored().get(), partial, poseStack,
            buffers, light)) {
            super.render(creature, entityYaw, partial, poseStack, buffers, light);
            return;
        }

        CreatureModel model = CreatureModels.of(creature.kind());
        Rig.Baked body = baked(creature);
        Pose pose = pose(creature, partial);
        for (ModelPart part : body.root().getAllParts().toList()) {
            part.resetPose();
            part.xScale = 1.0F;
            part.yScale = 1.0F;
            part.zScale = 1.0F;
            part.visible = true;
        }

        model.animate(body, pose, creature.look());
        this.shadowRadius = creature.getBbWidth() * 0.6F;
        float bodyYaw = Mth.rotLerp(partial, creature.yBodyRotO, creature.yBodyRot);
        float scale = model.renderScale() * creature.getScale();
        int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(creature.hurtTime > 0 || creature.deathTime > 0));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-scale, -scale, scale);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        body.root().render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(body.skin())), light, overlay);
        VertexConsumer glow = buffers.getBuffer(RenderType.eyes(body.glow()));
        body.root().render(poseStack, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(creature, entityYaw, partial, poseStack, buffers, light);
    }

    /** Draws a Mirror Beast as the player whose shape it has taken, cold and silvered; false if that player is unknown here. */
    private boolean renderMirrored(MirrorBeast mirror, UUID player, float partial, PoseStack poseStack, MultiBufferSource buffers, int light) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection != null ? connection.getPlayerInfo(player) : null;
        if (info == null) {
            return false;
        }

        PlayerSkin skin = info.getSkin();
        PlayerModel<DreamCreature> model = skin.model() == PlayerSkin.Model.SLIM ? this.slim : this.wide;
        float bodyYaw = Mth.rotLerp(partial, mirror.yBodyRotO, mirror.yBodyRot);
        float headYaw = Mth.wrapDegrees(Mth.rotLerp(partial, mirror.yHeadRotO, mirror.yHeadRot) - bodyYaw);
        model.attackTime = mirror.getAttackAnim(partial);
        model.setupAnim(mirror, mirror.walkAnimation.position(partial), mirror.walkAnimation.speed(partial), mirror.tickCount + partial, headYaw,
            mirror.getViewXRot(partial));
        int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(mirror.hurtTime > 0 || mirror.deathTime > 0));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityTranslucent(skin.texture())), light, overlay, 0xF0D8E4FF);
        poseStack.popPose();
        return true;
    }
}
