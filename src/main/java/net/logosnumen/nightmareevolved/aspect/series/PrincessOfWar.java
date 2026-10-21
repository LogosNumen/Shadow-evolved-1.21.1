package net.logosnumen.nightmareevolved.aspect.series;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.aspect.Forms;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Morgan's Aspect, the Princess of War's, of the Transcendent Rank (the novel never names it): she is a blade herself.
 * Whatever she touches grows sharp, her flesh is tempered steel, she draws enchantments into her own body and soul
 * (three at a time), and as a Saint she becomes a river of living metal.
 */
public final class PrincessOfWar {
    /** How many enchantments she can hold within herself at once. */
    private static final int HELD = 3;

    public static final Aspect ASPECT = Aspect.of("princess_of_war", Tier.TRANSCENDENT, Vfx.Palette.BLOOD)
        .with(
            Ability.passive("whetted_touch", Stage.DORMANT, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    if (source.getDirectEntity() != ctx.player()) {
                        return amount;
                    }

                    float edge = ctx.player().getMainHandItem().isEmpty() ? 4.0F : 2.0F;
                    return amount + edge * (float)ctx.power();
                }
            }),
            Ability.passive("tempered_flesh", Stage.AWAKENED, new Ability.Hooks() {
                @Override
                public void modifiers(Ctx ctx, Ability.Modifiers out) {
                    out.add(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE);
                    out.add(Attributes.ARMOR_TOUGHNESS, 4.0, AttributeModifier.Operation.ADD_VALUE);
                }
            }),
            Ability.active("enchantment_absorption", Stage.ASCENDED, 10.0, 100, "channel_self", PrincessOfWar::absorb, new Ability.Hooks() {
                @Override
                public float dealt(Ctx ctx, LivingEntity target, DamageSource source, float amount) {
                    return source.getDirectEntity() == ctx.player() ? amount + held(ctx) : amount;
                }

                @Override
                public float taken(Ctx ctx, DamageSource source, float amount) {
                    return source.is(DamageTypeTags.BYPASSES_ARMOR) ? amount : amount * Math.max(0.6F, 1.0F - 0.03F * held(ctx));
                }
            }),
            Ability.transformation("river_of_steel", 4.0, "transform_gather", Forms.of(new Forms.Shape(1.0, 10.0, 4.0, 0.3, 8.0, false),
                new Ability.Hooks() {
                    @Override
                    public float taken(Ctx ctx, DamageSource source, float amount) {
                        return source.is(DamageTypeTags.BYPASSES_ARMOR) ? amount : amount * 0.5F;
                    }

                    @Override
                    public void tick(Ctx ctx, int age) {
                        if (age % 3 == 0) {
                            Vfx.trail(ctx.level(), ctx.player().position(), ctx.player().position().add(ctx.player().getDeltaMovement().scale(-3.0)),
                                Vfx.Palette.STEEL);
                        }
                    }
                })),
            Ability.active("sundering_cut", Stage.SUPREME, 50.0, 600, "slash_down", PrincessOfWar::sunder).filled()
        )
        .flaw("brittle_touch")
        .build();

    private PrincessOfWar() {
    }

    /** The total of every enchantment level she holds within herself. */
    private static int held(Ctx ctx) {
        CompoundTag held = ctx.soul().abilityState().getCompound("Absorbed");
        int total = 0;
        for (String key : held.getAllKeys()) {
            total += held.getInt(key);
        }

        return total;
    }

    /**
     * Enchantment Absorption: the enchantments of what she holds flow into her, up to three; what she holds keeps none.
     * Crouched, she lets go of all she holds within.
     */
    private static boolean absorb(Ctx ctx) {
        if (ctx.player().isShiftKeyDown()) {
            ctx.soul().abilityState().remove("Absorbed");
            ctx.player().displayClientMessage(Component.translatable("ability.nightmare_evolved.enchantment_absorption.released"), true);
            return true;
        }

        ItemStack stack = ctx.player().getMainHandItem();
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        CompoundTag held = ctx.soul().abilityState().getCompound("Absorbed");
        if (enchantments.isEmpty() || held.size() >= HELD) {
            return false;
        }

        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (held.size() >= HELD) {
                break;
            }

            String id = enchantment.unwrapKey().map(k -> k.location().toString()).orElse("?");
            held.putInt(id, Math.max(held.getInt(id), enchantments.getLevel(enchantment)));
        }

        ctx.soul().abilityState().put("Absorbed", held);
        EnchantmentHelper.setEnchantments(stack, ItemEnchantments.EMPTY);
        Vfx.glyphs(ctx.level(), ctx.player().getBoundingBox().getCenter(), 0.9, Vfx.Palette.BLOOD, 8);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 0.6F);
        return true;
    }

    /** Sundering Cut: one stroke that cleaves everything in a long line before her. */
    private static boolean sunder(Ctx ctx) {
        Kit.bolt(ctx, 24.0, 1.2, (float)(18.0 * ctx.power()), false);
        Vfx.slash(ctx.level(), ctx.player(), 3.0, Vfx.Palette.BLOOD, 60.0, 80.0);
        Kit.sound(ctx.level(), ctx.player().position(), SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 0.4F);
        return true;
    }
}
