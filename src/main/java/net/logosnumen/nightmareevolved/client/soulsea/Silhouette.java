package net.logosnumen.nightmareevolved.client.soulsea;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Draws whatever is rendered through it in a single flat colour, keeping only its shape: the black shadows of the slain
 * in Sunny's sea, and his Shadows, dark as their master's.
 */
public final class Silhouette implements MultiBufferSource {
    private final MultiBufferSource inner;
    private final int red;
    private final int green;
    private final int blue;

    public Silhouette(MultiBufferSource inner, int rgb) {
        this.inner = inner;
        this.red = rgb >> 16 & 255;
        this.green = rgb >> 8 & 255;
        this.blue = rgb & 255;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return new Flat(this.inner.getBuffer(type));
    }

    /** Passes every vertex through, but with the silhouette's colour (and the original's alpha, so its outline holds). */
    private final class Flat implements VertexConsumer {
        private final VertexConsumer to;

        Flat(VertexConsumer to) {
            this.to = to;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.to.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.to.setColor(Silhouette.this.red, Silhouette.this.green, Silhouette.this.blue, a);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.to.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.to.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.to.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.to.setNormal(x, y, z);
            return this;
        }
    }
}
