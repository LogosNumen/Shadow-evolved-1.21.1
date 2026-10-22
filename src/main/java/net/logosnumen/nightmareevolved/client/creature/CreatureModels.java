package net.logosnumen.nightmareevolved.client.creature;

import java.util.HashMap;
import java.util.Map;
import net.logosnumen.nightmareevolved.client.creature.shore.CarapaceModels;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.creature.kind.Kind;

/** Which body each kind of creature wears. A kind without one of its own wears a plain dark shape of its size. */
public final class CreatureModels {
    private static final Map<String, CreatureModel> MODELS = new HashMap<>();

    static {
        MODELS.put("carapace_scavenger", new CarapaceModels.Scavenger());
        MODELS.put("carapace_centurion", new CarapaceModels.Centurion());
        MODELS.put("carapace_demon", new CarapaceModels.Demon());
    }

    private CreatureModels() {
    }

    public static CreatureModel of(Kind kind) {
        return MODELS.computeIfAbsent(kind.id, id -> new Plain(kind));
    }

    /** Whether a kind has a body of its own. */
    public static boolean has(Kind kind) {
        return MODELS.containsKey(kind.id) && !(MODELS.get(kind.id) instanceof Plain);
    }

    /** A dark block of the kind's size with two burning eyes. */
    private static final class Plain extends CreatureModel {
        private static final Look LOOK = new Look(0x2A2430, 0x0E0A12, 0x4A4256, 0x3A3240, 0x8A8090, 0xFF4040, 0x3A3446);
        private final Kind kind;

        Plain(Kind kind) {
            this.kind = kind;
        }

        @Override
        public Look look(int look) {
            return LOOK;
        }

        @Override
        public Rig build(int look) {
            float w = this.kind.width * 16.0F;
            float h = this.kind.height * 16.0F;
            Rig rig = new Rig();
            rig.bone("root", null, 0.0F, 24.0F, 0.0F);
            rig.bone("body", "root", 0.0F, 0.0F, 0.0F);
            box(rig, "body", Material.SMOKE, -w / 2.0F, -h, -w / 2.0F, w, h, w);
            both(rig, "body", Material.EYE, -w / 4.0F, -h * 0.85F, -w / 2.0F - 0.5F, 1.0F, 1.0F, 1.0F);
            return rig;
        }

        @Override
        public void animate(Rig.Baked body, Pose pose, int look) {
            Gaits.fall(body, pose, 0.0F, 90.0F);
        }
    }
}
