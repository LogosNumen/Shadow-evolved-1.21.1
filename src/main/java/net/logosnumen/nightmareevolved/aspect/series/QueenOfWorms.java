package net.logosnumen.nightmareevolved.aspect.series;

import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Ki Song's Aspect, the Queen of Worms', of the Supreme Rank (the novel never names it): authority over blood and
 * decay. The wounds she gives fester and grow, the dead rise as her puppets, harm done to her passes to them, as a
 * Saint she becomes a living tide of blood, and as a Sovereign whatever dies in her presence becomes hers.
 */
public final class QueenOfWorms {
    /** How many puppets she can hold at once. */
    private static final int PUPPETS = 8;

    public static final Aspect ASPECT = Aspect.of("queen_of_worms", Tier.SUPREME, Vfx.Palette.BLOOD)
        .with(
            Ability.passive("festering_wound", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    if (source.getEntity() == ctx.player()) {
                        Afflictions.afflict(target, ctx.player(), Afflictions.FESTER, 0.5 * ctx.power(), 6);
                    }

                    return amount;
                }

                @Override
                public void killed(Ctx ctx, LivingEntity victim) {
                    remember(ctx, victim);
                }
            }),
            Ability.active("puppetry", Stage.AWAKENED, 15.0, 200, "raise_one", QueenOfWorms::raiseLast),
            Ability.passive("transference", Stage.ASCENDED, new Ability.Hooks() {
                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    List<Mob> puppets = Bound.of(ctx.level(), ctx.player(), 32.0, "puppet");
                    if (amount <= 0.0F || puppets.isEmpty()) {
                        return amount;
                    }

                    Mob vessel = puppets.get(0);
                    vessel.invulnerableTime = 0;
                    vessel.hurt(ctx.level().damageSources().magic(), amount);
                    Vfx.trail(ctx.level(), ctx.player().position(), vessel.position(), Vfx.Palette.BLOOD);
                    return 0.0F;
                }
            }),
            Ability.transformation("tide_of_blood", 6.0, "transform_gather", Forms.of(new Forms.Shape(1.0, 10.0, 4.0, 0.3, 0.0, true),
                new Ability.Hooks() {
                    @Override
                    public void tick(Ctx ctx, int age) {
                        if (age % 4 == 0) {
                            Vfx.burst(ctx.level(), ctx.player().getBoundingBox().getCenter(), Vfx.Palette.BLOOD, 8, 0.25);
                        }

                        if (age % 20 == 0) {
                            for (LivingEntity foe : Targets.near(ctx.level(), ctx.player(), ctx.player().position(), 4.0)) {
                                Targets.hit(ctx.level(), ctx.player(), foe, (float)(2.0 * ctx.power()));
                                Afflictions.afflict(foe, ctx.player(), Afflictions.BLEED, ctx.power(), 4);
                            }
                        }
                    }
                })),
            Ability.passive("authority_over_death", Stage.SUPREME, new Ability.Hooks() {
                @Override
                public void witnessed(Ctx ctx, LivingEntity victim) {
                    if (victim instanceof Mob && !Bound.bound(victim) && ctx.player().getRandom().nextFloat() < 0.3F
                        && Bound.of(ctx.level(), ctx.player(), 48.0, "puppet").size() < PUPPETS) {
                        raise(ctx, BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()), victim.position());
                    }
                }
            })
        )
        .flaw("mothers_love")
        .build();

    private QueenOfWorms() {
    }

    private static void remember(Ctx ctx, LivingEntity victim) {
        if (!(victim instanceof Mob) || Bound.bound(victim)) {
            return;
        }

        CompoundTag last = new CompoundTag();
        last.putString("Type", BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString());
        last.putDouble("X", victim.getX());
        last.putDouble("Y", victim.getY());
        last.putDouble("Z", victim.getZ());
        last.putLong("At", ctx.now());
        ctx.soul().abilityState().put("LastSlain", last);
    }

    /** Puppetry: the last creature she slew (within the last minute, and not far off) rises again as her puppet. */
    private static boolean raiseLast(Ctx ctx) {
        CompoundTag last = ctx.soul().abilityState().getCompound("LastSlain");
        Vec3 at = new Vec3(last.getDouble("X"), last.getDouble("Y"), last.getDouble("Z"));
        if (last.isEmpty() || ctx.now() - last.getLong("At") > 1200 || at.distanceTo(ctx.player().position()) > 32.0) {
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.puppetry.none"), true);
            return false;
        }

        if (Bound.of(ctx.level(), ctx.player(), 48.0, "puppet").size() >= PUPPETS) {
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.puppetry.full"), true);
            return false;
        }

        ctx.soul().abilityState().remove("LastSlain");
        return raise(ctx, ResourceLocation.parse(last.getString("Type")), at);
    }

    private static boolean raise(Ctx ctx, ResourceLocation id, Vec3 at) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (!(type.create(ctx.level()) instanceof Mob puppet)) {
            return false;
        }

        puppet.moveTo(at.x, at.y, at.z, ctx.player().getYRot(), 0.0F);
        ctx.level().addFreshEntity(puppet);
        Bound.bind(puppet, ctx.player(), 2400, "puppet", true);
        Vfx.burst(ctx.level(), puppet.getBoundingBox().getCenter(), Vfx.Palette.BLOOD, 16, 0.2);
        Kit.sound(ctx.level(), at, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 0.8F, 0.6F);
        return true;
    }
}
