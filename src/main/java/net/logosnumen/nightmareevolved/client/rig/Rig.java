package net.logosnumen.nightmareevolved.client.rig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.mojang.blaze3d.platform.NativeImage;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * A creature's skeleton and skin, written in code: bones that turn, the boxes on them, and what each box is made
 * of. Baking it lays every box out on one sheet, builds the model, and paints the sheet (and a second sheet with only
 * what glows) straight from the materials, so no form needs a hand-made texture.
 *
 * <p>Units are the usual model sixteenths of a block, with y pointing down and the creature facing -z.
 */
public final class Rig {
    /** Painted pixels per model unit, for finer detail than the model's own grid. */
    static final int RES = 2;
    private static final int SHEET = 256;
    private static final int PAD = 1;

    private record Bone(String name, @Nullable String parent, PartPose pose) {
    }

    private record Box(String bone, Material material, float x, float y, float z, int w, int h, int d) {
        int width() {
            return 2 * (this.d + this.w);
        }

        int height() {
            return this.d + this.h;
        }
    }

    /** A baked form: its model, every bone by name, and its two painted sheets. */
    public record Baked(ModelPart root, Map<String, ModelPart> parts, ResourceLocation skin, ResourceLocation glow) {
        @Nullable
        public ModelPart part(String name) {
            return this.parts.get(name);
        }
    }

    private final Map<String, Bone> bones = new LinkedHashMap<>();
    private final List<Box> boxes = new ArrayList<>();

    /** A bone at (x, y, z) from its parent's pivot (null for the root), resting at the given turn in degrees. */
    public Rig bone(String name, @Nullable String parent, float x, float y, float z, float xRot, float yRot, float zRot) {
        if (parent != null && !this.bones.containsKey(parent)) {
            throw new IllegalArgumentException("Bone " + name + " needs its parent " + parent + " first");
        }

        this.bones.put(name, new Bone(name, parent, PartPose.offsetAndRotation(x, y, z, xRot * Mth.DEG_TO_RAD, yRot * Mth.DEG_TO_RAD, zRot * Mth.DEG_TO_RAD)));
        return this;
    }

    public Rig bone(String name, @Nullable String parent, float x, float y, float z) {
        return this.bone(name, parent, x, y, z, 0.0F, 0.0F, 0.0F);
    }

    /** A box on a bone, from (x, y, z) relative to the bone's pivot, w by h by d. */
    public Rig box(String bone, Material material, float x, float y, float z, int w, int h, int d) {
        if (!this.bones.containsKey(bone)) {
            throw new IllegalArgumentException("No bone " + bone);
        }

        this.boxes.add(new Box(bone, material, x, y, z, Math.max(0, w), Math.max(0, h), Math.max(0, d)));
        return this;
    }

    /** The same box on a bone and, flipped across the middle, on its twin (names ending _r and _l). */
    public Rig pair(String bone, Material material, float x, float y, float z, int w, int h, int d) {
        this.box(bone + "_r", material, x, y, z, w, h, d);
        return this.box(bone + "_l", material, -x - w, y, z, w, h, d);
    }

    /** A bone and its mirror twin: the right one (_r) as given, the left one (_l) flipped across the middle. */
    public Rig pairBone(String name, @Nullable String parent, float x, float y, float z, float xRot, float yRot, float zRot) {
        String parentR = parent != null && this.bones.containsKey(parent + "_r") ? parent + "_r" : parent;
        String parentL = parent != null && this.bones.containsKey(parent + "_l") ? parent + "_l" : parent;
        this.bone(name + "_r", parentR, x, y, z, xRot, yRot, zRot);
        return this.bone(name + "_l", parentL, -x, y, z, xRot, -yRot, -zRot);
    }

    public boolean has(String bone) {
        return this.bones.containsKey(bone);
    }

    /** Lays the boxes out, builds the model and paints its sheets, registering them under the given name. */
    public Baked bake(Look look, String name) {
        // Shelf packing: tallest first, left to right, a new row whenever one fills.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < this.boxes.size(); i++) {
            order.add(i);
        }

        order.sort(Comparator.comparingInt((Integer i) -> this.boxes.get(i).height()).reversed());
        int[] us = new int[this.boxes.size()];
        int[] vs = new int[this.boxes.size()];
        int x = 0;
        int y = 0;
        int row = 0;
        for (int i : order) {
            Box box = this.boxes.get(i);
            int w = box.width() + PAD;
            if (x + w > SHEET && x > 0) {
                x = 0;
                y += row;
                row = 0;
            }

            us[i] = x;
            vs[i] = y;
            x += w;
            row = Math.max(row, box.height() + PAD);
        }

        int sheetHeight = Math.max(16, Mth.roundToward(y + row, 16));
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> defs = new HashMap<>();
        for (Bone bone : this.bones.values()) {
            CubeListBuilder cubes = CubeListBuilder.create();
            for (int i = 0; i < this.boxes.size(); i++) {
                Box box = this.boxes.get(i);
                if (box.bone().equals(bone.name())) {
                    cubes.texOffs(us[i], vs[i]).addBox(box.x(), box.y(), box.z(), box.w(), box.h(), box.d());
                }
            }

            PartDefinition parent = bone.parent() == null ? mesh.getRoot() : defs.get(bone.parent());
            defs.put(bone.name(), parent.addOrReplaceChild(bone.name(), cubes, bone.pose()));
        }

        ModelPart root = LayerDefinition.create(mesh, SHEET, sheetHeight).bakeRoot();
        Map<String, ModelPart> parts = new HashMap<>();
        for (Bone bone : this.bones.values()) {
            ModelPart parent = bone.parent() == null ? root : parts.get(bone.parent());
            parts.put(bone.name(), parent.getChild(bone.name()));
        }

        NativeImage skin = new NativeImage(SHEET * RES, sheetHeight * RES, true);
        NativeImage glow = new NativeImage(SHEET * RES, sheetHeight * RES, true);
        for (int i = 0; i < this.boxes.size(); i++) {
            paint(this.boxes.get(i), us[i], vs[i], look, i * 7, skin, glow);
        }

        ResourceLocation skinId = NightmareEvolved.id("rig/" + name);
        ResourceLocation glowId = NightmareEvolved.id("rig/" + name + "_glow");
        Minecraft.getInstance().getTextureManager().register(skinId, new DynamicTexture(skin));
        Minecraft.getInstance().getTextureManager().register(glowId, new DynamicTexture(glow));
        return new Baked(root, parts, skinId, glowId);
    }

    /** Paints a box's six faces where the model expects them, each a little lighter or darker by which way it faces. */
    private static void paint(Box box, int u, int v, Look look, int seed, NativeImage skin, NativeImage glow) {
        int w = box.w();
        int h = box.h();
        int d = box.d();
        Material m = box.material();
        face(m, u + d, v, w, d, 1.1F, look, seed, skin, glow, false);
        // The underside's rows run the other way, so flip them to keep its pattern lined up with the top's.
        face(m, u + d + w, v, w, d, 0.72F, look, seed + 1, skin, glow, true);
        face(m, u, v + d, d, h, 0.9F, look, seed + 2, skin, glow, false);
        face(m, u + d, v + d, w, h, 1.0F, look, seed + 3, skin, glow, false);
        face(m, u + d + w, v + d, d, h, 0.9F, look, seed + 4, skin, glow, false);
        face(m, u + 2 * d + w, v + d, w, h, 0.84F, look, seed + 5, skin, glow, false);
    }

    private static void face(Material material, int u, int v, int w, int h, float light, Look look, int seed, NativeImage skin, NativeImage glow, boolean flip) {
        int pw = w * RES;
        int ph = h * RES;
        for (int py = 0; py < ph; py++) {
            for (int px = 0; px < pw; px++) {
                int row = flip ? ph - 1 - py : py;
                int colour = material.colour(look, px, row, pw, ph, seed);
                if (FastColor.ARGB32.alpha(colour) == 0) {
                    continue;
                }

                skin.setPixelRGBA(u * RES + px, v * RES + py, FastColor.ABGR32.fromArgb32(Material.shade(colour, light)));
                int lit = material.glowColour(look, px, row, pw, ph, seed);
                if (lit != 0) {
                    glow.setPixelRGBA(u * RES + px, v * RES + py, FastColor.ABGR32.fromArgb32(lit));
                }
            }
        }
    }
}
