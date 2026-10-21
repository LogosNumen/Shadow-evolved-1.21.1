package net.logosnumen.nightmareevolved.client.dream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.law.RealmLaws;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The air of the Dream Realm's regions: black water when the Dark Sea closes over one's head, the white mist of
 * nothingness in the Hollow Mountains, the Underworld's lightless dark, the Hollows' red, humid haze, ash over the
 * Burned Forest, the Stormsea's spray. And the Crushing, darkening the edges of sight as an island rises.
 */
public final class RealmFog {
    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");

    private RealmFog() {
    }

    /** Whether the camera is in the mist of nothingness. */
    private static boolean inMist(Camera camera) {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null && ClientRealm.region() == Region.HOLLOW_MOUNTAINS && camera.getPosition().y < RealmLaws.MIST
            && level.canSeeSky(BlockPos.containing(camera.getPosition()));
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!ClientRealm.active()) {
            return;
        }

        Camera camera = event.getCamera();
        float far = event.getFarPlaneDistance();
        float near = event.getNearPlaneDistance();
        if (ClientRealm.underDarkSea(camera, (float)event.getPartialTick())) {
            near = 0.0F;
            far = 5.0F;
        } else if (inMist(camera)) {
            near = 0.0F;
            far = 22.0F;
        } else {
            switch (ClientRealm.region()) {
                case UNDERWORLD -> {
                    near = 0.0F;
                    far = Math.min(far, 30.0F);
                }
                case GODGRAVE_HOLLOW -> far = Math.min(far, 90.0F);
                case BURNED_FOREST -> far = Math.min(far, 110.0F);
                case STORMSEA -> far = Math.min(far, 100.0F);
                case GODGRAVE -> far = Math.min(far, 170.0F);
                default -> {
                    return;
                }
            }

            near = Math.min(near, far * 0.3F);
        }

        event.setNearPlaneDistance(near);
        event.setFarPlaneDistance(far);
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    public static void onFogColour(ViewportEvent.ComputeFogColor event) {
        if (!ClientRealm.active()) {
            return;
        }

        Camera camera = event.getCamera();
        float[] colour;
        if (ClientRealm.underDarkSea(camera, (float)event.getPartialTick())) {
            colour = new float[] {0.008F, 0.008F, 0.014F};
        } else if (inMist(camera)) {
            colour = new float[] {0.88F, 0.89F, 0.92F};
        } else {
            colour = switch (ClientRealm.region()) {
                case UNDERWORLD -> new float[] {0.01F, 0.01F, 0.015F};
                case GODGRAVE_HOLLOW -> new float[] {0.22F, 0.07F, 0.05F};
                case BURNED_FOREST -> new float[] {0.33F, 0.31F, 0.29F};
                case STORMSEA -> new float[] {0.16F, 0.18F, 0.24F};
                default -> null;
            };
        }

        if (colour != null) {
            event.setRed(colour[0]);
            event.setGreen(colour[1]);
            event.setBlue(colour[2]);
        }
    }

    /** The Crushing, closing in from the edges of sight. */
    public static void renderPressure(GuiGraphics graphics, DeltaTracker delta) {
        float pressure = ClientRealm.active() ? ClientRealm.pressure() : 0.0F;
        if (pressure < 0.05F) {
            return;
        }

        float strength = Mth.clamp(pressure, 0.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO,
            GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        graphics.setColor(strength * 0.9F, strength, strength, 1.0F);
        graphics.blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
