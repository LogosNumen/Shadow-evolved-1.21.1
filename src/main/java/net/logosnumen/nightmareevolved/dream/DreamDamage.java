package net.logosnumen.nightmareevolved.dream;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/** The ways the Dream Realm itself kills: each has its own death message (data/nightmare_evolved/damage_type). */
public final class DreamDamage {
    /** The blade-like coral of the Forgotten Shore. */
    public static final ResourceKey<DamageType> CRIMSON_CORAL = key("crimson_coral");
    /** What lives in the Dark Sea, for anyone caught in it after nightfall. */
    public static final ResourceKey<DamageType> DARK_SEA = key("dark_sea");
    /** The pressure of the Chained Isles' sky as an island rises. */
    public static final ResourceKey<DamageType> CRUSHING = key("crushing");
    /** The mist of the Hollow Mountains, dissolving whoever lingers in it. */
    public static final ResourceKey<DamageType> NOTHINGNESS = key("nothingness");
    /** Godgrave's white sky, where the veil of clouds tears. */
    public static final ResourceKey<DamageType> WHITE_SKY = key("white_sky");
    /** The poisoned water of the Bone Sea. */
    public static final ResourceKey<DamageType> BONE_SEA = key("bone_sea");
    /** Sunlight gathered by the Glass Hell's lenses. */
    public static final ResourceKey<DamageType> FOCUSED_SUN = key("focused_sun");
    /** A Nightmare Gate, for anyone in the waking world who comes too close to it. */
    public static final ResourceKey<DamageType> GATE = key("gate");

    private DreamDamage() {
    }

    private static ResourceKey<DamageType> key(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, NightmareEvolved.id(id));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return level.damageSources().source(type);
    }
}
