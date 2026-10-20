package net.logosnumen.nightmareevolved.aspect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.logosnumen.nightmareevolved.anim.Poses;
import net.logosnumen.nightmareevolved.soul.AspectTier;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Every Aspect the Spell can grant, and the rules for using their abilities. Each Aspect has one ability that wakes
 * when its bearer becomes a Sleeper and another that wakes when they are Awakened.
 */
public final class Aspects {
    private static final Map<String, Aspect> ALL = new LinkedHashMap<>();

    public static final Aspect HOLLOW_SHADE = add(new Aspect("hollow_shade", List.of(
        new Ability("shade_step", Rank.SLEEPER, 6.0, 60, "dash", Powers::shadeStep),
        new Ability("umbral_grasp", Rank.AWAKENED, 15.0, 160, "cast", Powers::umbralGrasp)
    ), Aspect.Flaw.LIGHT_SHY));
    public static final Aspect EMBER_HERALD = add(new Aspect("ember_herald", List.of(
        new Ability("cinder_lash", Rank.SLEEPER, 5.0, 40, "slash", Powers::cinderLash),
        new Ability("dawn_pyre", Rank.AWAKENED, 18.0, 200, "raise_one", Powers::dawnPyre)
    ), Aspect.Flaw.DOUSED));
    public static final Aspect IRON_PENITENT = add(new Aspect("iron_penitent", List.of(
        new Ability("penitents_guard", Rank.SLEEPER, 6.0, 160, "guard", Powers::penitentsGuard),
        new Ability("shackle_break", Rank.AWAKENED, 15.0, 180, "slam", Powers::shackleBreak)
    ), Aspect.Flaw.LEADEN));
    public static final Aspect TIDE_WEAVER = add(new Aspect("tide_weaver", List.of(
        new Ability("undertow", Rank.SLEEPER, 6.0, 100, "channel", Powers::undertow),
        new Ability("riptide_lance", Rank.AWAKENED, 16.0, 160, "cast_both", Powers::riptideLance)
    ), Aspect.Flaw.PARCHED));
    public static final Aspect RUNE_SCRIBE = add(new Aspect("rune_scribe", List.of(
        new Ability("warding_sigil", Rank.SLEEPER, 8.0, 120, "write", Powers::wardingSigil),
        new Ability("glyph_volley", Rank.AWAKENED, 16.0, 140, "point", Powers::glyphVolley)
    ), Aspect.Flaw.BRITTLE));

    private Aspects() {
    }

    private static Aspect add(Aspect aspect) {
        ALL.put(aspect.id(), aspect);
        return aspect;
    }

    public static List<Aspect> all() {
        return new ArrayList<>(ALL.values());
    }

    public static @Nullable Aspect byId(String id) {
        return ALL.get(id);
    }

    public static @Nullable Aspect of(ServerPlayer player) {
        return byId(Soul.of(player).aspect());
    }

    public static boolean hasFlaw(ServerPlayer player, Aspect.Flaw flaw) {
        Aspect aspect = of(player);
        return aspect != null && aspect.flaw() == flaw;
    }

    /** How hard a soul's abilities hit: a stronger Aspect hits harder, and each rank past Sleeper adds a third again. */
    public static double power(AspectTier tier, Rank rank) {
        return tier.potency * (1.0 + 0.35 * Math.max(0, rank.ladder));
    }

    /** What an ability costs here: a Parched soul pays double in the Nether. */
    public static double cost(ServerPlayer player, Ability ability) {
        boolean parched = hasFlaw(player, Aspect.Flaw.PARCHED) && player.level().dimension() == Level.NETHER;
        return parched ? ability.cost() * 2.0 : ability.cost();
    }

    /** Grants a random Aspect at a rolled tier and tells the soul what it has become. */
    public static Aspect grant(ServerPlayer player) {
        List<Aspect> all = all();
        Aspect aspect = all.get(player.getRandom().nextInt(all.size()));
        grant(player, aspect, AspectTier.roll(player.getRandom()));
        return aspect;
    }

    public static void grant(ServerPlayer player, Aspect aspect, AspectTier tier) {
        SoulData soul = Soul.of(player);
        soul.setAspect(aspect.id(), tier);
        Soul.applyStats(player);
        soul.setEssence(soul.maxEssence());
        Spell.say(player, "spell.nightmare_evolved.aspect_granted", aspect.title(), tier.title());
        player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_described", aspect.description()));
        player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_flaw", aspect.flawTitle(), aspect.flawDescription()));
        for (Ability ability : aspect.abilities()) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_ability", title(aspect, ability), ability.unlock().title()));
        }

        Soul.sync(player);
    }

    public static Component title(Aspect aspect, Ability ability) {
        return Component.translatable("ability.nightmare_evolved." + aspect.id() + "." + ability.id());
    }

    /**
     * Uses the ability in the given slot (0 or 1), if the soul's rank has woken it, it is off cooldown and there is
     * essence enough. Says why not otherwise, on the action bar. Returns whether it was used.
     */
    public static boolean use(ServerPlayer player, int slot) {
        if (player.isSpectator() || !player.isAlive()) {
            return false;
        }

        SoulData soul = Soul.of(player);
        Aspect aspect = byId(soul.aspect());
        if (aspect == null) {
            player.displayClientMessage(Component.translatable("ability.nightmare_evolved.no_aspect"), true);
            return false;
        }

        if (slot < 0 || slot >= aspect.abilities().size()) {
            return false;
        }

        Ability ability = aspect.abilities().get(slot);
        if (!soul.rank().atLeast(ability.unlock())) {
            player.displayClientMessage(Component.translatable("ability.nightmare_evolved.locked", title(aspect, ability), ability.unlock().title()), true);
            return false;
        }

        long now = player.level().getGameTime();
        long ready = soul.readyAt(ability.id());
        if (now < ready) {
            player.displayClientMessage(Component.translatable("ability.nightmare_evolved.cooldown", title(aspect, ability), (ready - now + 19) / 20), true);
            return false;
        }

        double cost = cost(player, ability);
        if (soul.essence() < cost) {
            player.displayClientMessage(Component.translatable("ability.nightmare_evolved.no_essence", title(aspect, ability)), true);
            return false;
        }

        ServerLevel level = player.serverLevel();
        if (!ability.effect().use(player, level, power(soul.aspectTier(), soul.rank()))) {
            return false;
        }

        soul.spend(cost);
        soul.setReadyAt(ability.id(), now + ability.cooldown());
        Poses.play(player, ability.clip());
        Soul.sync(player);
        return true;
    }
}
