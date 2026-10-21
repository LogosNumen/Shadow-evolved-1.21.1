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
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Sunny's Aspect, [Shadow Slave], of the Divine Rank: shadows obey him, carry him, take shape for him, and in the end
 * march for him. Its Shadow Core grows only from what he kills himself, gathering the shadows of the slain.
 */
public final class ShadowSlave {
    /** How many gathered shadows a soul remembers. */
    private static final int MEMORY = 64;

    public static final Aspect ASPECT = Aspect.of("shadow_slave", Tier.DIVINE, Vfx.Palette.SHADOW)
        .with(
            Ability.innate("shadow_bond", Ability.Hooks.NONE),
            Ability.innate("shadow_core", new Ability.Hooks() {
                @Override
                public boolean refusesShards(Ctx ctx) {
                    return true;
                }

                @Override
                public void killed(Ctx ctx, LivingEntity victim) {
                    Soul.feed(ctx.player(), Summons.fragmentsFor(victim, ctx.soul().rank().ladder));
                    gather(ctx, victim);
                }
            }),
            Ability.toggle("shadow_control", Stage.DORMANT, 1.0, "channel_self", new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    int cores = ctx.soul().cores();
                    out.add(Attributes.ATTACK_DAMAGE, 1.0 + 0.5 * cores * ctx.power(), AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.ARMOR, 2.0 + cores, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.MOVEMENT_SPEED, 0.04 + 0.01 * cores, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                }

                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 8 == 0) {
                        Vfx.smoke(ctx.level(), ctx.player().position().add(0.0, 0.9, 0.0), Vfx.Palette.SHADOW.deep, 3, 0.4);
                    }
                }
            }),
            Ability.active("shadow_step", Stage.AWAKENED, 6.0, 40, "dash", ShadowSlave::step),
            Ability.active("shadow_manifestation", Stage.ASCENDED, 15.0, 160, "raise", ShadowSlave::manifest),
            Ability.transformation("shadow_incarnation", 3.0, "transform_burst", new Ability.Hooks() {
                @Override
                public void start(Ctx ctx) {
                    int count = Math.max(1, ctx.soul().cores());
                    for (int i = 0; i < count; i++) {
                        Summons.construct(ctx, SoulConstructEntity.Look.SHADOW, Summons.around(ctx, i, count, 2.0), 20.0 * ctx.power(), 4.0 * ctx.power(),
                            -1, "avatar");
                    }

                    Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.WARDEN_SONIC_CHARGE, 0.8F, 0.6F);
                }

                @Override
                public void stop(Ctx ctx) {
                    Summons.dismiss(ctx, "avatar");
                }
            }),
            Ability.active("shadow_legion", Stage.SUPREME, 60.0, 1200, "raise", ShadowSlave::legion)
        )
        .flaw("clear_conscience")
        .signature("fated", "mark_of_divinity", "child_of_shadows")
        .build();

    private ShadowSlave() {
    }

    /** The Shadow Core keeps the shadow of whatever its bearer slays, for the Legion to call on. */
    private static void gather(Ctx ctx, LivingEntity victim) {
        if (!(victim instanceof Mob) || Bound.bound(victim)) {
            return;
        }

        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        ListTag shadows = ctx.soul().abilityState().getList("Shadows", Tag.TAG_STRING);
        if (shadows.size() >= MEMORY) {
            shadows.remove(0);
        }

        shadows.add(StringTag.valueOf(type.toString()));
        ctx.soul().abilityState().put("Shadows", shadows);
        Vfx.trail(ctx.level(), victim.position(), ctx.player().position(), Vfx.Palette.SHADOW);
    }

    /** Shadow Step: into any shadow in sight, and out of it again. */
    private static boolean step(Ctx ctx) {
        double range = 16.0 + 8.0 * Math.max(0, ctx.soul().rank().ladder);
        BlockHitResult hit = Targets.lookedBlock(ctx.player(), range, ClipContext.Fluid.NONE);
        BlockPos stand = hit.getType() == HitResult.Type.MISS ? null : hit.getBlockPos().relative(hit.getDirection());
        if (stand == null || !Kit.shadowed(ctx.level(), stand)) {
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.shadow_step.no_shadow"), true);
            return false;
        }

        Vec3 to = Vec3.atBottomCenterOf(stand);
        if (!ctx.level().noCollision(ctx.player(), ctx.player().getBoundingBox().move(to.subtract(ctx.player().position())))) {
            return false;
        }

        Kit.blink(ctx, to);
        Kit.sound(ctx.level(), to, SoundEvents.ENDERMAN_TELEPORT, 0.6F, 0.5F);
        return true;
    }

    /** Shadow Manifestation: shadows rise solid where the bearer looks, seize what stands there, and crush it. */
    private static boolean manifest(Ctx ctx) {
        BlockHitResult hit = Targets.lookedBlock(ctx.player(), 24.0, ClipContext.Fluid.NONE);
        Vec3 at = hit.getLocation();
        boolean dark = Kit.shadowed(ctx.level(), BlockPos.containing(at).above());
        float damage = (float)(6.0 * ctx.power() * (dark ? 1.5 : 1.0));
        for (LivingEntity target : Kit.nova(ctx, at, 3.5, damage, false)) {
            Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 60, 5);
            Afflictions.afflict(target, ctx.player(), Afflictions.BLEED, ctx.power(), 3);
        }

        Vfx.pillar(ctx.level(), at, 3.0, Vfx.Palette.SHADOW);
        Kit.sound(ctx.level(), at, SoundEvents.SCULK_SHRIEKER_SHRIEK, 0.7F, 0.6F);
        return true;
    }

    /** Shadow Legion: the shadows of the slain rise from the soul and march for their lord. */
    private static boolean legion(Ctx ctx) {
        ListTag shadows = ctx.soul().abilityState().getList("Shadows", Tag.TAG_STRING);
        if (shadows.isEmpty()) {
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.shadow_legion.empty"), true);
            return false;
        }

        int count = Math.min(shadows.size(), 6 + 2 * ctx.soul().cores());
        for (int i = 0; i < count; i++) {
            String id = shadows.getString(shadows.size() - 1 - i);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
            if (type.create(ctx.level()) instanceof Mob shadow) {
                Vec3 at = Summons.around(ctx, i, count, 3.0);
                shadow.moveTo(at.x, at.y, at.z, ctx.player().getYRot(), 0.0F);
                ctx.level().addFreshEntity(shadow);
                Bound.bind(shadow, ctx.player(), 900, "legion", true);
                shadow.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900, 1, false, false));
                Vfx.smoke(ctx.level(), shadow.getBoundingBox().getCenter(), Vfx.Palette.SHADOW.deep, 14, shadow.getBbWidth());
            }
        }

        Vfx.shockwave(ctx.level(), ctx.player().position(), 8.0, Vfx.Palette.SHADOW);
        return true;
    }
}
