package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Aspects of the Awakened who fought beside Sunny in Antarctica, and of Caster of the Forgotten Shore. The novel names
 * none of these Aspects, only what they do, so each is called by what it does.
 */
public final class Cohort {
    /** Caster's: speed beyond the eye, paid for with his life. */
    public static final Aspect BLINDING_SPEED = Aspect.of("blinding_speed", Tier.ASCENDED, Vfx.Palette.FROST)
        .with(
            Ability.active("burst_of_speed", Stage.DORMANT, 6.0, 60, "dash", Cohort::burst),
            Ability.passive("quickened_hand", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.ATTACK_SPEED, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                    out.add(Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                }
            }).filled(),
            Ability.active("afterimage", Stage.ASCENDED, 15.0, 200, "spin", Cohort::afterimage).filled()
        )
        .flaw("borrowed_time")
        .build();

    /** Dorn's: push and pull, something close to gravity in his hands. */
    public static final Aspect PUSH_AND_PULL = Aspect.of("push_and_pull", Tier.AWAKENED, Vfx.Palette.EARTH)
        .with(
            Ability.active("push_and_pull", Stage.DORMANT, 6.0, 40, "slam", Cohort::pushPull),
            Ability.passive("crushing_weight", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    if (source.getDirectEntity() == ctx.player()) {
                        Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 40, 1);
                        return amount + 3.0F * (float)ctx.power();
                    }

                    return amount;
                }
            })
        )
        .flaw("endless_growth")
        .build();

    /** Quentin's: restoration and corrosion, mending what is his and rotting what is not. */
    public static final Aspect RESTORATION_AND_RUIN = Aspect.of("restoration_and_ruin", Tier.AWAKENED, Vfx.Palette.ACID)
        .with(
            Ability.passive("mending_bulwark", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 40 == 0) {
                        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
                            EquipmentSlot.OFFHAND}) {
                            mend(ctx.player().getItemBySlot(slot), 1);
                        }
                    }
                }
            }),
            Ability.passive("corrosion", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    if (source.getDirectEntity() == ctx.player()) {
                        Afflictions.afflict(target, ctx.player(), Afflictions.CORROSION, 0.25 * ctx.power(), 6);
                    }

                    return amount;
                }
            }),
            Ability.active("universal_restoration", Stage.ASCENDED, 15.0, 200, "raise", Cohort::restore)
        )
        .flaw("binding_word")
        .build();

    /** Belle's: a body that can slip through matter for an instant, and an edge no armour stops. */
    public static final Aspect PHANTOM_BLADE = Aspect.of("phantom_blade", Tier.AWAKENED, Vfx.Palette.STEEL)
        .with(
            Ability.active("phase", Stage.DORMANT, 5.0, 40, "dash", Cohort::phase),
            Ability.toggle("displacement_edge", Stage.AWAKENED, 2.0, "slash", new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    return source.getDirectEntity() == ctx.player() ? amount * 1.4F * (1.0F + target.getArmorValue() * 0.03F) : amount;
                }
            })
        )
        .flaw("rash_heart")
        .build();

    private Cohort() {
    }

    private static void mend(ItemStack stack, int amount) {
        if (stack.isDamageableItem() && stack.isDamaged()) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
        }
    }

    /** Burst of Speed: a flash forward too quick to follow, striking everything on the way. */
    private static boolean burst(Ctx ctx) {
        Vec3 to = Kit.clearSpotAhead(ctx.player(), 8.0 + 2.0 * ctx.power());
        if (to == null) {
            return false;
        }

        Kit.bolt(ctx, to.distanceTo(ctx.player().position()), 1.0, (float)(4.0 * ctx.power()), false);
        Kit.blink(ctx, to);
        Kit.quiet(ctx.player(), MobEffects.MOVEMENT_SPEED, 60, 3);
        Kit.sound(ctx.level(), to, SoundEvents.BREEZE_WIND_CHARGE_BURST, 0.8F, 1.6F);
        return true;
    }

    /** Afterimage: he strikes every foe near him in the space of a breath and is back where he stood. */
    private static boolean afterimage(Ctx ctx) {
        var foes = Targets.nearest(ctx.level(), ctx.player(), 10.0, 5);
        if (foes.isEmpty()) {
            return false;
        }

        for (LivingEntity foe : foes) {
            Vfx.trail(ctx.level(), ctx.player().position(), foe.position(), Vfx.Palette.FROST);
            foe.invulnerableTime = 0;
            Targets.hit(ctx.level(), ctx.player(), foe, (float)(5.0 * ctx.power()));
        }

        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.8F);
        return true;
    }

    /** Push and Pull: the space before him shoves foes away hard, or (crouched) drags them in. */
    private static boolean pushPull(Ctx ctx) {
        boolean pull = ctx.player().isShiftKeyDown();
        for (LivingEntity foe : Kit.cone(ctx, 8.0, 40.0, pull ? 0.0F : (float)(4.0 * ctx.power()), false)) {
            Kit.shove(foe, ctx.player().position(), pull ? -1.3 : 1.6, pull ? 0.1 : 0.5);
        }

        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.WARDEN_SONIC_CHARGE, 0.6F, pull ? 0.6F : 1.4F);
        return true;
    }

    /** Universal Restoration: he and the allies near him heal, and what they carry mends. */
    private static boolean restore(Ctx ctx) {
        for (LivingEntity ally : Targets.alliesNear(ctx.level(), ctx.player(), ctx.player().position(), 8.0)) {
            Kit.heal(ctx.level(), ally, (float)(4.0 * ctx.power()), Vfx.Palette.ACID);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = ally.getItemBySlot(slot);
                mend(stack, Math.max(1, stack.getMaxDamage() / 5));
            }
        }

        Vfx.ring(ctx.level(), ctx.player().position().add(0.0, 0.1, 0.0), 8.0, Vfx.Palette.ACID, 48);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.BEACON_POWER_SELECT, 0.8F, 1.3F);
        return true;
    }

    /** Phase: for an instant his body passes through matter, and through whatever was about to hit him. */
    private static boolean phase(Ctx ctx) {
        ctx.player().invulnerableTime = 20;
        Kit.quiet(ctx.player(), MobEffects.DAMAGE_RESISTANCE, 20, 4);
        Vec3 to = Kit.clearSpotAhead(ctx.player(), 4.0);
        if (to != null) {
            Kit.blink(ctx, to);
        }

        Vfx.smoke(ctx.level(), ctx.player().getBoundingBox().getCenter(), 0xA0A8B8, 10, 0.5);
        return true;
    }
}
