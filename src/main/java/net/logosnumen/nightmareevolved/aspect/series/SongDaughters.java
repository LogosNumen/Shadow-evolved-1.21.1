package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Summons;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.entity.soul.SoulConstructEntity;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/**
 * Aspects of the daughters Ki Song took in: Seishan, Revel the Lightslayer, Beastmaster and Silent Stalker. The novel
 * names none of these Aspects, so each is called by its bearer or what it does.
 */
public final class SongDaughters {
    /** Seishan's: a monster's body, curses that open veins, and a strength that drinks the blood around it. */
    public static final Aspect CRIMSON_HUNGER = Aspect.of("crimson_hunger", Tier.TRANSCENDENT, Vfx.Palette.BLOOD)
        .with(
            Ability.toggle("monster_form", Stage.DORMANT, 1.5, "transform_burst", new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.ATTACK_DAMAGE, 3.0 * ctx.power(), AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.MOVEMENT_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                    out.add(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.SCALE, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                }

                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 10 == 0) {
                        Vfx.burst(ctx.level(), ctx.player().getEyePosition(), Vfx.Palette.BLOOD, 2, 0.05);
                    }
                }
            }),
            Ability.active("bleeding_curse", Stage.AWAKENED, 12.0, 160, "cast_both", ctx -> {
                var foes = Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 10.0);
                foes.forEach(foe -> Afflictions.afflict(foe, ctx.player(), Afflictions.BLEED, 1.5 * ctx.power(), 6));
                Vfx.shockwave(ctx.level(), ctx.player().position(), 10.0, Vfx.Palette.BLOOD);
                return !foes.isEmpty();
            }),
            Ability.passive("blood_frenzy", Stage.ASCENDED, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    long bleeding = Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 12.0).stream()
                        .filter(e -> Afflictions.has(e, Afflictions.BLEED)).count();
                    return amount * (1.0F + Math.min(0.5F, 0.05F * bleeding));
                }
            }),
            Ability.transformation("monstrous_kin", 5.0, "transform_burst", Forms.of(new Forms.Shape(1.3, 12.0, 4.0, 0.15, 4.0, false),
                new Ability.Hooks() {
                    @Override
                    public void tick(Ctx ctx, int age) {
                        if (age % 20 == 0) {
                            for (LivingEntity ally : Targets.alliesNear(ctx.level(), ctx.player(), ctx.player().position(), 16.0)) {
                                if (ally != ctx.player()) {
                                    Kit.quiet(ally, MobEffects.DAMAGE_BOOST, 40, 1);
                                    Kit.quiet(ally, MobEffects.DAMAGE_RESISTANCE, 40, 0);
                                }
                            }
                        }
                    }
                }))
        )
        .flaw("bloodthirst")
        .build();

    /** Revel's: elemental darkness that floods wherever she is, carries her, and makes her strong. */
    public static final Aspect LIGHTSLAYER = Aspect.of("lightslayer", Tier.TRANSCENDENT, Vfx.Palette.SHADOW)
        .with(
            Ability.active("flood_of_darkness", Stage.DORMANT, 10.0, 200, "raise", ctx -> {
                for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 10.0)) {
                    Kit.quiet(foe, MobEffects.BLINDNESS, 120, 0);
                    Kit.quiet(foe, MobEffects.DARKNESS, 120, 0);
                    if (foe instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                }

                Kit.quiet(ctx.player(), MobEffects.NIGHT_VISION, 240, 0);
                Vfx.dome(ctx.level(), ctx.player().position(), 10.0, Vfx.Palette.SHADOW, 90);
                return true;
            }),
            Ability.active("dark_tide", Stage.AWAKENED, 8.0, 60, "dash", ctx -> {
                Vec3 to = Kit.clearSpotAhead(ctx.player(), 20.0);
                if (to == null) {
                    return false;
                }

                Vfx.smoke(ctx.level(), ctx.player().position().add(0.0, 0.3, 0.0), 0x050508, 20, 0.8);
                Kit.blink(ctx, to);
                return true;
            }),
            Ability.passive("darkborn_strength", Stage.ASCENDED, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    return Kit.shadowed(ctx.level(), BlockPos.containing(ctx.player().getEyePosition())) ? amount * 1.4F : amount;
                }

                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return Kit.shadowed(ctx.level(), BlockPos.containing(ctx.player().getEyePosition())) ? amount * 0.8F : amount;
                }
            }),
            Ability.transformation("lightslayer_form", 5.0, "transform_burst", Forms.of(new Forms.Shape(2.0, 16.0, 5.0, 0.1, 6.0, true)))
        )
        .flaw("lightless")
        .build();

    /** Beastmaster's: hexes that sap the will, senses misled, and beasts brought to heel. */
    public static final Aspect BEASTMASTER = Aspect.of("beastmaster", Tier.TRANSCENDENT, Vfx.Palette.DRAGON)
        .with(
            Ability.active("hex", Stage.DORMANT, 6.0, 80, "point", ctx -> {
                LivingEntity seen = Targets.looked(ctx.player(), 20.0);
                if (seen == null || !Targets.foe(seen, ctx.player())) {
                    return false;
                }

                Kit.quiet(seen, MobEffects.MOVEMENT_SLOWDOWN, 160, 1);
                Kit.quiet(seen, MobEffects.WEAKNESS, 160, 1);
                Kit.quiet(seen, MobEffects.DIG_SLOWDOWN, 160, 1);
                Vfx.glyphs(ctx.level(), seen.getEyePosition(), 0.7, Vfx.Palette.DRAGON, 6);
                return true;
            }),
            Ability.active("phantoms", Stage.AWAKENED, 12.0, 200, "cast", ctx -> {
                for (int i = 0; i < 3; i++) {
                    Summons.construct(ctx, SoulConstructEntity.Look.LIGHT, Summons.around(ctx, i, 3, 3.0), 1.0, 0.0, 200, "illusion");
                }

                for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 16.0)) {
                    if (foe instanceof Mob mob && mob.getTarget() == ctx.player()) {
                        mob.setTarget(null);
                    }
                }

                return true;
            }),
            Ability.active("beast_taming", Stage.ASCENDED, 25.0, 600, "raise_one", ctx -> {
                LivingEntity seen = Targets.looked(ctx.player(), 16.0);
                if (!(seen instanceof Mob mob) || mob.getMaxHealth() > 80.0F * ctx.power()) {
                    return false;
                }

                Bound.bind(mob, ctx.player(), 6000, "tamed", false);
                Vfx.glyphs(ctx.level(), mob.getEyePosition(), 0.8, Vfx.Palette.DRAGON, 8);
                Kit.sound(ctx.level(), mob.position(), SoundEvents.WOLF_AMBIENT, 1.0F, 0.6F);
                return true;
            }),
            Ability.transformation("ruinous_beauty", 3.0, "transform_gather", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 != 0) {
                        return;
                    }

                    for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 12.0)) {
                        if (foe instanceof Mob mob && mob.hasLineOfSight(ctx.player())) {
                            mob.setTarget(null);
                            Kit.quiet(mob, MobEffects.MOVEMENT_SLOWDOWN, 40, 2);
                        }
                    }

                    Vfx.aura(ctx.level(), ctx.player(), Vfx.Palette.DRAGON, 6);
                }
            })
        )
        .fittingFlaws("still_waters", "crown_of_thorns")
        .build();

    /** Silent Stalker's: a hunter's eye that always knows where to aim, and a panther's body. */
    public static final Aspect SILENT_STALKER = Aspect.of("silent_stalker", Tier.TRANSCENDENT, Vfx.Palette.SHADOW)
        .with(
            Ability.passive("true_aim", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    return source.getDirectEntity() instanceof Projectile ? amount * 1.3F * (ctx.player().getRandom().nextFloat() < 0.2F ? 1.5F : 1.0F)
                        : amount;
                }
            }),
            Ability.toggle("unseen_hunter", Stage.AWAKENED, 1.0, "guard", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 10 == 0 && ctx.player().isShiftKeyDown()) {
                        Kit.quiet(ctx.player(), MobEffects.INVISIBILITY, 20, 0);
                        for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 24.0)) {
                            if (foe instanceof Mob mob && mob.getTarget() == ctx.player() && mob.distanceToSqr(ctx.player()) > 16.0) {
                                mob.setTarget(null);
                            }
                        }
                    }
                }
            }).filled(),
            Ability.transformation("black_panther", 4.0, "transform_burst", Forms.of(new Forms.Shape(1.3, 12.0, 6.0, 0.35, 4.0, false)))
        )
        .fittingFlaws("overlooked", "still_waters")
        .build();

    private SongDaughters() {
    }
}
