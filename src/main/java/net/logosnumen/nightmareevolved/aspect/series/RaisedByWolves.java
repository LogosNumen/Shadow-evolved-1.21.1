package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Effie's Aspect, Raised by Wolves, of the Transcendent Rank: a warrior's body above all others. It makes her stronger
 * and swifter in every way, turns her skin to steel, lends her might to every ally who can see her, and as a Saint
 * makes her a giantess of steel. Her soldiers' determination is what refills her.
 */
public final class RaisedByWolves {
    public static final Aspect ASPECT = Aspect.of("raised_by_wolves", Tier.TRANSCENDENT, Vfx.Palette.EARTH)
        .with(
            Ability.passive("war_body", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.ATTACK_DAMAGE, 2.0 * ctx.power(), AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.MAX_HEALTH, 4.0 * ctx.power(), AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                    out.add(Attributes.JUMP_STRENGTH, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                    out.add(Attributes.ATTACK_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                }
            }),
            Ability.toggle("skin_of_steel", Stage.AWAKENED, 1.5, "guard", new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.ARMOR, 10.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.ARMOR_TOUGHNESS, 6.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.KNOCKBACK_RESISTANCE, 0.8, AttributeModifier.Operation.ADD_VALUE);
                }

                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return amount * 0.6F;
                }

                @Override
                public void start(Ctx ctx) {
                    Vfx.burst(ctx.level(), ctx.player().getBoundingBox().getCenter(), Vfx.Palette.STEEL, 16, 0.15);
                    Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.ARMOR_EQUIP_NETHERITE, 1.0F, 0.7F);
                }
            }),
            Ability.toggle("shared_might", Stage.ASCENDED, 2.0, "raise", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 != 0) {
                        return;
                    }

                    for (LivingEntity ally : Targets.alliesNear(ctx.level(), ctx.player(), ctx.player().position(), 24.0)) {
                        if (ally != ctx.player() && ally.hasLineOfSight(ctx.player())) {
                            Kit.quiet(ally, MobEffects.DAMAGE_BOOST, 40, 0);
                            Kit.quiet(ally, MobEffects.MOVEMENT_SPEED, 40, 0);
                            Kit.quiet(ally, MobEffects.DAMAGE_RESISTANCE, 40, 0);
                            Vfx.embers(ctx.level(), ally.getBoundingBox().getCenter(), Vfx.Palette.EARTH, 2, 0.4);
                        }
                    }
                }
            }),
            Ability.transformation("war_giantess", 6.0, "transform_burst", Forms.of(new Forms.Shape(5.0, 40.0, 8.0, 0.15, 10.0, false),
                new Ability.Hooks() {
                    @Override
                    public void start(Ctx ctx) {
                        Vfx.shockwave(ctx.level(), ctx.player().position(), 10.0, Vfx.Palette.EARTH);
                        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.RAID_HORN, 2.0F, 0.8F);
                    }
                })),
            Ability.passive("unbroken_host", Stage.SUPREME, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 == 0) {
                        int allies = Targets.alliesNear(ctx.level(), ctx.player(), ctx.player().position(), 24.0).size() - 1;
                        if (allies > 0) {
                            ctx.soul().setEssence(ctx.soul().essence() + ctx.soul().maxEssence() * 0.01 * Math.min(10, allies));
                        }
                    }
                }
            })
        )
        .flaw("ravenous_body")
        .build();

    private RaisedByWolves() {
    }
}
