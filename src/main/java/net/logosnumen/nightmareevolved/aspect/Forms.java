package net.logosnumen.nightmareevolved.aspect;

import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The bodies Saints take when they transform: a dragon, a thunderbird, a winged lion, a centaur, a giantess. Each
 * changes size and strength, may fly, and wears its Aspect's light. Clients draw the creature itself in place of the
 * Saint's body (see {@code client.form.FormBodies}).
 */
public final class Forms {
    private Forms() {
    }

    /** What a form changes: size (1 is unchanged), extra health, extra damage (scaled by power), speed, and flight. */
    public record Shape(double scale, double health, double damage, double speed, double armour, boolean flight) {
    }

    public static Ability.Hooks of(Shape shape) {
        return of(shape, Ability.Hooks.NONE);
    }

    /** A form, plus whatever else the Transformation does while worn. */
    public static Ability.Hooks of(Shape shape, Ability.Hooks extra) {
        return new Ability.Hooks() {
            @Override
            public void modifiers(Ctx ctx, Ability.Modifiers out) {
                out.add(Attributes.SCALE, shape.scale() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                out.add(Attributes.MAX_HEALTH, shape.health() * ctx.power(), AttributeModifier.Operation.ADD_VALUE);
                out.add(Attributes.ATTACK_DAMAGE, shape.damage() * ctx.power(), AttributeModifier.Operation.ADD_VALUE);
                out.add(Attributes.MOVEMENT_SPEED, shape.speed(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                out.add(Attributes.ARMOR, shape.armour(), AttributeModifier.Operation.ADD_VALUE);
                if (shape.scale() > 1.0) {
                    out.add(Attributes.ENTITY_INTERACTION_RANGE, shape.scale() - 1.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.BLOCK_INTERACTION_RANGE, shape.scale() - 1.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.STEP_HEIGHT, (shape.scale() - 1.0) * 0.5, AttributeModifier.Operation.ADD_VALUE);
                }

                extra.modifiers(ctx, out);
            }

            @Override
            public boolean flight(Ctx ctx) {
                return shape.flight() || extra.flight(ctx);
            }

            @Override
            public void tick(Ctx ctx, int age) {
                // The form's own body carries its wings now; the Aspect's light still clings to it.
                if (age % 10 == 0) {
                    Vfx.aura(ctx.level(), ctx.player(), ctx.palette(), 6);
                }

                extra.tick(ctx, age);
            }

            @Override
            public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                return extra.dealt(ctx, target, source, amount);
            }

            @Override
            public float taken(Ctx ctx, DamageSource source, float amount) {
                return extra.taken(ctx, source, amount);
            }

            @Override
            public void killed(Ctx ctx, LivingEntity victim) {
                extra.killed(ctx, victim);
            }

            @Override
            public void start(Ctx ctx) {
                Vfx.flash(ctx.level(), ctx.player().getBoundingBox().getCenter(), ctx.palette(), (float)(1.5 * shape.scale()));
                Vfx.burst(ctx.level(), ctx.player().getBoundingBox().getCenter(), ctx.palette(), 30, 0.3);
                Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.6F);
                extra.start(ctx);
            }

            @Override
            public void stop(Ctx ctx) {
                Vfx.smoke(ctx.level(), ctx.player().getBoundingBox().getCenter(), ctx.palette().deep, 16, 0.8);
                extra.stop(ctx);
            }
        };
    }
}
