package net.logosnumen.nightmareevolved.client.creature;

import java.util.HashMap;
import java.util.Map;
import com.mojang.logging.LogUtils;
import net.logosnumen.nightmareevolved.client.creature.bastion.BastionModels;
import net.logosnumen.nightmareevolved.client.creature.burned.BurnedModels;
import net.logosnumen.nightmareevolved.client.creature.desert.DesertModels;
import net.logosnumen.nightmareevolved.client.creature.godgrave.GodgraveBeasts;
import net.logosnumen.nightmareevolved.client.creature.godgrave.GodgraveGreats;
import net.logosnumen.nightmareevolved.client.creature.isles.IsleBeasts;
import net.logosnumen.nightmareevolved.client.creature.isles.IsleFolk;
import net.logosnumen.nightmareevolved.client.creature.isles.IsleHorrors;
import net.logosnumen.nightmareevolved.client.creature.moonriver.MoonriverModels;
import net.logosnumen.nightmareevolved.client.creature.mountains.MountainModels;
import net.logosnumen.nightmareevolved.client.creature.ravenheart.RavenheartModels;
import net.logosnumen.nightmareevolved.client.creature.shore.CarapaceModels;
import net.logosnumen.nightmareevolved.client.creature.shore.ShoreBeasts;
import net.logosnumen.nightmareevolved.client.creature.shore.ShoreFolk;
import net.logosnumen.nightmareevolved.client.creature.shore.ShoreHorrors;
import net.logosnumen.nightmareevolved.client.creature.stormsea.StormseaModels;
import net.logosnumen.nightmareevolved.client.rig.Look;
import net.logosnumen.nightmareevolved.client.rig.Material;
import net.logosnumen.nightmareevolved.client.rig.Pose;
import net.logosnumen.nightmareevolved.client.rig.Rig;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import org.slf4j.Logger;

/** Which body each kind of creature wears. A kind without one of its own wears a plain dark shape of its size. */
public final class CreatureModels {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, CreatureModel> MODELS = new HashMap<>();

    static {
        MODELS.put("carapace_scavenger", new CarapaceModels.Scavenger());
        MODELS.put("carapace_centurion", new CarapaceModels.Centurion());
        MODELS.put("carapace_demon", new CarapaceModels.Demon());
        MODELS.put("forsaken_knight", new ShoreFolk.Knight());
        MODELS.put("stone_saint", new ShoreFolk.Saint());
        MODELS.put("crimson_golem", new ShoreFolk.Golem());
        MODELS.put("blood_fiend", new ShoreFolk.Fiend());
        Look messenger = new Look(0x18141A, 0x060406, 0x3A3440, 0xDCD4CC, 0x2A2428, 0xFF2A2A, 0x2A2228);
        Look bloodied = new Look(0x1A1012, 0x080304, 0x4A2026, 0xD0C4BC, 0x2A1A1C, 0xFF3A2A, 0x3A1A1E);
        MODELS.put("spire_messenger", new ShoreBeasts.Herald(1.05F, messenger, bloodied));
        MODELS.put("cursed_herald", new ShoreBeasts.Herald(1.4F, bloodied));
        MODELS.put("iron_spider", new ShoreBeasts.Spider(1.0F, false));
        MODELS.put("iron_spider_matriarch", new ShoreBeasts.Spider(1.7F, true));
        MODELS.put("flesh_reaver", new ShoreBeasts.Reaver());
        MODELS.put("corpse_eater", new ShoreBeasts.Leech());
        MODELS.put("weeping_beast", new ShoreBeasts.Weeper());
        MODELS.put("crimson_terror", new ShoreHorrors.Terror());
        MODELS.put("lord_of_the_dead", new ShoreHorrors.BoneLord());
        MODELS.put("soul_devouring_tree", new ShoreHorrors.SoulTree());
        MODELS.put("blood_flower", new ShoreHorrors.Flower());
        MODELS.put("rolling_stone", new ShoreHorrors.Stone());
        MODELS.put("vile_thieving_birds_spawn", new ShoreHorrors.Egg());
        MODELS.put("black_ravager", new IsleBeasts.Ravager());
        MODELS.put("dread_wolf", new IsleBeasts.DreadWolf());
        MODELS.put("griffin", new IsleBeasts.Griffin());
        MODELS.put("giant_ant", new IsleBeasts.Ant());
        MODELS.put("chain_worm", new IsleFolk.ChainWorm());
        MODELS.put("chalice_maiden", new IsleFolk.Maiden());
        MODELS.put("mirror_beast", new IsleFolk.Mirror());
        MODELS.put("sailor_doll", new IsleFolk.Doll());
        MODELS.put("stone_gargoyle", new IsleFolk.Gargoyle());
        MODELS.put("mordant_mimic", new IsleHorrors.Mimic());
        MODELS.put("wormvine", new IsleHorrors.Wormvine());
        MODELS.put("blood_nourished_root", new IsleHorrors.Root());
        MODELS.put("isle_horror", new IsleHorrors.IsleHorror());
        MODELS.put("mist_being", new MountainModels.MistBeing());
        MODELS.put("tainted_saint", new MountainModels.TaintedSaint());
        MODELS.put("absence", new MountainModels.Absence());
        MODELS.put("dark_one", new MountainModels.DarkOne());
        MODELS.put("jade_titan", new MountainModels.JadeTitan());
        MODELS.put("horror_of_the_lightless_current", new MountainModels.LightlessHorror());
        MODELS.put("deathless", new DesertModels.Warrior());
        MODELS.put("deathless_giant", new DesertModels.Giant());
        MODELS.put("skinwalker", new DesertModels.Vessel());
        MODELS.put("asura_of_condemnation", new GodgraveGreats.Asura());
        MODELS.put("condemnation", new GodgraveGreats.Condemnation());
        MODELS.put("abjuration", new GodgraveGreats.Abjuration());
        MODELS.put("guardian_of_vanishing_lake", new GodgraveGreats.LakeGuardian());
        MODELS.put("maggot_beast", new GodgraveBeasts.MaggotBeast());
        MODELS.put("jackal_giant", new GodgraveBeasts.JackalGiant());
        MODELS.put("crimson_python", new GodgraveBeasts.Python());
        MODELS.put("black_tiger", new GodgraveBeasts.BlackTiger());
        MODELS.put("blood_mosquito", new GodgraveBeasts.Mosquito());
        MODELS.put("harpy", new GodgraveBeasts.Harpy());
        MODELS.put("three_headed_hound", new GodgraveBeasts.Hound());
        MODELS.put("viper_lion", new GodgraveBeasts.ViperLion());
        MODELS.put("tyrant_lizard", new GodgraveBeasts.TyrantLizard());
        MODELS.put("ash_worm", new GodgraveBeasts.AshWorm());
        MODELS.put("depth_dweller", new GodgraveGreats.DepthDweller());
        MODELS.put("black_millipede", new BurnedModels.Millipede());
        MODELS.put("painted_millipede", new BurnedModels.Painted());
        MODELS.put("millipede_queen", new BurnedModels.Queen());
        MODELS.put("chalice_knight", new RavenheartModels.ChaliceKnight());
        MODELS.put("stone_worm", new RavenheartModels.StoneWorm());
        MODELS.put("gaunt_maw", new MoonriverModels.GauntMaw());
        MODELS.put("undetectable_forest_maw", new BastionModels.ForestMaw());
        MODELS.put("sea_serpent", new StormseaModels.SeaSerpent());
        MODELS.put("old_tom", new StormseaModels.OldTom());
    }

    private CreatureModels() {
    }

    /**
     * Builds every kind's body in every look once, without painting it, and reports any that can't be built: a body
     * that names a bone it doesn't have would otherwise only fail the first time one of its kind is drawn.
     */
    public static void verify() {
        for (Kind kind : Kinds.all()) {
            for (int look = 0; look < Math.max(1, kind.looks); look++) {
                try {
                    of(kind).build(look);
                } catch (RuntimeException e) {
                    LOGGER.error("The body of {} (look {}) can't be built", kind.id, look, e);
                }
            }

            if (!has(kind)) {
                LOGGER.warn("{} has no body of its own yet", kind.id);
            }
        }
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
