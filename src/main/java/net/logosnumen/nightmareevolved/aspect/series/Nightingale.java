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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Kai's Aspect, Nightingale, which rose from the Awakened to the Ascended Rank when he slew the Ivory Dragon: he flies,
 * sees far in the dark and through every illusion, and his voice compels whoever hears it. As a Saint he becomes a
 * dragon of midnight scales; as a Sovereign he brands a word on the souls that follow him.
 */
public final class Nightingale {
    public static final Aspect ASPECT = Aspect.of("nightingale", Tier.ASCENDED, Vfx.Palette.FROST)
        .with(
            Ability.toggle("wings_of_night", Stage.DORMANT, 1.5, "flap", new Ability.Hooks() {
                @Override
                public boolean flight(Ctx ctx) {
                    return true;
                }

                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 3 == 0 && ctx.player().getAbilities().flying) {
                        Vfx.wings(ctx.level(), ctx.player(), Vfx.Palette.FROST, 0.9, age * 0.2);
                    }
                }
            }),
            Ability.toggle("true_sight", Stage.AWAKENED, 0.5, "point", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 10 == 0) {
                        Kit.quiet(ctx.player(), MobEffects.NIGHT_VISION, 260, 0);
                        ctx.level().getEntitiesOfClass(LivingEntity.class, ctx.player().getBoundingBox().inflate(32.0), e -> e != ctx.player())
                            .forEach(e -> Kit.quiet(e, MobEffects.GLOWING, 15, 0));
                    }
                }
            }),
            Ability.active("commanding_voice", Stage.ASCENDED, 15.0, 200, "roar", Nightingale::voice),
            Ability.transformation("midnight_dragon", 5.0, "transform_burst", Forms.of(new Forms.Shape(2.5, 20.0, 4.0, 0.1, 6.0, true))),
            Ability.active("branded_word", Stage.SUPREME, 40.0, 600, "raise_one", Nightingale::brand, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 40 != 0) {
                        return;
                    }

                    ListTag branded = ctx.soul().abilityState().getList("Branded", Tag.TAG_STRING);
                    for (Player other : ctx.level().players()) {
                        if (other != ctx.player() && other.distanceToSqr(ctx.player()) < 32.0 * 32.0
                            && branded.stream().anyMatch(t -> t.getAsString().equals(other.getUUID().toString()))) {
                            Kit.quiet(other, MobEffects.DAMAGE_BOOST, 60, 0);
                            Kit.quiet(other, MobEffects.REGENERATION, 60, 0);
                        }
                    }
                }
            })
        )
        .flaw("ear_for_lies")
        .build();

    private Nightingale() {
    }

    /**
     * Commanding Voice: foes who hear it falter and forget their prey, and allies take heart. Crouched, it is a
     * crushing wave of sound instead.
     */
    private static boolean voice(Ctx ctx) {
        ServerPlayer player = ctx.player();
        if (player.isShiftKeyDown()) {
            for (LivingEntity target : Kit.cone(ctx, 10.0, 35.0, (float)(6.0 * ctx.power()), false)) {
                Kit.shove(target, player.position(), 1.2, 0.3);
            }

            Kit.sound(ctx.level(), player.position(), SoundEvents.WARDEN_SONIC_BOOM, 1.0F, 1.2F);
            return true;
        }

        for (LivingEntity foe : Targets.near(ctx.level(), player, player.position(), 12.0)) {
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }

            Kit.quiet(foe, MobEffects.MOVEMENT_SLOWDOWN, 120, 2);
            Kit.quiet(foe, MobEffects.WEAKNESS, 120, 1);
        }

        for (LivingEntity ally : Targets.alliesNear(ctx.level(), player, player.position(), 16.0)) {
            Kit.buff(ally, MobEffects.DAMAGE_BOOST, 10, 0);
            Kit.buff(ally, MobEffects.DAMAGE_RESISTANCE, 10, 0);
        }

        Vfx.shockwave(ctx.level(), player.position(), 12.0, Vfx.Palette.FROST);
        Kit.sound(ctx.level(), player.position(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 1.5F, 0.6F);
        return true;
    }

    /** Branded Word: his word marks the souls of willing allies nearby; near him, they share his Domain's strength. */
    private static boolean brand(Ctx ctx) {
        ListTag branded = ctx.soul().abilityState().getList("Branded", Tag.TAG_STRING);
        int marked = 0;
        for (Player other : ctx.level().players()) {
            String id = other.getUUID().toString();
            if (other != ctx.player() && other.distanceToSqr(ctx.player()) < 24.0 * 24.0 && branded.stream().noneMatch(t -> t.getAsString().equals(id))) {
                branded.add(StringTag.valueOf(id));
                Vfx.glyphs(ctx.level(), other.getEyePosition(), 0.7, Vfx.Palette.FROST, 6);
                marked++;
            }
        }

        ctx.soul().abilityState().put("Branded", branded);
        return marked > 0;
    }
}
