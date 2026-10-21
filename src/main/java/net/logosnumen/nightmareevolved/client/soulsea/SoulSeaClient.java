package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.shaders.FogShape;
import net.logosnumen.nightmareevolved.client.soul.ClientSoul;
import net.logosnumen.nightmareevolved.soulsea.SoulSea;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * How a Soul Sea looks from inside, which depends on whose it is. Most are calm seas under a deep blue night, lit by
 * their own suns; Sunny's is dark and silent under a black one; Nephis's is lit gold; Mordret's lies under grey cloud
 * in a white mist.
 */
public final class SoulSeaClient {
    private SoulSeaClient() {
    }

    /** The look of the local player's sea, by their Aspect. */
    public enum Look {
        STILL(new float[] {0.05F, 0.07F, 0.15F}, 12.0F, 90.0F, 1.0F),
        SHADOW(new float[] {0.004F, 0.004F, 0.008F}, 6.0F, 60.0F, 0.12F),
        LIGHT(new float[] {0.62F, 0.47F, 0.2F}, 20.0F, 120.0F, 0.4F),
        NOTHING(new float[] {0.55F, 0.57F, 0.6F}, 1.0F, 34.0F, 0.0F);

        public final float[] fog;
        public final float near;
        public final float far;
        /** How brightly the stars show. */
        public final float stars;

        Look(float[] fog, float near, float far, float stars) {
            this.fog = fog;
            this.near = near;
            this.far = far;
            this.stars = stars;
        }

        public static Look current() {
            return switch (ClientSoul.aspect()) {
                case "shadow_slave" -> SHADOW;
                case "light_bringer" -> LIGHT;
                case "prince_of_nothing" -> NOTHING;
                default -> STILL;
            };
        }
    }

    private static boolean inSea() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.dimension().equals(SoulSea.DIMENSION);
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (inSea()) {
            Look look = Look.current();
            event.setNearPlaneDistance(look.near);
            event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), look.far));
            event.setFogShape(FogShape.CYLINDER);
            event.setCanceled(true);
        }
    }

    public static void onFogColour(ViewportEvent.ComputeFogColor event) {
        if (inSea()) {
            float[] fog = Look.current().fog;
            event.setRed(fog[0]);
            event.setGreen(fog[1]);
            event.setBlue(fog[2]);
        }
    }
}
