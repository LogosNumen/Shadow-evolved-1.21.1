package net.logosnumen.nightmareevolved.client.dream;

import java.util.List;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.gen.landmark.CrimsonSpire;
import net.logosnumen.nightmareevolved.dream.gen.landmark.DesertPlaces;
import net.logosnumen.nightmareevolved.dream.gen.landmark.IslePlaces;
import net.logosnumen.nightmareevolved.dream.law.Cloudveil;
import net.logosnumen.nightmareevolved.dream.law.DarkSea;
import net.logosnumen.nightmareevolved.network.RealmPayload;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * What the Dream Realm draws besides its skies:
 * <ul>
 * <li>Over the sky: the Chained Isles' Sky Below with its own stars, the Moonriver Plain's other two moons, the
 * Desert's blinding sun, and the far landmarks one can see from a region though they stand beyond sight (the Crimson
 * Spire from the Shore, the Tomb of Ariel from the Desert, the Ivory Tower from the Isles, Bastion and the Black
 * Obelisk across their lands and water).</li>
 * <li>Over the land: the Dark Sea's black, still surface wherever it has risen over the Shore, and the columns of
 * white light where Godgrave's veil is torn.</li>
 * </ul>
 */
public final class RealmRender {
    private static final ResourceLocation GLARE = NightmareEvolved.id("textures/environment/glare.png");
    private static final ResourceLocation MOONS = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final Long2BooleanOpenHashMap SEA_CHUNKS = new Long2BooleanOpenHashMap();
    private static VertexBuffer stars;
    private static VertexBuffer starsBelow;

    /** A landmark seen from afar, as a silhouette on the horizon. */
    private record Far(Places.Place place, int base, float width, float height, float topWidth, float r, float g, float b, Region... from) {
    }

    private static final List<Far> FAR = List.of(
        new Far(Places.CRIMSON_SPIRE, CrimsonSpire.BASE, CrimsonSpire.BASE_RADIUS, CrimsonSpire.TOP - CrimsonSpire.BASE, CrimsonSpire.TOP_RADIUS,
            0.24F, 0.05F, 0.06F, Region.FORGOTTEN_SHORE, Region.SEA_OF_ASH, Region.LAND_OF_BARROWS),
        new Far(Places.TOMB_OF_ARIEL, DesertPlaces.TombOfAriel.BASE, DesertPlaces.TombOfAriel.HALF, DesertPlaces.TombOfAriel.HEIGHT, 0.0F, 0.03F, 0.03F,
            0.04F, Region.NIGHTMARE_DESERT),
        new Far(Places.IVORY_TOWER, IslePlaces.IvoryTower.TOP, 9.0F, 72.0F, 2.0F, 0.95F, 0.94F, 0.9F, Region.CHAINED_ISLES),
        new Far(Places.BASTION, 70, 140.0F, 128.0F, 120.0F, 0.42F, 0.43F, 0.45F, Region.BASTION),
        new Far(Places.BLACK_OBELISK, 72, 13.0F, 178.0F, 3.0F, 0.05F, 0.05F, 0.06F, Region.STORMSEA)
    );

    private RealmRender() {
    }

    /** Runs some drawing with the model-view matrix cleared, for vertices already placed in view space. */
    static void identityModelView(Runnable draw) {
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.identity();
        RenderSystem.applyModelViewMatrix();
        try {
            draw.run();
        } finally {
            stack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /** A square facing the camera at a distance along a direction, textured or plain. */
    static void billboard(Matrix4f m, Vector3f direction, float distance, float size, boolean textured) {
        Quaternionf face = new Quaternionf().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), direction);
        Vector3f centre = new Vector3f(direction).mul(distance);
        Vector3f a = new Vector3f(-size, -size, 0.0F).rotate(face).add(centre);
        Vector3f b = new Vector3f(size, -size, 0.0F).rotate(face).add(centre);
        Vector3f c = new Vector3f(size, size, 0.0F).rotate(face).add(centre);
        Vector3f d = new Vector3f(-size, size, 0.0F).rotate(face).add(centre);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, textured ? DefaultVertexFormat.POSITION_TEX : DefaultVertexFormat.POSITION);
        if (textured) {
            builder.addVertex(m, a.x, a.y, a.z).setUv(0.0F, 0.0F);
            builder.addVertex(m, b.x, b.y, b.z).setUv(1.0F, 0.0F);
            builder.addVertex(m, c.x, c.y, c.z).setUv(1.0F, 1.0F);
            builder.addVertex(m, d.x, d.y, d.z).setUv(0.0F, 1.0F);
        } else {
            builder.addVertex(m, a.x, a.y, a.z);
            builder.addVertex(m, b.x, b.y, b.z);
            builder.addVertex(m, c.x, c.y, c.z);
            builder.addVertex(m, d.x, d.y, d.z);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    /** How brightly a tear in the veil shows: rising through its warning, full while it burns. */
    static float tearStrength(RealmPayload.Tear tear, long now, float partialTick) {
        double t = now - tear.start() + partialTick;
        if (t < 0.0 || t > Cloudveil.WARNING + Cloudveil.BURN + 20) {
            return 0.0F;
        }

        if (t < Cloudveil.WARNING) {
            return (float)(t / Cloudveil.WARNING) * 0.45F;
        }

        if (t < Cloudveil.WARNING + Cloudveil.BURN) {
            return 1.0F;
        }

        return (float)(1.0 - (t - Cloudveil.WARNING - Cloudveil.BURN) / 20.0);
    }

    /** The realm's stars, faint or bright; below the horizon only, for the Isles' Sky Below. */
    static void stars(Matrix4f modelView, float alpha, boolean below) {
        if (alpha <= 0.0F) {
            return;
        }

        if (stars == null) {
            stars = buildStars(false);
            starsBelow = buildStars(true);
        }

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderColor(0.85F, 0.88F, 1.0F, alpha);
        VertexBuffer buffer = below ? starsBelow : stars;
        PoseStack pose = new PoseStack();
        pose.mulPose(modelView);
        buffer.bind();
        buffer.drawWithShader(pose.last().pose(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private static VertexBuffer buildStars(boolean below) {
        RandomSource random = RandomSource.create(below ? 9071L : 3301L);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 2200; i++) {
            float x = random.nextFloat() * 2.0F - 1.0F;
            float y = random.nextFloat() * 2.0F - 1.0F;
            float z = random.nextFloat() * 2.0F - 1.0F;
            if (below && y > -0.04F) {
                continue;
            }

            float size = 0.08F + random.nextFloat() * 0.12F;
            float lengthSq = Mth.lengthSquared(x, y, z);
            if (lengthSq > 0.01F && lengthSq < 1.0F) {
                Vector3f dir = new Vector3f(x, y, z).normalize(100.0F);
                Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), dir).rotateZ((float)(random.nextDouble() * Math.PI * 2.0));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(size, -size, 0.0F).rotate(rotation)));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(size, size, 0.0F).rotate(rotation)));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(-size, size, 0.0F).rotate(rotation)));
                builder.addVertex(new Vector3f(dir).add(new Vector3f(-size, -size, 0.0F).rotate(rotation)));
            }
        }

        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.buildOrThrow());
        VertexBuffer.unbind();
        return buffer;
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || !DreamRealm.is(level)) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            overSky(level, event.getModelViewMatrix(), event.getCamera(), partialTick);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            darkSea(level, event.getModelViewMatrix(), event.getCamera(), partialTick);
            beams(level, event.getModelViewMatrix(), event.getCamera(), partialTick);
        }
    }

    private static void overSky(ClientLevel level, Matrix4f modelView, Camera camera, float partialTick) {
        Region.Sky sky = ClientRealm.sky();
        float day = DreamSky.daylight(level, partialTick);
        identityModelView(() -> {
            if (sky == Region.Sky.ISLES) {
                // The Sky Below: a velvety dark under the islands, with stars of its own and the far glow of divine flame.
                RenderSystem.depthMask(false);
                RenderSystem.setShader(GameRenderer::getPositionShader);
                PoseStack pose = new PoseStack();
                pose.mulPose(modelView);
                Matrix4f m = pose.last().pose();
                RenderSystem.setShaderColor(0.02F + 0.06F * day, 0.02F + 0.07F * day, 0.05F + 0.12F * day, 1.0F);
                BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
                builder.addVertex(m, 0.0F, -60.0F, 0.0F);
                for (int i = 180; i >= -180; i -= 20) {
                    float a = i * Mth.DEG_TO_RAD;
                    builder.addVertex(m, 512.0F * Mth.cos(a), -4.0F, 512.0F * Mth.sin(a));
                }

                BufferUploader.drawWithShader(builder.buildOrThrow());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                stars(modelView, 0.55F + 0.35F * (1.0F - day), true);
            } else if (sky == Region.Sky.MOONRIVER && day < 0.6F) {
                moons(level, modelView, partialTick, 1.0F - day / 0.6F);
            } else if (sky == Region.Sky.DESERT && day > 0.2F) {
                glare(level, modelView, partialTick, day);
            }

            silhouettes(level, modelView, camera, day);
        });
    }

    /** The Moonriver Plain's other two moons, at their own places in the sky and their own phases. */
    private static void moons(ClientLevel level, Matrix4f modelView, float partialTick, float strength) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MOONS);
        for (int m = 1; m <= 2; m++) {
            PoseStack pose = new PoseStack();
            pose.mulPose(modelView);
            DreamSky.sunRotation(pose, level, partialTick);
            pose.mulPose(Axis.ZP.rotationDegrees(m == 1 ? 34.0F : -27.0F));
            pose.mulPose(Axis.XP.rotationDegrees(m == 1 ? 22.0F : -31.0F));
            Matrix4f mat = pose.last().pose();
            int phase = (level.getMoonPhase() + m * 3) % 8;
            float u0 = (phase % 4) / 4.0F;
            float v0 = (phase / 4) / 2.0F;
            float size = m == 1 ? 14.0F : 9.0F;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, strength);
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(mat, -size, -100.0F, size).setUv(u0 + 0.25F, v0 + 0.5F);
            builder.addVertex(mat, size, -100.0F, size).setUv(u0, v0 + 0.5F);
            builder.addVertex(mat, size, -100.0F, -size).setUv(u0, v0);
            builder.addVertex(mat, -size, -100.0F, -size).setUv(u0 + 0.25F, v0);
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    /** The Desert's sun is a blinding disc: a glare around it far wider than the sun itself. */
    private static void glare(ClientLevel level, Matrix4f modelView, float partialTick, float day) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GLARE);
        PoseStack pose = new PoseStack();
        pose.mulPose(modelView);
        DreamSky.sunRotation(pose, level, partialTick);
        Matrix4f mat = pose.last().pose();
        for (int layer = 0; layer < 2; layer++) {
            float size = layer == 0 ? 52.0F : 110.0F;
            RenderSystem.setShaderColor(1.0F, 0.97F, 0.85F, (layer == 0 ? 0.7F : 0.25F) * day);
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(mat, -size, 100.0F, -size).setUv(0.0F, 0.0F);
            builder.addVertex(mat, size, 100.0F, -size).setUv(1.0F, 0.0F);
            builder.addVertex(mat, size, 100.0F, size).setUv(1.0F, 1.0F);
            builder.addVertex(mat, -size, 100.0F, size).setUv(0.0F, 1.0F);
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    /**
     * Far landmarks as silhouettes on the horizon, hazed by distance, drawn under the land so anything nearer hides
     * them. The Tomb of Ariel shimmers like the mirage it is by day.
     */
    private static void silhouettes(ClientLevel level, Matrix4f modelView, Camera camera, float day) {
        Region region = ClientRealm.region();
        Vec3 at = camera.getPosition();
        float view = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        PoseStack pose = new PoseStack();
        pose.mulPose(modelView);
        Matrix4f m = pose.last().pose();
        float[] fog = RenderSystem.getShaderFogColor();
        for (Far far : FAR) {
            boolean seen = false;
            for (Region from : far.from) {
                seen |= from == region;
            }

            double dx = far.place.x() + 0.5 - at.x;
            double dz = far.place.z() + 0.5 - at.z;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (!seen || distance < view * 0.85 || distance > 4200.0) {
                continue;
            }

            float scale = (float)(380.0 / distance);
            float haze = (float)Math.min(0.85, Math.pow(distance / 4200.0, 0.8));
            float alpha = 1.0F - haze * 0.6F;
            if (far.place == Places.TOMB_OF_ARIEL) {
                alpha *= 0.65F + 0.25F * Mth.sin((level.getGameTime() % 1000) * 0.05F) * day;
            }

            float r = Mth.lerp(haze, far.r, fog[0]);
            float g = Mth.lerp(haze, far.g, fog[1]);
            float b = Mth.lerp(haze, far.b, fog[2]);
            float ux = (float)(dx / distance);
            float uz = (float)(dz / distance);
            float cx = ux * 380.0F;
            float cz = uz * 380.0F;
            float sx = -uz;
            float sz = ux;
            float bottom = (float)((far.base - at.y) * scale);
            float top = bottom + far.height * scale;
            float wb = far.width * scale;
            float wt = far.topWidth * scale;
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.addVertex(m, cx - sx * wb, bottom, cz - sz * wb).setColor(r, g, b, alpha);
            builder.addVertex(m, cx + sx * wb, bottom, cz + sz * wb).setColor(r, g, b, alpha);
            builder.addVertex(m, cx + sx * wt, top, cz + sz * wt).setColor(r, g, b, alpha);
            builder.addVertex(m, cx - sx * wt, top, cz - sz * wt).setColor(r, g, b, alpha);
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /**
     * The Dark Sea's surface: one flat black plane at the tide's height over every part of the Shore it reaches, hidden
     * by whatever stands above it and fading into the fog at the edge of sight.
     */
    private static void darkSea(ClientLevel level, Matrix4f modelView, Camera camera, float partialTick) {
        if (ClientRealm.sealed()) {
            return;
        }

        Region region = ClientRealm.region();
        if (region != Region.FORGOTTEN_SHORE && region != Region.SEA_OF_ASH && region != Region.LAND_OF_BARROWS
            && region != Region.HOLLOW_MOUNTAINS && region != Region.NIGHTMARE_DESERT) {
            return;
        }

        Vec3 at = camera.getPosition();
        float tide = (float)DarkSea.level(level.getDayTime(), partialTick);
        int radius = Minecraft.getInstance().options.getEffectiveRenderDistance();
        int ccx = Mth.floor(at.x) >> 4;
        int ccz = Mth.floor(at.z) >> 4;
        float y = (float)(tide - at.y) + 0.88F;
        float fogStart = RenderSystem.getShaderFogStart();
        float fogEnd = Math.max(fogStart + 1.0F, RenderSystem.getShaderFogEnd());
        float[] fog = RenderSystem.getShaderFogColor();
        identityModelView(() -> {
            PoseStack pose = new PoseStack();
            pose.mulPose(modelView);
            Matrix4f m = pose.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            boolean any = false;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int cx = ccx + dx;
                    int cz = ccz + dz;
                    if (!reaches(cx, cz)) {
                        continue;
                    }

                    float x0 = (float)((cx << 4) - at.x);
                    float z0 = (float)((cz << 4) - at.z);
                    vertex(builder, m, x0, y, z0, fogStart, fogEnd, fog);
                    vertex(builder, m, x0, y, z0 + 16.0F, fogStart, fogEnd, fog);
                    vertex(builder, m, x0 + 16.0F, y, z0 + 16.0F, fogStart, fogEnd, fog);
                    vertex(builder, m, x0 + 16.0F, y, z0, fogStart, fogEnd, fog);
                    any = true;
                }
            }

            if (any) {
                BufferUploader.drawWithShader(builder.buildOrThrow());
            } else {
                builder.build();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        });
    }

    private static void vertex(BufferBuilder builder, Matrix4f m, float x, float y, float z, float fogStart, float fogEnd, float[] fog) {
        float distance = Mth.sqrt(x * x + z * z);
        float f = Mth.clamp((distance - fogStart) / (fogEnd - fogStart), 0.0F, 1.0F);
        float shimmer = 0.012F * Mth.sin((x + z) * 0.05F);
        builder.addVertex(m, x, y, z).setColor(Mth.lerp(f, 0.012F + shimmer, fog[0]), Mth.lerp(f, 0.012F + shimmer, fog[1]),
            Mth.lerp(f, 0.024F + shimmer, fog[2]), 0.97F);
    }

    /** Whether the Dark Sea can reach a chunk (remembered, since the Shore's shape never changes). */
    private static boolean reaches(int cx, int cz) {
        long key = ((long)cx << 32) ^ (cz & 0xFFFFFFFFL);
        if (SEA_CHUNKS.size() > 65536) {
            SEA_CHUNKS.clear();
        }

        return SEA_CHUNKS.computeIfAbsent(key, k -> DarkSea.reaches((cx << 4) + 8, (cz << 4) + 8));
    }

    /** The white sky through the veil's tears: columns of light from the clouds to the ground. */
    private static void beams(ClientLevel level, Matrix4f modelView, Camera camera, float partialTick) {
        if (ClientRealm.tears().isEmpty()) {
            return;
        }

        Vec3 at = camera.getPosition();
        long now = level.getGameTime();
        identityModelView(() -> {
            PoseStack pose = new PoseStack();
            pose.mulPose(modelView);
            Matrix4f m = pose.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            for (RealmPayload.Tear tear : ClientRealm.tears()) {
                float strength = tearStrength(tear, now, partialTick);
                if (strength <= 0.0F) {
                    continue;
                }

                BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                float cx = (float)(tear.x() - at.x);
                float cz = (float)(tear.z() - at.z);
                float lo = (float)(40.0 - at.y);
                float hi = (float)(380.0 - at.y);
                float burning = strength >= 1.0F ? 0.55F : strength * 0.4F;
                for (int i = 0; i < 24; i++) {
                    float a0 = i * Mth.TWO_PI / 24.0F;
                    float a1 = (i + 1) * Mth.TWO_PI / 24.0F;
                    float r = tear.radius();
                    builder.addVertex(m, cx + Mth.cos(a0) * r, lo, cz + Mth.sin(a0) * r).setColor(1.0F, 1.0F, 1.0F, burning);
                    builder.addVertex(m, cx + Mth.cos(a1) * r, lo, cz + Mth.sin(a1) * r).setColor(1.0F, 1.0F, 1.0F, burning);
                    builder.addVertex(m, cx + Mth.cos(a1) * r, hi, cz + Mth.sin(a1) * r).setColor(1.0F, 1.0F, 1.0F, burning * 0.3F);
                    builder.addVertex(m, cx + Mth.cos(a0) * r, hi, cz + Mth.sin(a0) * r).setColor(1.0F, 1.0F, 1.0F, burning * 0.3F);
                }

                BufferUploader.drawWithShader(builder.buildOrThrow());
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        });
    }
}
