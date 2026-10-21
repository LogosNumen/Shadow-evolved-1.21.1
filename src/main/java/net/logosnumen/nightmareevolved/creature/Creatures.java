package net.logosnumen.nightmareevolved.creature;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Raises an ordinary creature into a Nightmare Creature: it takes a rank and a class, grows tougher and harder-hitting
 * with each rank, larger with the fiercer classes, and much stronger again as a guardian, and is named for what it has
 * become ("Awakened Tyrant"). Colossi only take the rank; they are already built to their size.
 */
public final class Creatures {
    private static final ResourceLocation HEALTH = NightmareEvolved.id("creature_health");
    private static final ResourceLocation DAMAGE = NightmareEvolved.id("creature_damage");
    private static final ResourceLocation ARMOR = NightmareEvolved.id("creature_armor");
    private static final ResourceLocation SCALE = NightmareEvolved.id("creature_scale");
    private static final ResourceLocation STEADY = NightmareEvolved.id("creature_steady");

    private Creatures() {
    }

    public static CreatureProfile profile(LivingEntity entity) {
        return entity.getData(ModAttachments.CREATURE.get());
    }

    /** Whether a creature belongs to the Nightmare: a colossus, or anything raised into one. */
    public static boolean ofNightmare(LivingEntity entity) {
        return entity instanceof ColossusEntity || entity.getExistingData(ModAttachments.CREATURE.get()).map(CreatureProfile::present).orElse(false);
    }

    public static CreatureRank rankOf(LivingEntity entity) {
        CreatureProfile profile = entity.getExistingData(ModAttachments.CREATURE.get()).orElse(null);
        if (profile != null && profile.present()) {
            return profile.rank();
        }

        return entity instanceof ColossusEntity colossus ? colossus.kind().rank : CreatureRank.DORMANT;
    }

    /** How many soul cores a creature has: one for a Beast, up to seven for a Titan. A slain creature leaves one shard each. */
    public static int cores(LivingEntity entity) {
        return classOf(entity).ordinal() + 1;
    }

    /**
     * Whether slaying a creature feeds a soul: Nightmare Creatures do, and so do ordinary monsters, which count as
     * Dormant Beasts. Animals and people don't.
     */
    public static boolean feedsSouls(LivingEntity entity) {
        return ofNightmare(entity) || entity instanceof Enemy;
    }

    public static CreatureClass classOf(LivingEntity entity) {
        CreatureProfile profile = entity.getExistingData(ModAttachments.CREATURE.get()).orElse(null);
        if (profile != null && profile.present()) {
            return profile.creatureClass();
        }

        return entity instanceof ColossusEntity ? CreatureClass.TITAN : CreatureClass.BEAST;
    }

    /** "Awakened Tyrant", coloured by rank. */
    public static MutableComponent title(CreatureRank rank, CreatureClass creatureClass) {
        return Component.translatable("creature.nightmare_evolved.title", rank.title(), creatureClass.title()).withStyle(colour(rank));
    }

    public static ChatFormatting colour(CreatureRank rank) {
        return switch (rank) {
            case DORMANT -> ChatFormatting.GRAY;
            case AWAKENED -> ChatFormatting.AQUA;
            case FALLEN -> ChatFormatting.DARK_AQUA;
            case CORRUPTED -> ChatFormatting.GREEN;
            case GREAT -> ChatFormatting.GOLD;
            case CURSED -> ChatFormatting.RED;
            case UNHOLY -> ChatFormatting.DARK_PURPLE;
        };
    }

    /** Makes a creature a Nightmare Creature of a rank and class, belonging to a trial (or -1 for none). */
    public static void raise(Mob mob, CreatureRank rank, CreatureClass creatureClass, boolean guardian, long trial) {
        profile(mob).set(rank, creatureClass, guardian, trial);
        mob.setPersistenceRequired();
        if (mob instanceof ColossusEntity) {
            return;
        }

        int r = rank.ordinal();
        set(mob, Attributes.MAX_HEALTH, HEALTH, r * 0.9 + (guardian ? 2.5 : 0.0), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        set(mob, Attributes.ATTACK_DAMAGE, DAMAGE, r * 2.0 + (guardian ? 2.0 : 0.0), AttributeModifier.Operation.ADD_VALUE);
        set(mob, Attributes.ARMOR, ARMOR, r * 2.0, AttributeModifier.Operation.ADD_VALUE);
        double scale = switch (creatureClass) {
            case TYRANT -> 0.15;
            case TERROR -> 0.3;
            case TITAN -> 0.6;
            default -> 0.0;
        };
        set(mob, Attributes.SCALE, SCALE, scale + (guardian ? 0.25 : 0.0), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        set(mob, Attributes.KNOCKBACK_RESISTANCE, STEADY, Math.min(1.0, creatureClass.ordinal() * 0.12 + (guardian ? 0.4 : 0.0)), AttributeModifier.Operation.ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
        mob.setCustomName(title(rank, creatureClass));
        mob.setCustomNameVisible(guardian);
    }

    private static void set(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            if (amount != 0.0) {
                instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
            }
        }
    }
}
