package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Summons;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/**
 * Nephis's Aspect, [Light Bringer], of the Divine Rank: a creature of light banished into darkness. Its white flame
 * heals and destroys, all fire bends to it, and its soul cannot be corrupted. A Saint who bears it becomes a winged
 * spirit of light.
 */
public final class LightBringer {
    public static final Aspect ASPECT = Aspect.of("light_bringer", Tier.DIVINE, Vfx.Palette.LIGHT)
        .with(
            Ability.innate("halfbreed", new Ability.Hooks() {
                @Override
                public void killed(Ctx ctx, LivingEntity victim) {
                    if (victim.isOnFire() || Afflictions.has(victim, Afflictions.SOUL_FIRE)) {
                        ctx.soul().setEssence(ctx.soul().essence() + ctx.soul().maxEssence() * 0.08);
                        Soul.feed(ctx.player(), Summons.fragmentsFor(victim, ctx.soul().rank().ladder) / 4);
                        Vfx.trail(ctx.level(), victim.position(), ctx.player().position(), Vfx.Palette.LIGHT);
                    }
                }
            }),
            Ability.active("soul_flame", Stage.DORMANT, 8.0, 60, "cast", LightBringer::soulFlame),
            Ability.active("flame_manipulation", Stage.AWAKENED, 12.0, 80, "cast_both", LightBringer::torrent, new Ability.Hooks() {
                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return source.is(DamageTypeTags.IS_FIRE) ? 0.0F : amount;
                }

                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.BURNING_TIME, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                }
            }),
            Ability.passive("longing", Stage.ASCENDED, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 10 == 0) {
                        ctx.player().removeEffect(MobEffects.WITHER);
                        ctx.player().removeEffect(MobEffects.POISON);
                        ctx.player().removeEffect(MobEffects.CONFUSION);
                        ctx.player().removeEffect(MobEffects.HUNGER);
                    }
                }
            }),
            Ability.transformation("radiant_form", 4.0, "transform_burst", new Ability.Hooks() {
                @Override
                public boolean flight(Ctx ctx) {
                    return true;
                }

                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 3 == 0) {
                        Vfx.wings(ctx.level(), ctx.player(), Vfx.Palette.LIGHT, 1.0, age * 0.15);
                    }

                    if (age % 20 == 0 && ctx.player().getHealth() < ctx.player().getMaxHealth() && ctx.soul().spend(1.5)) {
                        ctx.player().heal(1.0F + (float)ctx.power() * 0.5F);
                    }
                }

                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    target.igniteForSeconds(4.0F);
                    return amount + 3.0F * (float)ctx.power();
                }

                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return source.is(DamageTypeTags.IS_FIRE) ? 0.0F : amount;
                }
            }),
            Ability.active("dawn_star", Stage.SUPREME, 80.0, 1200, "raise", LightBringer::dawnStar).filled()
        )
        .flaw("pristine_soul")
        .signature("dreamspawn")
        .build();

    private LightBringer() {
    }

    /** Soul Flame: on a foe, white fire that burns the soul itself; on oneself or an ally (or when crouched), healing. */
    private static boolean soulFlame(Ctx ctx) {
        LivingEntity looked = Targets.looked(ctx.player(), 16.0);
        if (looked != null && Targets.foe(looked, ctx.player()) && !ctx.player().isShiftKeyDown()) {
            Targets.soulHit(ctx.level(), ctx.player(), looked, (float)(4.0 * ctx.power()));
            Afflictions.afflict(looked, ctx.player(), Afflictions.SOUL_FIRE, 1.5 * ctx.power(), 4);
            Vfx.beam(ctx.level(), ctx.player().getEyePosition(), looked.getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 0.3);
            Kit.sound(ctx.level(), looked.position(), SoundEvents.BLAZE_SHOOT, 0.7F, 1.4F);
            return true;
        }

        LivingEntity patient = looked != null && Targets.ally(looked, ctx.player()) ? looked : ctx.player();
        Kit.heal(ctx.level(), patient, (float)(4.0 * ctx.power()), Vfx.Palette.LIGHT);
        Kit.buff(patient, MobEffects.REGENERATION, 4, 0);
        Kit.sound(ctx.level(), patient.position(), SoundEvents.BEACON_POWER_SELECT, 0.6F, 1.6F);
        return true;
    }

    /** Flame Manipulation: a torrent of white fire across everything in front. */
    private static boolean torrent(Ctx ctx) {
        for (LivingEntity target : Kit.cone(ctx, 8.0, 30.0, (float)(6.0 * ctx.power()), false)) {
            target.igniteForSeconds(5.0F);
        }

        Vfx.cone(ctx.level(), ctx.player().getEyePosition(), ctx.player().getLookAngle(), 8.0, 30.0, Vfx.Palette.FIRE, 40, true);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.FIRECHARGE_USE, 1.0F, 0.8F);
        return true;
    }

    /** Dawn Star: a white star falls where she looks and breaks over everything there. */
    private static boolean dawnStar(Ctx ctx) {
        Vec3 at = Targets.lookedBlock(ctx.player(), 40.0, ClipContext.Fluid.NONE).getLocation();
        Vfx.beam(ctx.level(), at.add(0.0, 30.0, 0.0), at, Vfx.Palette.LIGHT, 0.5);
        for (LivingEntity target : Kit.nova(ctx, at, 8.0, (float)(20.0 * ctx.power()), true)) {
            target.igniteForSeconds(8.0F);
        }

        Vfx.flash(ctx.level(), at.add(0.0, 1.0, 0.0), Vfx.Palette.LIGHT, 4.0F);
        Vfx.dome(ctx.level(), at, 6.0, Vfx.Palette.LIGHT, 80);
        Kit.sound(ctx.level(), at, SoundEvents.GENERIC_EXPLODE, 2.0F, 0.6F);
        return true;
    }
}
