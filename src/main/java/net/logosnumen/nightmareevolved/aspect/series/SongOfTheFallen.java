package net.logosnumen.nightmareevolved.aspect.series;

import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Summons;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

/**
 * Cassie's Aspect, Song of the Fallen, of the Sacred Rank: a prophetess's power to know. She reads the runes of
 * whatever she meets, sees the next few seconds before they come, looks through the eyes of those she has marked,
 * reaches into memories through her own eyes, and at last makes what she remembers real. Her Flaw is blindness, and
 * her Aspect becomes her sight.
 */
public final class SongOfTheFallen {
    public static final Aspect ASPECT = Aspect.of("song_of_the_fallen", Tier.SACRED, Vfx.Palette.RUNE)
        .with(
            Ability.active("insight", Stage.DORMANT, 4.0, 20, "point", SongOfTheFallen::insight, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 2400 == 1200 && ctx.player().getRandom().nextFloat() < 0.6F) {
                        vision(ctx);
                    }
                }
            }),
            Ability.passive("premonition", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 10 == 0) {
                        ctx.level().getEntitiesOfClass(LivingEntity.class, ctx.player().getBoundingBox().inflate(20.0), e -> e != ctx.player())
                            .forEach(e -> Kit.quiet(e, MobEffects.GLOWING, 15, 0));
                    }
                }

                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    if (source.getEntity() != null && ctx.player().getRandom().nextFloat() < 0.2F) {
                        Vfx.glyph(ctx.level(), ctx.player().getEyePosition(), Vfx.Palette.RUNE.bright);
                        return 0.0F;
                    }

                    return amount;
                }
            }),
            Ability.active("shared_senses", Stage.ASCENDED, 10.0, 200, "channel", SongOfTheFallen::share),
            Ability.transformation("eyes_of_memory", 3.0, "transform_gather", new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 5 == 0) {
                        ctx.player().removeEffect(MobEffects.BLINDNESS);
                        LivingEntity seen = Targets.looked(ctx.player(), 16.0);
                        if (seen instanceof Mob mob && Targets.foe(mob, ctx.player()) && mob.getTarget() == ctx.player()) {
                            mob.setTarget(null);
                            Kit.quiet(mob, MobEffects.CONFUSION, 100, 0);
                            Kit.quiet(mob, MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
                            Vfx.glyphs(ctx.level(), mob.getEyePosition(), 0.6, Vfx.Palette.RUNE, 6);
                        }
                    }
                }
            }),
            Ability.active("manifest_memory", Stage.SUPREME, 50.0, 900, "raise", SongOfTheFallen::manifest)
        )
        .flaw("blindness")
        .build();

    private SongOfTheFallen() {
    }

    /** Insight: the runes of the creature or person looked at. Everything the Spell knows, except their Flaw. */
    private static boolean insight(Ctx ctx) {
        LivingEntity seen = Targets.looked(ctx.player(), 24.0);
        if (seen == null) {
            return false;
        }

        ServerPlayer player = ctx.player();
        player.sendSystemMessage(Spell.line("ability.nightmare_evolved.insight.name", seen.getDisplayName()));
        if (seen instanceof ServerPlayer other) {
            SoulData soul = Soul.of(other);
            player.sendSystemMessage(Spell.line("ability.nightmare_evolved.insight.rank", soul.rank().title(), soul.rank().tier()));
            Aspect aspect = Aspects.byId(soul.aspect());
            if (aspect != null) {
                player.sendSystemMessage(Spell.line("ability.nightmare_evolved.insight.aspect", aspect.title(), aspect.tier().title()));
            }

            for (String id : soul.attributes()) {
                SoulAttribute attribute = SoulAttribute.byId(id);
                if (attribute != null) {
                    player.sendSystemMessage(Component.literal("  [").append(attribute.title()).append("]"));
                }
            }
        } else if (Creatures.feedsSouls(seen)) {
            player.sendSystemMessage(Spell.line("ability.nightmare_evolved.insight.creature",
                Creatures.title(Creatures.rankOf(seen), Creatures.classOf(seen)), Creatures.cores(seen)));
        }

        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(seen.getType());
        ctx.soul().abilityState().putString("Remembered", type.toString());
        Vfx.glyphs(ctx.level(), seen.getEyePosition(), 0.8, Vfx.Palette.RUNE, 8);
        return true;
    }

    /** A vision: the strongest threat nearby, and which way it lies. */
    private static void vision(Ctx ctx) {
        List<Mob> threats = ctx.level().getEntitiesOfClass(Mob.class, ctx.player().getBoundingBox().inflate(48.0), m -> m instanceof Enemy && m.isAlive());
        Mob worst = null;
        for (Mob threat : threats) {
            if (worst == null || Creatures.rankOf(threat).ordinal() * 10 + Creatures.cores(threat) > Creatures.rankOf(worst).ordinal() * 10 + Creatures.cores(worst)) {
                worst = threat;
            }
        }

        if (worst != null) {
            Vec3 to = worst.position().subtract(ctx.player().position());
            String direction = Math.abs(to.x) > Math.abs(to.z) ? (to.x > 0 ? "east" : "west") : (to.z > 0 ? "south" : "north");
            ctx.player().sendSystemMessage(Spell.line("ability.nightmare_evolved.insight.vision", worst.getDisplayName(),
                Component.translatable("ability.nightmare_evolved.direction." + direction)));
        }
    }

    /** Shared Senses: she looks through the eyes of a creature or ally for a few seconds. */
    private static boolean share(Ctx ctx) {
        LivingEntity seen = Targets.looked(ctx.player(), 32.0);
        if (seen == null) {
            return false;
        }

        ServerPlayer player = ctx.player();
        player.setCamera(seen);
        Kit.sound(ctx.level(), player.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.6F);
        Vfx.later(120, () -> {
            if (player.getCamera() == seen) {
                player.setCamera(player);
            }
        });
        return true;
    }

    /** Manifest Memory: whatever she last read with Insight takes shape again, and fights for her for a while. */
    private static boolean manifest(Ctx ctx) {
        String remembered = ctx.soul().abilityState().getString("Remembered");
        if (remembered.isEmpty()) {
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.manifest_memory.empty"), true);
            return false;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(remembered));
        if (!(type.create(ctx.level()) instanceof Mob memory)) {
            return false;
        }

        Vec3 at = Summons.around(ctx, 0, 1, 2.5);
        memory.moveTo(at.x, at.y, at.z, ctx.player().getYRot(), 0.0F);
        ctx.level().addFreshEntity(memory);
        Bound.bind(memory, ctx.player(), 600, "memory", true);
        Vfx.glyphs(ctx.level(), memory.getBoundingBox().getCenter(), 1.2, Vfx.Palette.RUNE, 10);
        return true;
    }
}
