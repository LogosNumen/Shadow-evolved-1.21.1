package net.logosnumen.nightmareevolved.client.form;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.logosnumen.nightmareevolved.client.creature.CreatureRenderer;
import net.logosnumen.nightmareevolved.client.memory.ClientWorn;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.slf4j.Logger;

/**
 * Draws a transformed Saint as their form: the creature they have become, posed from how they move, swing and fly, in
 * place of their body; or their own body changed by its tint and light. In a creature's body, the hand in front of the
 * eyes is gone too.
 */
public final class FormRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Rig.Baked> BAKED = new HashMap<>();
    /** Turns every form by this many degrees, to look at forms side-on in a test ({@code -Dnightmare_evolved.formPreviewTurn=70}). */
    private static final int PREVIEW_TURN = Integer.getInteger("nightmare_evolved.formPreviewTurn", 0);

    private FormRenderer() {
    }

    private static Rig.Baked baked(String form, FormBodies.Body body) {
        return BAKED.computeIfAbsent(form, key -> body.model().build(0).bake(body.model().look(0), "form/" + key));
    }

    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        String form = ClientWorn.form(player.getId());
        if (form.isEmpty()) {
            return;
        }

        FormBodies.Body body = FormBodies.body(form);
        FormBodies.Skin skin = body == null ? FormBodies.skin(form) : null;
        if (body == null && skin == null) {
            return;
        }

        event.setCanceled(true);
        if (player.isInvisible()) {
            return;
        }

        float partial = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();
        int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(player.hurtTime > 0 || player.deathTime > 0));
        float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.wrapDegrees(Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot) - bodyYaw);
        bodyYaw += PREVIEW_TURN;
        float death = player.deathTime > 0 ? Math.min(1.0F, (player.deathTime + partial) / 20.0F) : 0.0F;
        if (body != null) {
            float swing = player.getAttackAnim(partial);
            // A creature form reads "enraged" as aloft: wings beat and legs tuck up while the Saint flies.
            Pose pose = new Pose(player.tickCount + partial, player.walkAnimation.position(partial), Math.min(1.0F, player.walkAnimation.speed(partial)),
                headYaw, player.getViewXRot(partial), swing > 0.0F ? body.attack() : null, swing, death, player.getAbilities().flying);
            CreatureRenderer.draw(body.model(), baked(form, body), pose, 0, bodyYaw, player.getScale(), body.tint(), poseStack, buffers, light, overlay);
            return;
        }

        if (player instanceof AbstractClientPlayer client) {
            drawSkin(client, event.getRenderer().getModel(), skin, bodyYaw, headYaw, partial, poseStack, buffers, light, overlay);
        }
    }

    /** The Saint's own body, in its form's tint, lit or shining. Its armour and what it holds have become the form too. */
    private static void drawSkin(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, FormBodies.Skin skin, float bodyYaw, float headYaw,
        float partial, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        model.attackTime = player.getAttackAnim(partial);
        model.crouching = player.isCrouching();
        model.setupAnim(player, player.walkAnimation.position(partial), player.walkAnimation.speed(partial), player.tickCount + partial, headYaw,
            player.getViewXRot(partial));
        float scale = 0.9375F * player.getScale();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        if (player.deathTime > 0) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(Math.min(1.0F, (player.deathTime + partial) / 20.0F) * 90.0F));
        }

        poseStack.scale(-scale, -scale, scale);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        ResourceLocation texture = player.getSkin().texture();
        model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityTranslucent(texture)), light, overlay, skin.tint());
        if (skin.glows()) {
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.eyes(texture)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, skin.tint());
        }

        poseStack.popPose();
    }

    /** In a creature's body, the Saint has no human hand to see. */
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && FormBodies.body(ClientWorn.form(player.getId())) != null) {
            event.setCanceled(true);
        }
    }

    /** Builds every form's body once at start, and reports any that can't be built. */
    public static void verify() {
        FormBodies.bodies().forEach((form, body) -> {
            try {
                body.model().build(0);
            } catch (RuntimeException e) {
                LOGGER.error("The body of the {} form can't be built", form, e);
            }
        });
    }
}
