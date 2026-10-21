package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
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

/**
 * Jet's Aspect, Soul Reaper, of the Supreme Rank. Her soul is dead and her core broken, so she lives on what she reaps:
 * every kill fills her, her strikes pass through flesh to the soul, broken cores feed her own, and her body takes more
 * essence than any living body could. As a Saint she becomes a wraith of mist that nothing physical can touch. Her
 * Aspect Legacy is a soulbound scythe of chilling mist.
 */
public final class SoulReaper {
    public static final Aspect ASPECT = Aspect.of("soul_reaper", Tier.SUPREME, Vfx.Palette.SOUL)
        .with(
            Ability.passive("reap_essence", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public void killed(Ctx ctx, LivingEntity victim) {
                    ctx.soul().setEssence(ctx.soul().essence() + ctx.soul().maxEssence() * 0.15);
                    Vfx.trail(ctx.level(), victim.position(), ctx.player().position(), Vfx.Palette.SOUL);
                }
            }),
            Ability.toggle("soul_strike", Stage.AWAKENED, 1.0, "slash", new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    if (source.getDirectEntity() == ctx.player()) {
                        Vfx.glyph(ctx.level(), target.getBoundingBox().getCenter(), Vfx.Palette.SOUL.bright);
                        return amount * (1.0F + target.getArmorValue() * 0.04F) + (float)ctx.power();
                    }

                    return amount;
                }
            }),
            Ability.passive("core_reaping", Stage.ASCENDED, new Ability.Hooks() {
                @Override
                public void killed(Ctx ctx, LivingEntity victim) {
                    Soul.feed(ctx.player(), Summons.fragmentsFor(victim, ctx.soul().rank().ladder));
                }

                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 == 0 && ctx.soul().essence() > ctx.soul().maxEssence() * 0.7) {
                        Kit.quiet(ctx.player(), MobEffects.DAMAGE_BOOST, 40, 1);
                        Kit.quiet(ctx.player(), MobEffects.MOVEMENT_SPEED, 40, 1);
                        Kit.quiet(ctx.player(), MobEffects.DAMAGE_RESISTANCE, 40, 0);
                    }
                }
            }),
            Ability.transformation("mist_wraith", 4.0, "transform_gather", Forms.of(new Forms.Shape(1.0, 0.0, 2.0, 0.2, 0.0, false),
                new Ability.Hooks() {
                    @Override
                    public float taken(Ctx ctx, DamageSource source, float amount) {
                        boolean physical = !source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.IS_FIRE);
                        return physical ? 0.0F : amount;
                    }

                    @Override
                    public void tick(Ctx ctx, int age) {
                        if (age % 4 == 0) {
                            Vfx.smoke(ctx.level(), ctx.player().getBoundingBox().getCenter(), 0xC8E8F0, 4, 0.6);
                        }

                        Kit.quiet(ctx.player(), MobEffects.SLOW_FALLING, 10, 0);
                    }
                })),
            Ability.active("soul_scythe", Stage.SUPREME, 30.0, 300, "spin", SoulReaper::scythe)
        )
        .flaw("deathly_core")
        .build();

    private SoulReaper() {
    }

    /** Soul Scythe: her Legacy's scythe of mist sweeps all around her, reaping souls rather than flesh. */
    private static boolean scythe(Ctx ctx) {
        Vfx.slash(ctx.level(), ctx.player(), 4.5, Vfx.Palette.SOUL, 360.0);
        for (LivingEntity target : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 6.0)) {
            Targets.soulHit(ctx.level(), ctx.player(), target, (float)(14.0 * ctx.power()));
        }

        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.5F);
        return true;
    }
}
