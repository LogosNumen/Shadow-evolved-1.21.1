package net.logosnumen.nightmareevolved.aspect;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * One of an Aspect's abilities. Its {@link Kind} decides how it is used: an active ability fires once and rests; a
 * toggle is held on, drawing essence each second; a passive simply is; a Transformation is a toggle that changes the
 * body. {@code cost} is essence per use, or per second for toggles; {@code cooldown} is in ticks. Its {@link Hooks} run
 * while it is unlocked (actives and passives) or while held on (toggles and Transformations). Abilities the novel
 * never shows are marked {@link #filled}: the mod filled the gap in the Aspect's spirit.
 */
public final class Ability {
    public enum Kind {
        ACTIVE,
        TOGGLE,
        PASSIVE,
        TRANSFORMATION
    }

    @FunctionalInterface
    public interface Use {
        /** Fires the ability; false if it found nothing to act on, so nothing is spent. */
        boolean use(Ctx ctx);
    }

    @FunctionalInterface
    public interface Modifiers {
        void add(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation);
    }

    /** What an ability does besides being used. Every method is optional. */
    public interface Hooks {
        Hooks NONE = new Hooks() {
        };

        /** Every tick; {@code age} counts the player's ticks, for things that only happen every second. */
        default void tick(Ctx ctx, int age) {
        }

        /** Lasting changes to the body, reapplied whenever the soul changes. */
        default void modifiers(Ctx ctx, Modifiers out) {
        }

        /** A blow the player lands; returns the damage it should do. */
        default float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
            return amount;
        }

        /** A blow the player takes; returns the damage it should do. */
        default float taken(Ctx ctx, DamageSource source, float amount) {
            return amount;
        }

        default void killed(Ctx ctx, LivingEntity victim) {
        }

        /** Any death within 24 blocks of the player, whoever caused it. */
        default void witnessed(Ctx ctx, LivingEntity victim) {
        }

        /** Whether the soul turns away soul shards while this is in effect (its core grows some other way). */
        default boolean refusesShards(Ctx ctx) {
            return false;
        }

        /** Whether, while this is in effect, the player may fly. */
        default boolean flight(Ctx ctx) {
            return false;
        }

        /** A toggle or Transformation being switched on. */
        default void start(Ctx ctx) {
        }

        /** A toggle or Transformation being switched off, by choice or when essence runs out. */
        default void stop(Ctx ctx) {
        }
    }

    public final String id;
    public final Stage stage;
    public final Kind kind;
    public final double cost;
    public final int cooldown;
    public final String clip;
    public final Use use;
    public final Hooks hooks;
    public final boolean innate;
    public final boolean filled;

    private Ability(String id, Stage stage, Kind kind, double cost, int cooldown, String clip, Use use, Hooks hooks, boolean innate, boolean filled) {
        this.id = id;
        this.stage = stage;
        this.kind = kind;
        this.cost = cost;
        this.cooldown = cooldown;
        this.clip = clip;
        this.use = use;
        this.hooks = hooks;
        this.innate = innate;
        this.filled = filled;
    }

    public static Ability active(String id, Stage stage, double cost, int cooldown, String clip, Use use) {
        return new Ability(id, stage, Kind.ACTIVE, cost, cooldown, clip, use, Hooks.NONE, false, false);
    }

    /** An active ability that also changes something for as long as it is unlocked. */
    public static Ability active(String id, Stage stage, double cost, int cooldown, String clip, Use use, Hooks always) {
        return new Ability(id, stage, Kind.ACTIVE, cost, cooldown, clip, use, always, false, false);
    }

    public static Ability toggle(String id, Stage stage, double costPerSecond, String clip, Hooks whileOn) {
        return new Ability(id, stage, Kind.TOGGLE, costPerSecond, 20, clip, null, whileOn, false, false);
    }

    public static Ability passive(String id, Stage stage, Hooks hooks) {
        return new Ability(id, stage, Kind.PASSIVE, 0.0, 0, "", null, hooks, false, false);
    }

    public static Ability transformation(String id, double costPerSecond, String clip, Hooks whileOn) {
        return new Ability(id, Stage.TRANSCENDENT, Kind.TRANSFORMATION, costPerSecond, 100, clip, null, whileOn, false, false);
    }

    /** An innate ability: part of the Aspect from the start, with no key of its own. */
    public static Ability innate(String id, Hooks hooks) {
        return new Ability(id, Stage.DORMANT, Kind.PASSIVE, 0.0, 0, "", null, hooks, true, false);
    }

    /** The same ability, marked as one the novel never shows. */
    public Ability filled() {
        return new Ability(this.id, this.stage, this.kind, this.cost, this.cooldown, this.clip, this.use, this.hooks, this.innate, true);
    }

    public boolean held() {
        return this.kind == Kind.TOGGLE || this.kind == Kind.TRANSFORMATION;
    }

    public Component title() {
        return Component.translatable("ability.nightmare_evolved." + this.id);
    }

    public Component description() {
        return Component.translatable("ability.nightmare_evolved." + this.id + ".desc");
    }
}
