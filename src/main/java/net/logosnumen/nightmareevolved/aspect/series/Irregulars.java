package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Aspects of the First Irregular Company, Sunny's cohort in Antarctica, and of the Sleeper who came to be called Lucky.
 * The novel names none of them, only what they do.
 */
public final class Irregulars {
    /** Kim's: a lesser gift of revelation that shows the weak points of living things, and shares the sight. */
    public static final Aspect FLAW_SIGHT = Aspect.of("flaw_sight", Tier.DORMANT, Vfx.Palette.RUNE)
        .with(
            Ability.active("weak_points", Stage.DORMANT, 4.0, 60, "point", ctx -> {
                LivingEntity seen = Targets.looked(ctx.player(), 24.0);
                if (seen == null || !Targets.foe(seen, ctx.player())) {
                    return false;
                }

                Afflictions.afflict(seen, ctx.player(), Afflictions.MARKED, 0.35, 10);
                Kit.quiet(seen, MobEffects.GLOWING, 200, 0);
                return true;
            }),
            Ability.active("shared_sight", Stage.AWAKENED, 10.0, 160, "raise_one", ctx -> {
                int marked = 0;
                for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 16.0)) {
                    if (foe.hasLineOfSight(ctx.player())) {
                        Afflictions.afflict(foe, ctx.player(), Afflictions.MARKED, 0.25, 8);
                        Kit.quiet(foe, MobEffects.GLOWING, 160, 0);
                        marked++;
                    }
                }

                return marked > 0;
            })
        )
        .flaw("overlooked")
        .build();

    /** Luster's: a soul healer's gift, quickening the essence of those around him and pouring his own into them. */
    public static final Aspect WELLSPRING = Aspect.of("wellspring", Tier.DORMANT, Vfx.Palette.SOUL)
        .with(
            Ability.toggle("essence_wellspring", Stage.DORMANT, 0.5, "channel_self", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 20 != 0) {
                        return;
                    }

                    for (ServerPlayer other : ctx.level().players()) {
                        if (other.distanceToSqr(ctx.player()) < 24.0 * 24.0) {
                            SoulData soul = Soul.of(other);
                            soul.setEssence(soul.essence() + soul.maxEssence() * (other == ctx.player() ? 0.01 : 0.02));
                            Vfx.mote(ctx.level(), other.getBoundingBox().getCenter(), Vfx.Palette.SOUL.bright, 0.05);
                        }
                    }
                }
            }),
            Ability.active("essence_transfer", Stage.AWAKENED, 20.0, 100, "cast", ctx -> {
                LivingEntity seen = Targets.looked(ctx.player(), 12.0);
                if (!(seen instanceof ServerPlayer other)) {
                    return false;
                }

                SoulData soul = Soul.of(other);
                soul.setEssence(soul.essence() + 20.0 * ctx.power());
                Vfx.beam(ctx.level(), ctx.player().getEyePosition(), other.getEyePosition(), Vfx.Palette.SOUL, 0.3);
                return true;
            })
        )
        .flaw("unspoken")
        .build();

    /** Samara's: essence driven into things, and turned into force or heat; her rounds become charges. */
    public static final Aspect CHARGED_ESSENCE = Aspect.of("charged_essence", Tier.AWAKENED, Vfx.Palette.FIRE)
        .with(
            Ability.toggle("charged_rounds", Stage.DORMANT, 1.5, "point", new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    if (source.getDirectEntity() instanceof Projectile) {
                        Kit.nova(ctx, target.getBoundingBox().getCenter(), 2.5, (float)(3.0 * ctx.power()), false);
                        Vfx.flash(ctx.level(), target.getBoundingBox().getCenter(), Vfx.Palette.FIRE, 1.2F);
                        Kit.sound(ctx.level(), target.position(), SoundEvents.GENERIC_EXPLODE, 0.6F, 1.6F);
                    }

                    return amount;
                }
            }),
            Ability.active("kinetic_burst", Stage.AWAKENED, 12.0, 120, "cast_both", ctx -> {
                Vec3 at = Targets.lookedBlock(ctx.player(), 24.0, ClipContext.Fluid.NONE).getLocation();
                for (LivingEntity foe : Kit.nova(ctx, at, 3.0, (float)(5.0 * ctx.power()), false)) {
                    foe.igniteForSeconds(3.0F);
                    Kit.shove(foe, at, 1.2, 0.5);
                }

                Vfx.flash(ctx.level(), at, Vfx.Palette.FIRE, 2.0F);
                Kit.sound(ctx.level(), at, SoundEvents.GENERIC_EXPLODE, 1.0F, 1.2F);
                return true;
            })
        )
        .flaw("slow_well")
        .build();

    /** Lucky Sleeper's: a craftsman's gift for mending, restoring and growing things; golden hands. */
    public static final Aspect GOLDEN_HANDS = Aspect.of("golden_hands", Tier.DORMANT, Vfx.Palette.LIGHT)
        .with(
            Ability.active("golden_touch", Stage.DORMANT, 6.0, 60, "channel_self", Irregulars::goldenTouch),
            Ability.active("restoration", Stage.AWAKENED, 10.0, 200, "raise", ctx -> {
                for (LivingEntity ally : Targets.alliesNear(ctx.level(), ctx.player(), ctx.player().position(), 6.0)) {
                    Kit.heal(ctx.level(), ally, (float)(2.0 * ctx.power()), Vfx.Palette.LIGHT);
                }

                return true;
            }).filled()
        )
        .flaw("forgotten_name")
        .build();

    private Irregulars() {
    }

    /**
     * Golden Touch: aimed at growing things, it makes the plants around flourish; otherwise it mends what he holds.
     */
    private static boolean goldenTouch(Ctx ctx) {
        BlockHitResult hit = Targets.lookedBlock(ctx.player(), 6.0, ClipContext.Fluid.NONE);
        if (hit.getType() == HitResult.Type.BLOCK && ctx.level().getBlockState(hit.getBlockPos()).getBlock() instanceof BonemealableBlock) {
            int grown = 0;
            for (BlockPos pos : BlockPos.betweenClosed(hit.getBlockPos().offset(-2, -1, -2), hit.getBlockPos().offset(2, 1, 2))) {
                BlockState state = ctx.level().getBlockState(pos);
                if (state.getBlock() instanceof BonemealableBlock && BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), ctx.level(), pos.immutable(), ctx.player())) {
                    Vfx.mote(ctx.level(), Vec3.atCenterOf(pos), Vfx.Palette.LIGHT.bright, 0.05);
                    grown++;
                }
            }

            return grown > 0;
        }

        ItemStack held = ctx.player().getMainHandItem();
        if (!held.isDamageableItem() || !held.isDamaged()) {
            return false;
        }

        held.setDamageValue(Math.max(0, held.getDamageValue() - Math.max(1, held.getMaxDamage() * 3 / 10)));
        Vfx.burst(ctx.level(), ctx.player().getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 10, 0.12);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.ANVIL_USE, 0.6F, 1.6F);
        return true;
    }
}
