package net.logosnumen.nightmareevolved.client.form;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.client.creature.CreatureModel;

/**
 * How each Saint's Transformation looks, by the ability's id. Most Saints become a creature of their own, drawn by the
 * rig in place of their body; some keep their body and it changes (to liquid metal, mist, sunlight or shadow); and some
 * Transformations change nothing anyone can see.
 */
public final class FormBodies {
    /** A creature body worn in place of the Saint's own: the move a swing plays, and the tint it is drawn in. */
    public record Body(CreatureModel model, String attack, int tint) {
    }

    /** The Saint's own body, changed: drawn in a tint, and shining at full brightness if it {@code glows}. */
    public record Skin(int tint, boolean glows) {
    }

    private static final Map<String, Body> BODIES = new HashMap<>();
    private static final Map<String, Skin> SKINS = new HashMap<>();

    static {
        BODIES.put("midnight_dragon", new Body(new SaintForms.MidnightDragon(), "bite", 0xFFFFFFFF));
        BODIES.put("thunderbird", new Body(new SaintForms.Thunderbird(), "talons", 0xFFFFFFFF));
        BODIES.put("winged_lion", new Body(new SaintForms.WingedLion(), "maul", 0xFFFFFFFF));
        BODIES.put("graceful_centaur", new Body(new SaintForms.Centaur(), "shoot", 0xFFFFFFFF));
        BODIES.put("black_panther", new Body(new SaintForms.Panther(), "maul", 0xFFFFFFFF));
        BODIES.put("moonlight_wolf", new Body(new SaintForms.MoonlightWolf(), "bite", 0xD8FFFFFF));
        BODIES.put("fiend_of_wrath", new Body(new SaintForms.Fiend(), "claw", 0xFFFFFFFF));
        BODIES.put("monstrous_kin", new Body(new SaintForms.SharkKin(), "rend", 0xFFFFFFFF));
        BODIES.put("lightslayer_form", new Body(new SaintForms.BatWinged(), "claw", 0xFFFFFFFF));
        BODIES.put("tide_of_blood", new Body(new SaintForms.BloodTide(), "surge", 0xE8FFFFFF));
        SKINS.put("river_of_steel", new Skin(0xFFB4BCC8, false));
        SKINS.put("mist_wraith", new Skin(0x70B0B8C4, false));
        SKINS.put("body_of_light", new Skin(0xB8FFE8A0, true));
        SKINS.put("radiant_form", new Skin(0xC8FFF6DC, true));
        SKINS.put("shadow_incarnation", new Skin(0xFF18181F, false));
    }

    private FormBodies() {
    }

    @Nullable
    public static Body body(String form) {
        return BODIES.get(form);
    }

    @Nullable
    public static Skin skin(String form) {
        return SKINS.get(form);
    }

    public static Map<String, Body> bodies() {
        return BODIES;
    }
}
