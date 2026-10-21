package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Summons;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.entity.soul.SoulConstructEntity;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

/**
 * Mordret's Aspect, of the Divine Rank: the Prince of Nothing's power over mirrors, reflections and souls. He walks
 * from reflection to reflection, takes bodies by meeting their eyes, keeps a realm inside the mirror, and as a Saint
 * splits his soul across many bodies at once.
 */
public final class PrinceOfNothing {
    public static final Aspect ASPECT = Aspect.of("prince_of_nothing", Tier.DIVINE, Vfx.Palette.STEEL)
        .with(
            Ability.active("mirror_walk", Stage.DORMANT, 6.0, 40, "dash", PrinceOfNothing::walk),
            Ability.active("stolen_eyes", Stage.AWAKENED, 15.0, 300, "point", PrinceOfNothing::possess),
            Ability.active("mirror_realm", Stage.ASCENDED, 20.0, 400, "cast", PrinceOfNothing::realm),
            Ability.transformation("shattered_reflections", 3.0, "transform_burst", new Ability.Hooks() {
                @Override
                public void start(Ctx ctx) {
                    for (int i = 0; i < 3; i++) {
                        Summons.construct(ctx, SoulConstructEntity.Look.MIRROR, Summons.around(ctx, i, 3, 2.0), 18.0 * ctx.power(), 4.0 * ctx.power(),
                            -1, "reflection");
                    }

                    Vfx.shards(ctx.level(), ctx.player().getBoundingBox().getCenter(), Vfx.Palette.STEEL, 24);
                    Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.GLASS_BREAK, 1.0F, 0.6F);
                }

                @Override
                public void stop(Ctx ctx) {
                    Summons.dismiss(ctx, "reflection");
                }
            }),
            Ability.active("kingdom_of_nothing", Stage.SUPREME, 60.0, 1200, "raise", PrinceOfNothing::kingdom).filled()
        )
        .flaw("sevenfold_self")
        .build();

    private PrinceOfNothing() {
    }

    private static boolean reflective(BlockState state) {
        return state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES) || state.is(BlockTags.ICE) || !state.getFluidState().isEmpty();
    }

    /** Mirror Walk: through any reflection in sight (glass, ice, still water) and out beside it. */
    private static boolean walk(Ctx ctx) {
        BlockHitResult hit = Targets.lookedBlock(ctx.player(), 48.0, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.MISS || !reflective(ctx.level().getBlockState(hit.getBlockPos()))) {
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.mirror_walk.no_reflection"), true);
            return false;
        }

        BlockPos stand = hit.getBlockPos().relative(hit.getDirection());
        if (!ctx.level().getFluidState(hit.getBlockPos()).isEmpty()) {
            stand = hit.getBlockPos().above();
        }

        Vec3 to = Vec3.atBottomCenterOf(stand);
        if (!ctx.level().noCollision(ctx.player(), ctx.player().getBoundingBox().move(to.subtract(ctx.player().position())))) {
            return false;
        }

        Vfx.shards(ctx.level(), ctx.player().getBoundingBox().getCenter(), Vfx.Palette.STEEL, 10);
        Kit.blink(ctx, to);
        Kit.sound(ctx.level(), to, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.4F);
        return true;
    }

    /** Stolen Eyes: meeting a creature's gaze, he takes it; it fights for him until its will returns. */
    private static boolean possess(Ctx ctx) {
        LivingEntity seen = Targets.looked(ctx.player(), 16.0);
        if (!(seen instanceof Mob mob) || mob.getMaxHealth() > 120.0F + 60.0F * ctx.power()) {
            return false;
        }

        Bound.bind(mob, ctx.player(), 400, "possessed", false);
        Vfx.beam(ctx.level(), ctx.player().getEyePosition(), mob.getEyePosition(), Vfx.Palette.STEEL, 0.3);
        Vfx.glyphs(ctx.level(), mob.getEyePosition(), 0.6, Vfx.Palette.STEEL, 6);
        Kit.sound(ctx.level(), mob.position(), SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 0.8F);
        return true;
    }

    /** Mirror Realm: a foe is drawn into the reflection and held there, out of the world, for a while. */
    private static boolean realm(Ctx ctx) {
        LivingEntity seen = Targets.looked(ctx.player(), 20.0);
        if (!(seen instanceof Mob mob) || !Targets.foe(mob, ctx.player())) {
            return false;
        }

        Vfx.shards(ctx.level(), mob.getBoundingBox().getCenter(), Vfx.Palette.STEEL, 18);
        Afflictions.banish(mob, (int)(120 + 40 * ctx.power()));
        Kit.sound(ctx.level(), mob.position(), SoundEvents.ILLUSIONER_CAST_SPELL, 1.0F, 1.2F);
        return true;
    }

    /** Kingdom of Nothing: every foe around him falls into the mirror at once. */
    private static boolean kingdom(Ctx ctx) {
        int taken = 0;
        for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 16.0)) {
            if (foe instanceof Mob mob) {
                Afflictions.banish(mob, 160);
                Vfx.shards(ctx.level(), mob.getBoundingBox().getCenter(), Vfx.Palette.STEEL, 10);
                taken++;
            }
        }

        Vfx.dome(ctx.level(), ctx.player().position(), 16.0, Vfx.Palette.STEEL, 120);
        return taken > 0;
    }
}
