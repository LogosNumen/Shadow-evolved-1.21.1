package net.logosnumen.nightmareevolved.aspect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.series.Roster;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulHooks;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Every Aspect, and the rules for wielding them. Each Aspect's abilities sit on five keys, one per step of Ascension;
 * an ability wakes when the soul reaches its step, costs essence (adjusted by the Flaw and Attributes), and rests
 * after use. Toggles and Transformations are held on until switched off or until the essence to hold them runs out.
 */
public final class Aspects {
    private static Map<String, Aspect> registry;

    private Aspects() {
    }

    private static Map<String, Aspect> registry() {
        if (registry == null) {
            Map<String, Aspect> map = new LinkedHashMap<>();
            for (Aspect aspect : Roster.all()) {
                map.put(aspect.id(), aspect);
            }

            registry = map;
        }

        return registry;
    }

    public static List<Aspect> all() {
        return new ArrayList<>(registry().values());
    }

    @Nullable
    public static Aspect byId(String id) {
        return registry().get(id);
    }

    /** How hard a soul's abilities hit, by its Rank and its Aspect's. */
    public static double power(SoulData soul, Aspect aspect) {
        return SoulRules.power(soul.rank(), aspect.tier());
    }

    public static Ctx ctx(ServerPlayer player, SoulData soul, Aspect aspect, Ability ability) {
        return new Ctx(player, player.serverLevel(), soul, aspect, ability, power(soul, aspect));
    }

    /** Every ability whose hooks are in effect now: woken actives and passives, and held toggles and Transformations. */
    public static List<Ctx> inEffect(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        Aspect aspect = byId(soul.aspect());
        List<Ctx> out = new ArrayList<>();
        if (aspect == null) {
            return out;
        }

        for (Ability ability : aspect.unlocked(soul.rank())) {
            boolean on = switch (ability.kind) {
                case ACTIVE, PASSIVE -> true;
                case TOGGLE -> soul.toggled(ability.id);
                case TRANSFORMATION -> ability.id.equals(soul.form());
            };
            if (on) {
                out.add(ctx(player, soul, aspect, ability));
            }
        }

        return out;
    }

    private static double costShare(ServerPlayer player, SoulData soul, Ability ability) {
        double share = 1.0;
        for (SoulHooks trait : Soul.hooks(player)) {
            share *= trait.cost(player, soul, ability);
        }

        return share;
    }

    private static double cooldownShare(ServerPlayer player, SoulData soul, Ability ability) {
        double share = 1.0;
        for (SoulHooks trait : Soul.hooks(player)) {
            share *= trait.cooldown(player, soul, ability);
        }

        return share;
    }

    private static void tell(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("ability.nightmare_evolved." + key, args), true);
    }

    /**
     * Uses the ability on a step's key (0 Dormant to 4 Supreme): fires an active one, or switches a toggle or
     * Transformation on or off. Says why not on the action bar when it can't. Returns whether anything happened.
     */
    public static boolean use(ServerPlayer player, int slot) {
        if (player.isSpectator() || !player.isAlive()) {
            return false;
        }

        SoulData soul = Soul.of(player);
        Aspect aspect = byId(soul.aspect());
        if (aspect == null) {
            tell(player, "no_aspect");
            return false;
        }

        Stage stage = Stage.bySlot(slot);
        Ability ability = aspect.at(stage);
        if (ability == null) {
            tell(player, "empty", stage.title());
            return false;
        }

        if (!soul.rank().atLeast(stage.unlock)) {
            tell(player, "locked", ability.title(), stage.unlock.title());
            return false;
        }

        if (ability.kind == Ability.Kind.PASSIVE) {
            tell(player, "passive", ability.title());
            return false;
        }

        boolean on = ability.kind == Ability.Kind.TOGGLE ? soul.toggled(ability.id) : ability.id.equals(soul.form());
        if (ability.held() && on) {
            stop(player, soul, aspect, ability);
            return true;
        }

        for (SoulHooks trait : Soul.hooks(player)) {
            Component reason = trait.forbids(player, soul, ability);
            if (reason != null) {
                player.displayClientMessage(reason, true);
                return false;
            }
        }

        long now = player.level().getGameTime();
        long ready = soul.readyAt(ability.id);
        if (now < ready) {
            tell(player, "cooldown", ability.title(), (ready - now + 19) / 20);
            return false;
        }

        double cost = ability.cost * costShare(player, soul, ability);
        if (soul.essence() < (ability.held() ? cost * 2.0 : cost)) {
            tell(player, "no_essence", ability.title());
            return false;
        }

        Ctx ctx = ctx(player, soul, aspect, ability);
        if (ability.held()) {
            start(ctx);
        } else {
            if (!ability.use.use(ctx)) {
                return false;
            }

            soul.spend(cost);
            soul.setReadyAt(ability.id, now + Math.round(ability.cooldown * cooldownShare(player, soul, ability)));
            Kit.pose(player, ability.clip);
        }

        Soul.hooks(player).forEach(trait -> trait.used(player, soul, ability));
        Soul.sync(player);
        return true;
    }

    private static void start(Ctx ctx) {
        ServerPlayer player = ctx.player();
        SoulData soul = ctx.soul();
        Ability ability = ctx.ability();
        if (ability.kind == Ability.Kind.TRANSFORMATION) {
            Ability worn = ctx.aspect().ability(soul.form());
            if (worn != null) {
                stop(player, soul, ctx.aspect(), worn);
            }

            soul.setForm(ability.id);
        } else {
            soul.setToggled(ability.id, true);
        }

        ability.hooks.start(ctx);
        Kit.pose(player, ability.clip);
        Soul.applyStats(player);
    }

    private static void stop(ServerPlayer player, SoulData soul, Aspect aspect, Ability ability) {
        if (ability.kind == Ability.Kind.TRANSFORMATION) {
            soul.setForm("");
        } else {
            soul.setToggled(ability.id, false);
        }

        ability.hooks.stop(ctx(player, soul, aspect, ability));
        soul.setReadyAt(ability.id, player.level().getGameTime() + Math.round(ability.cooldown * cooldownShare(player, soul, ability)));
        Soul.applyStats(player);
        Soul.sync(player);
    }

    /** Switches off every held ability, as when the Aspect changes or the soul rises. */
    public static void stopAll(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        Aspect aspect = byId(soul.aspect());
        if (aspect == null) {
            soul.toggles().clear();
            soul.setForm("");
            return;
        }

        for (Ability ability : aspect.abilities()) {
            if (soul.toggled(ability.id) || ability.id.equals(soul.form())) {
                stop(player, soul, aspect, ability);
            }
        }
    }

    /** Once a second: held abilities draw their essence, and drop when there is not enough. */
    public static void upkeep(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        Aspect aspect = byId(soul.aspect());
        if (aspect == null) {
            return;
        }

        for (Ability ability : aspect.abilities()) {
            boolean on = ability.kind == Ability.Kind.TOGGLE ? soul.toggled(ability.id) : ability.kind == Ability.Kind.TRANSFORMATION && ability.id.equals(soul.form());
            if (!on) {
                continue;
            }

            if (!soul.rank().atLeast(ability.stage.unlock) || !soul.spend(ability.cost * costShare(player, soul, ability))) {
                stop(player, soul, aspect, ability);
                tell(player, "faded", ability.title());
            } else {
                Soul.hooks(player).forEach(trait -> trait.used(player, soul, ability));
            }
        }
    }
}
