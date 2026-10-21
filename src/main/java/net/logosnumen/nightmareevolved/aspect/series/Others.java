package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.phys.Vec3;

/**
 * The remaining Aspects the novel shows: Rain's, Naeve's of the House of Night, the Bright Lord Gunlaug's, Shakti's of
 * the Fire Keepers, and Noctis's of the old Kingdom of Hope. None is named in the novel.
 */
public final class Others {
    /**
     * Rain's: the novel shows her Aspect waking but not what it does; it is known that she cannot kill. Here, a gift
     * that binds and calls the storm, as befits Promise of a Distant Sky.
     */
    public static final Aspect DISTANT_SKY = Aspect.of("distant_sky", Tier.AWAKENED, Vfx.Palette.FROST)
        .with(
            Ability.active("name_of_binding", Stage.DORMANT, 8.0, 120, "write", ctx -> {
                LivingEntity seen = Targets.looked(ctx.player(), 20.0);
                if (seen == null || !Targets.foe(seen, ctx.player())) {
                    return false;
                }

                Kit.quiet(seen, MobEffects.MOVEMENT_SLOWDOWN, 80, 6);
                Kit.quiet(seen, MobEffects.WEAKNESS, 80, 2);
                Vfx.glyphs(ctx.level(), seen.getBoundingBox().getCenter(), 0.8, Vfx.Palette.FROST, 8);
                return true;
            }).filled(),
            Ability.active("distant_storm", Stage.AWAKENED, 14.0, 200, "raise", ctx -> {
                Vec3 at = Targets.lookedBlock(ctx.player(), 32.0, ClipContext.Fluid.NONE).getLocation();
                for (LivingEntity foe : Kit.nova(ctx, at, 5.0, (float)(4.0 * ctx.power()), false)) {
                    Kit.quiet(foe, MobEffects.MOVEMENT_SLOWDOWN, 100, 2);
                    Kit.quiet(foe, MobEffects.BLINDNESS, 60, 0);
                }

                Vfx.dome(ctx.level(), at, 5.0, Vfx.Palette.FROST, 60);
                Kit.sound(ctx.level(), at, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8F, 1.2F);
                return true;
            }).filled()
        )
        .flaw("crown_of_thorns")
        .build();

    /** Naeve's: he belongs to the water, swift in it and untouchable as liquid within it. */
    public static final Aspect DEEP_CURRENT = Aspect.of("deep_current", Tier.AWAKENED, Vfx.Palette.FROST)
        .with(
            Ability.passive("tidecaller", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.OXYGEN_BONUS, 8.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0, AttributeModifier.Operation.ADD_VALUE);
                }

                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 == 0 && ctx.player().isInWater()) {
                        Kit.quiet(ctx.player(), MobEffects.DOLPHINS_GRACE, 40, 0);
                    }
                }
            }),
            Ability.passive("liquid_body", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return ctx.player().isInWater() && !source.is(DamageTypeTags.BYPASSES_ARMOR) ? 0.0F : amount;
                }
            })
        )
        .flaw("landbound")
        .build();

    /** Gunlaug's: master of the water, whose golden armour bowed the will of all who saw themselves in it. */
    public static final Aspect BRIGHT_LORD = Aspect.of("bright_lord", Tier.ASCENDED, Vfx.Palette.LIGHT)
        .with(
            Ability.passive("deep_breath", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.OXYGEN_BONUS, 6.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.8, AttributeModifier.Operation.ADD_VALUE);
                }
            }),
            Ability.active("golden_mantle", Stage.AWAKENED, 15.0, 300, "raise", ctx -> {
                int cowed = 0;
                for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 10.0)) {
                    if (foe instanceof Mob mob && mob.hasLineOfSight(ctx.player())) {
                        mob.setTarget(null);
                        Kit.quiet(mob, MobEffects.WEAKNESS, 120, 1);
                        Kit.quiet(mob, MobEffects.MOVEMENT_SLOWDOWN, 120, 1);
                        cowed++;
                    }
                }

                Vfx.flash(ctx.level(), ctx.player().getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 2.0F);
                return cowed > 0;
            }).filled()
        )
        .fittingFlaws("landbound", "forsaken_treasures")
        .build();

    /** Shakti's: wherever she goes, plants thrive. */
    public static final Aspect VERDANT_TOUCH = Aspect.of("verdant_touch", Tier.DORMANT, Vfx.Palette.ACID)
        .with(
            Ability.active("bloom", Stage.DORMANT, 6.0, 80, "raise", ctx -> {
                int grown = 0;
                for (BlockPos pos : BlockPos.betweenClosed(ctx.player().blockPosition().offset(-4, -1, -4), ctx.player().blockPosition().offset(4, 1, 4))) {
                    if (ctx.level().getBlockState(pos).getBlock() instanceof BonemealableBlock
                        && BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), ctx.level(), pos.immutable(), ctx.player())) {
                        grown++;
                    }
                }

                Vfx.ring(ctx.level(), ctx.player().position().add(0.0, 0.1, 0.0), 4.0, Vfx.Palette.ACID, 32);
                return grown > 0;
            }),
            Ability.active("binding_vines", Stage.AWAKENED, 10.0, 160, "slam", ctx -> {
                for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 5.0)) {
                    Kit.quiet(foe, MobEffects.MOVEMENT_SLOWDOWN, 80, 3);
                    Afflictions.afflict(foe, ctx.player(), Afflictions.BLEED, 0.5 * ctx.power(), 4);
                }

                Vfx.shockwave(ctx.level(), ctx.player().position(), 5.0, Vfx.Palette.ACID);
                return true;
            }).filled()
        )
        .fittingFlaws("slow_well", "overlooked")
        .build();

    /** Noctis's: a presence of true divinity that becomes a torrent of light and a colossal wolf woven of moonlight. */
    public static final Aspect MOONLIGHT = Aspect.of("moonlight", Tier.TRANSCENDENT, Vfx.Palette.SOUL)
        .with(
            Ability.toggle("moonlit_veil", Stage.DORMANT, 1.0, "channel_self", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 == 0) {
                        Kit.quiet(ctx.player(), MobEffects.NIGHT_VISION, 260, 0);
                        if (!ctx.level().isDay()) {
                            Kit.quiet(ctx.player(), MobEffects.REGENERATION, 40, 0);
                        }
                    }
                }
            }).filled(),
            Ability.active("lunar_lance", Stage.AWAKENED, 12.0, 120, "point", ctx -> {
                Kit.bolt(ctx, 20.0, 1.0, (float)(7.0 * ctx.power()), true);
                return true;
            }).filled(),
            Ability.transformation("moonlight_wolf", 5.0, "transform_burst", Forms.of(new Forms.Shape(2.5, 20.0, 6.0, 0.3, 6.0, false)))
        )
        .fittingFlaws("lightless", "still_waters")
        .build();

    private Others() {
    }
}
