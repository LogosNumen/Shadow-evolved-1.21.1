package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.aspect.Summons;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Anvil's Aspect, the King of Swords', of the Supreme Rank (the novel never names it): a deep affinity for metal. His
 * blade moves a little by his will, then flies by itself, then a storm of blades answers him. He can forge a slain
 * creature into a Memory (how, the novel keeps secret; his Transformation is never seen). As a Sovereign, everyone
 * who wields a sword near him is part of his Domain.
 */
public final class KingOfSwords {
    public static final Aspect ASPECT = Aspect.of("king_of_swords", Tier.SUPREME, Vfx.Palette.STEEL)
        .with(
            Ability.passive("metal_affinity", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    ItemStack held = ctx.player().getMainHandItem();
                    if (source.getDirectEntity() != ctx.player() || !(held.is(ItemTags.SWORDS) || held.is(ItemTags.AXES))) {
                        return amount;
                    }

                    return amount * 1.15F * (ctx.player().getRandom().nextFloat() < 0.1F ? 1.5F : 1.0F);
                }
            }),
            Ability.active("flying_sword", Stage.AWAKENED, 10.0, 200, "point", ctx -> {
                Summons.sword(ctx, (float)(5.0 * ctx.power()), 300, 0.0F);
                Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.TRIDENT_RETURN, 1.0F, 1.2F);
                return true;
            }),
            Ability.active("storm_of_swords", Stage.ASCENDED, 30.0, 600, "raise", KingOfSwords::storm),
            Ability.passive("forged_memory", Stage.TRANSCENDENT, new Ability.Hooks() {
                @Override
                public void killed(Ctx ctx, LivingEntity victim) {
                    if (Creatures.ofNightmare(victim) && Creatures.rankOf(victim).ordinal() >= CreatureRank.FALLEN.ordinal()
                        && ctx.player().getRandom().nextFloat() < 0.15F) {
                        forge(ctx, victim);
                    }
                }
            }),
            Ability.passive("sword_domain", Stage.SUPREME, new Ability.Hooks() {
                @Override
                public void tick(Ctx ctx, int age) {
                    if (age % 40 != 0) {
                        return;
                    }

                    for (Player other : ctx.level().players()) {
                        if (other.distanceToSqr(ctx.player()) < 32.0 * 32.0 && other.getMainHandItem().is(ItemTags.SWORDS)) {
                            Kit.quiet(other, MobEffects.DAMAGE_BOOST, 60, 0);
                            Kit.quiet(other, MobEffects.DIG_SPEED, 60, 0);
                        }
                    }
                }

                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return amount * 0.8F;
                }
            })
        )
        .flaw("forsaken_treasures")
        .build();

    private KingOfSwords() {
    }

    /** Storm of Swords: a host of blades rises around him and falls on everything that threatens him. */
    private static boolean storm(Ctx ctx) {
        int count = 6 + 2 * Math.max(0, ctx.soul().rank().ladder);
        for (int i = 0; i < count; i++) {
            Summons.sword(ctx, (float)(4.0 * ctx.power()), 300, (float)(i * Math.PI * 2.0 / count));
        }

        Vfx.ring(ctx.level(), ctx.player().position().add(0.0, 1.5, 0.0), 2.5, Vfx.Palette.STEEL, 32);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.ANVIL_USE, 1.0F, 1.5F);
        return true;
    }

    /** Forged Memory: the slain creature is beaten into a blade that remembers it. */
    private static void forge(Ctx ctx, LivingEntity victim) {
        ItemStack blade = new ItemStack(Items.IRON_SWORD);
        blade.set(DataComponents.CUSTOM_NAME, Component.translatable("ability.nightmare_evolved.forged_memory.name", victim.getDisplayName()));
        ItemEntity drop = new ItemEntity(ctx.level(), victim.getX(), victim.getY() + 0.5, victim.getZ(), blade);
        ctx.level().addFreshEntity(drop);
        Vfx.pillar(ctx.level(), victim.position(), 2.0, Vfx.Palette.STEEL);
        Kit.sound(ctx.level(), victim.position(), SoundEvents.ANVIL_LAND, 0.8F, 1.2F);
    }
}
