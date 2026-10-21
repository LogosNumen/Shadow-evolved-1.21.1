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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * Aspects of Clan Valor's Saints and its vassal houses: Helie the centaur huntress, Tristan of Aegis Rose, the Summer
 * Knight, Tamar, Roan of White Feather, and Sky Tide. The novel names none of these Aspects.
 */
public final class Valor {
    /** Helie's: a huntress's bow that grows with her, and a centaur's body. */
    public static final Aspect HUNTRESS_BOW = Aspect.of("huntress_bow", Tier.TRANSCENDENT, Vfx.Palette.EARTH)
        .with(
            Ability.passive("steady_draw", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    return source.getDirectEntity() instanceof Projectile ? amount * 1.25F : amount;
                }
            }).filled(),
            Ability.active("volley", Stage.AWAKENED, 12.0, 160, "point", Valor::volley).filled(),
            Ability.transformation("graceful_centaur", 4.0, "transform_burst", Forms.of(new Forms.Shape(1.6, 16.0, 3.0, 0.4, 4.0, false),
                new Ability.Hooks() {
                    @Override
                    public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                        return source.getDirectEntity() instanceof Projectile ? amount * 1.5F : amount;
                    }
                }))
        )
        .flaw("still_waters")
        .build();

    /** Tristan's: the novel shows only his temper. Here, a shield of the Aegis Rose and the thorns on it. */
    public static final Aspect ROSE_AEGIS = Aspect.of("rose_aegis", Tier.AWAKENED, Vfx.Palette.BLOOD)
        .with(
            Ability.active("aegis", Stage.DORMANT, 8.0, 200, "guard", ctx -> {
                Kit.buff(ctx.player(), MobEffects.ABSORPTION, 10, Math.min(4, (int)ctx.power()));
                Vfx.dome(ctx.level(), ctx.player().position(), 1.8, Vfx.Palette.BLOOD, 40);
                return true;
            }).filled(),
            Ability.passive("thorned_guard", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != ctx.player()) {
                        Targets.hit(ctx.level(), ctx.player(), attacker, amount * 0.3F);
                    }

                    return amount;
                }
            }).filled()
        )
        .flaw("hot_blood")
        .build();

    /** The Summer Knight's: rivers and lakes rise as weapons in his hands, and as a Saint his body becomes light. */
    public static final Aspect SUMMER_KNIGHT = Aspect.of("summer_knight", Tier.TRANSCENDENT, Vfx.Palette.LIGHT)
        .with(
            Ability.active("water_whips", Stage.DORMANT, 10.0, 100, "slash", Valor::whips),
            Ability.toggle("tide_mantle", Stage.AWAKENED, 1.5, "guard", new Ability.Hooks() {
                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return ctx.player().isInWaterRainOrBubble() ? amount * 0.7F : amount * 0.85F;
                }
            }).filled(),
            Ability.transformation("body_of_light", 4.0, "transform_burst", Forms.of(new Forms.Shape(1.0, 0.0, 3.0, 0.1, 0.0, false),
                new Ability.Hooks() {
                    @Override
                    public float taken(Ctx ctx, DamageSource source, float amount) {
                        return source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.IS_FIRE) ? amount : 0.0F;
                    }
                }))
        )
        .fittingFlaws("binding_word", "still_waters")
        .build();

    /** Tamar's: she steps on the air itself, and moves in bursts too fast to see. */
    public static final Aspect STEPPING_AIR = Aspect.of("stepping_air", Tier.ASCENDED, Vfx.Palette.FROST)
        .with(
            Ability.toggle("air_step", Stage.DORMANT, 0.5, "rise", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    var player = ctx.player();
                    if (!player.onGround() && !player.isShiftKeyDown() && player.getDeltaMovement().y < 0.0) {
                        player.setDeltaMovement(player.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                        player.hurtMarked = true;
                        player.fallDistance = 0.0F;
                        if (age % 4 == 0) {
                            Vfx.ring(ctx.level(), player.position(), 0.6, Vfx.Palette.FROST, 8);
                        }
                    }
                }
            }),
            Ability.active("flash_step", Stage.AWAKENED, 6.0, 50, "dash", ctx -> {
                Vec3 to = Kit.clearSpotAhead(ctx.player(), 10.0);
                if (to == null) {
                    return false;
                }

                Kit.blink(ctx, to);
                Kit.sound(ctx.level(), to, SoundEvents.BREEZE_WIND_CHARGE_BURST, 0.6F, 1.8F);
                return true;
            })
        )
        .fittingFlaws("rash_heart", "hot_blood")
        .build();

    /** Roan's: golden lightning in arcs, bolts and walls, and as a Saint a winged white lion. */
    public static final Aspect GOLDEN_THUNDER = Aspect.of("golden_thunder", Tier.TRANSCENDENT, Vfx.Palette.LIGHT)
        .with(
            Ability.toggle("golden_arcs", Stage.DORMANT, 1.5, "channel_self", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 == 0) {
                        for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 3.5)) {
                            Targets.hit(ctx.level(), ctx.player(), foe, (float)(2.0 * ctx.power()));
                            Vfx.beam(ctx.level(), ctx.player().getBoundingBox().getCenter(), foe.getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 0.4);
                        }
                    }
                }
            }),
            Ability.active("thunderbolt", Stage.AWAKENED, 12.0, 100, "point", ctx -> {
                Vec3 at = Targets.lookedBlock(ctx.player(), 32.0, ClipContext.Fluid.NONE).getLocation();
                strike(ctx.level(), at);
                Kit.nova(ctx, at, 3.0, (float)(7.0 * ctx.power()), false);
                return true;
            }),
            Ability.active("lightning_wall", Stage.ASCENDED, 15.0, 300, "cast_both", Valor::wall),
            Ability.transformation("winged_lion", 5.0, "transform_burst", Forms.of(new Forms.Shape(1.8, 18.0, 5.0, 0.2, 6.0, true)))
        )
        .fittingFlaws("hot_blood", "rash_heart")
        .build();

    /** Sky Tide's: the winds and the lightning, and as a Saint a great white bird of prey wreathed in storm. */
    public static final Aspect SKY_TIDE = Aspect.of("sky_tide", Tier.TRANSCENDENT, Vfx.Palette.FROST)
        .with(
            Ability.active("gale", Stage.DORMANT, 8.0, 80, "cast_both", ctx -> {
                for (LivingEntity foe : Kit.cone(ctx, 10.0, 40.0, (float)(3.0 * ctx.power()), false)) {
                    Kit.shove(foe, ctx.player().position(), 2.0, 0.6);
                }

                Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.BREEZE_SHOOT, 1.0F, 0.6F);
                return true;
            }),
            Ability.active("storm_strike", Stage.AWAKENED, 14.0, 140, "point", ctx -> {
                LivingEntity seen = Targets.looked(ctx.player(), 40.0);
                if (seen == null || !Targets.foe(seen, ctx.player())) {
                    return false;
                }

                strike(ctx.level(), seen.position());
                Targets.hit(ctx.level(), ctx.player(), seen, (float)(10.0 * ctx.power()));
                return true;
            }),
            Ability.transformation("thunderbird", 5.0, "transform_burst", Forms.of(new Forms.Shape(2.2, 20.0, 5.0, 0.2, 6.0, true),
                new Ability.Hooks() {
                    @Override
                    public void tick(Ctx ctx, int age) {
                        if (age % 40 == 0) {
                            for (LivingEntity foe : Targets.nearest(ctx.level(), ctx.player(), 12.0, 2)) {
                                Vfx.beam(ctx.level(), ctx.player().getBoundingBox().getCenter(), foe.getBoundingBox().getCenter(), Vfx.Palette.FROST, 0.3);
                                Targets.hit(ctx.level(), ctx.player(), foe, (float)(4.0 * ctx.power()));
                            }
                        }
                    }
                }))
        )
        .fittingFlaws("rash_heart", "still_waters")
        .build();

    private Valor() {
    }

    /** A bolt of lightning for show: the harm is the ability's own. */
    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(at.x, at.y, at.z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    /** Volley: a fan of arrows loosed at once. */
    private static boolean volley(Ctx ctx) {
        for (int i = -2; i <= 2; i++) {
            Arrow arrow = new Arrow(ctx.level(), ctx.player(), new ItemStack(Items.ARROW), null);
            arrow.shootFromRotation(ctx.player(), ctx.player().getXRot(), ctx.player().getYRot() + i * 6.0F, 0.0F, 3.0F, 1.0F);
            arrow.setBaseDamage(2.0 + ctx.power());
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            ctx.level().addFreshEntity(arrow);
        }

        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.ARROW_SHOOT, 1.0F, 0.8F);
        return true;
    }

    /** Water Whips: nearby water rises and lashes the foes in front, dragging them in; with no water, a lesser lash. */
    private static boolean whips(Ctx ctx) {
        boolean water = false;
        for (BlockPos pos : BlockPos.betweenClosed(ctx.player().blockPosition().offset(-8, -3, -8), ctx.player().blockPosition().offset(8, 3, 8))) {
            if (ctx.level().getFluidState(pos).is(Fluids.WATER)) {
                water = true;
                break;
            }
        }

        float damage = (float)(6.0 * ctx.power() * (water || ctx.player().isInWaterRainOrBubble() ? 1.0 : 0.5));
        for (LivingEntity foe : Kit.cone(ctx, 10.0, 45.0, damage, false)) {
            Kit.shove(foe, ctx.player().position(), -0.8, 0.3);
        }

        Vfx.slash(ctx.level(), ctx.player(), 4.0, Vfx.Palette.FROST, 160.0);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.0F, 0.8F);
        return true;
    }

    /** Lightning Wall: a wall of crackling light stands across his front for six seconds, burning whatever crosses it. */
    private static boolean wall(Ctx ctx) {
        Vec3 look = ctx.player().getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
        Vec3 centre = ctx.player().position().add(look.scale(4.0));
        Vec3 across = new Vec3(-look.z, 0.0, look.x);
        Vfx.over(120, tick -> {
            if (tick % 10 == 0) {
                for (int i = -4; i <= 4; i++) {
                    Vfx.pillar(ctx.level(), centre.add(across.scale(i)), 2.5, Vfx.Palette.LIGHT);
                }

                for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), centre, 5.0)) {
                    Vec3 rel = foe.position().subtract(centre);
                    if (Math.abs(rel.dot(look)) < 1.2) {
                        foe.invulnerableTime = 0;
                        Targets.hit(ctx.level(), ctx.player(), foe, (float)(3.0 * ctx.power()));
                    }
                }
            }
        });
        Kit.sound(ctx.level(), centre, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.6F, 1.6F);
        return true;
    }
}
