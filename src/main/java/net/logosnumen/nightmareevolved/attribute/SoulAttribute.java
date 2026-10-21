package net.logosnumen.nightmareevolved.attribute;

import java.util.Locale;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulHooks;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Spider;

/**
 * Every Attribute the novel shows on a soul's runes. {@link Kind#BORN} ones are the traits a soul brings to its First
 * Nightmare, and are rolled there; the rest are earned, inherited through lineage Memories, bound from relics, or
 * belong to Echoes and Shadows. Some grow into stronger forms as the soul rises ({@link #evolvesTo} at
 * {@link #evolvesAt}). Descriptions are the mod's own words, not the Spell's runes.
 */
public enum SoulAttribute implements SoulHooks {
    /** Fate bends around this soul: unlikely things happen to it, for good and ill. Once in a while, death misses. */
    FATED(Kind.BORN) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.LUCK, 3.0, AttributeModifier.Operation.ADD_VALUE);
        }

        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            long now = player.level().getGameTime();
            if (amount >= player.getHealth() && now >= soul.abilityState().getLong("FateSpared")) {
                soul.abilityState().putLong("FateSpared", now + 12000);
                Vfx.flash(player.serverLevel(), player.getBoundingBox().getCenter(), Vfx.Palette.SOUL, 1.5F);
                return Math.max(0.0F, player.getHealth() - 1.0F);
            }

            return amount;
        }
    },
    /** A faint trace of divinity. Blows against profane creatures land a little truer. */
    MARK_OF_DIVINITY(Kind.BORN, "spark_of_divinity", Rank.MASTER) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return Creatures.ofNightmare(target) ? amount * 1.05F : amount;
        }
    },
    SPARK_OF_DIVINITY(Kind.EARNED, "ember_of_divinity", Rank.SAINT) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return Creatures.ofNightmare(target) ? amount * 1.1F : amount;
        }
    },
    EMBER_OF_DIVINITY(Kind.EARNED, "flame_of_divinity", Rank.SOVEREIGN) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return Creatures.ofNightmare(target) ? amount * 1.16F : amount;
        }
    },
    FLAME_OF_DIVINITY(Kind.EARNED) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return Creatures.ofNightmare(target) ? amount * 1.25F : amount;
        }
    },
    /** Kin to the shadows: sees in the dark and feels what moves in it. */
    CHILD_OF_SHADOWS(Kind.BORN, "master_of_shadows", Rank.SAINT) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            darksight(player, age);
        }
    },
    /** Shadows answer this soul: in darkness, hunters lose sight of it when it moves softly. */
    MASTER_OF_SHADOWS(Kind.EARNED, "lord_of_shadows", Rank.SOVEREIGN) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            darksight(player, age);
            shroud(player, age);
        }
    },
    /** Lord over shadows, even those of lesser living things; strikes from the dark land far harder. */
    LORD_OF_SHADOWS(Kind.EARNED) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            darksight(player, age);
            shroud(player, age);
        }

        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return AttributeKit.inDarkness(player) ? amount * 1.15F : amount;
        }
    },
    /** Born of two worlds and welcome in neither; stronger on the dreaming side. */
    DREAMSPAWN(Kind.BORN) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return AttributeKit.dreaming(player) ? amount * 1.1F : amount;
        }

        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return AttributeKit.dreaming(player) ? amount * 0.9F : amount;
        }
    },
    /** Of the divine and the profane at once: beautiful, harrowing, and hardier than a human should be. */
    NEPHILIM(Kind.BORN) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.MAX_HEALTH, 4.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** A blade that strikes fast and true. */
    SWIFT_HAND(Kind.BORN) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ATTACK_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }

        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return player.getRandom().nextFloat() < 0.1F ? amount * 1.5F : amount;
        }
    },
    /** Known to the shadows, if less than their children are: crouched in darkness, it is hard to see. */
    SHADOW_SWORN(Kind.BORN) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 10 == 0 && player.isShiftKeyDown() && AttributeKit.inDarkness(player)) {
                Kit.quiet(player, MobEffects.INVISIBILITY, 30, 0);
            }
        }
    },
    /** Made, like Death, to be a weapon; deadliest against the already failing. */
    MARK_OF_DEATH(Kind.BORN) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            float out = amount * 1.1F;
            return AttributeKit.low(target, 0.25F) ? out * 1.25F : out;
        }
    },
    /** A gift for words that carry weight: the soul's power costs a little less and returns a little faster. */
    POET(Kind.BORN) {
        @Override
        public double cost(ServerPlayer player, SoulData soul, Ability ability) {
            return 0.9;
        }

        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return 1.1;
        }
    },
    /** A soul tied to the world's own essence; storms and rain refill it. */
    GODHEART(Kind.BORN) {
        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return player.level().isRainingAt(player.blockPosition().above()) ? 1.5 : 1.0;
        }
    },
    /** Favoured by fortune; now and then a blow simply misses. */
    LUCKY(Kind.BORN) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.LUCK, 2.0, AttributeModifier.Operation.ADD_VALUE);
        }

        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return source.getEntity() != null && player.getRandom().nextFloat() < 0.08F ? 0.0F : amount;
        }
    },
    /** A marvellous learner: picks up what it sees fast, and its powers rest less between uses. */
    MARVEL(Kind.BORN) {
        @Override
        public double cooldown(ServerPlayer player, SoulData soul, Ability ability) {
            return 0.9;
        }
    },
    /** Swift and tireless. */
    SWIFT(Kind.BORN) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            out.add(Attributes.JUMP_STRENGTH, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    },
    /** Born for the battlefield: at home with any weapon. */
    BATTLE_MASTER(Kind.BORN) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ATTACK_DAMAGE, 1.5, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.SWEEPING_DAMAGE_RATIO, 0.25, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** Hard to hurt in any way, and the mind cannot be shaken. */
    STALWART(Kind.BORN) {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return amount * 0.9F;
        }

        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 10 == 0 && player.hasEffect(MobEffects.CONFUSION)) {
                player.removeEffect(MobEffects.CONFUSION);
            }
        }
    },
    /** An insatiable hunger, and the growth that comes of feeding it. */
    RAVENOUS(Kind.BORN) {
        @Override
        public double shards(ServerPlayer player, SoulData soul) {
            return 1.25;
        }

        @Override
        public void killed(ServerPlayer player, SoulData soul, LivingEntity victim) {
            player.getFoodData().eat(1, 0.2F);
        }
    },
    /** A body tougher than flesh, and growing tougher: iron, then steel. */
    LESSER_IRON_BODY(Kind.BORN, "greater_steel_body", Rank.MASTER) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ARMOR, 1.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ARMOR_TOUGHNESS, 2.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    GREATER_STEEL_BODY(Kind.EARNED, "complete_steel_body", Rank.SAINT) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ARMOR_TOUGHNESS, 4.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    COMPLETE_STEEL_BODY(Kind.EARNED) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ARMOR_TOUGHNESS, 6.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.KNOCKBACK_RESISTANCE, 0.3, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** A vestige of an ancient darkness in the heart, waking when no light falls. */
    HEART_OF_DARKNESS(Kind.BORN) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return AttributeKit.inDarkness(player) ? amount * 1.2F : amount;
        }
    },
    /** Might that grows the more it is feared: the more that hunt it, the harder it strikes back. */
    DREADLORD(Kind.BORN) {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return amount * (1.0F + 0.04F * Math.min(5, AttributeKit.hunters(player, 12.0)));
        }
    },
    /** Walks dreams as others walk roads: quick on the dreaming side, and never troubled by sleeplessness. */
    DREAMWALKER(Kind.BORN) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 1200 == 0) {
                player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            }

            if (age % 20 == 0 && AttributeKit.dreaming(player)) {
                Kit.quiet(player, MobEffects.MOVEMENT_SPEED, 40, 0);
            }
        }
    },
    /** Draws the world's elemental essence in faster. */
    SPIRIT_GUIDE(Kind.BORN) {
        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return 1.25;
        }
    },
    /** Guides essence as it flows through the body: it is spent more slowly and returns more quickly. */
    SHADOW_GUIDE(Kind.BORN) {
        @Override
        public double cost(ServerPlayer player, SoulData soul, Ability ability) {
            return 0.85;
        }

        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return 1.1;
        }
    },
    /** Bound companions thrive near this soul. */
    SOUL_COMPANION(Kind.BORN) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 40 == 0) {
                player.level().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(16.0), t -> t.isOwnedBy(player))
                    .forEach(t -> Kit.quiet(t, MobEffects.REGENERATION, 60, 0));
            }
        }
    },
    /** The Sun God's lineage, in the blood: fire troubles it less, and its essence burns brighter. */
    THE_FIRE(Kind.BORN) {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FIRE) ? amount * 0.5F : amount;
        }

        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.BURNING_TIME, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    },
    /** The core of the Beast God's lineage: beasts recognise their better, and strike it less hard. */
    THE_BLOOD(Kind.BORN) {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return source.getEntity() instanceof Animal || source.getEntity() instanceof Spider ? amount * 0.8F : amount;
        }
    },
    /** Weaver's forbidden lineage in the blood: it will not stop flowing, and the body mends itself. */
    BLOOD_WEAVE(Kind.LINEAGE) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 100 == 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0F);
            }
        }
    },
    /** Weaver's lineage in the bones: they do not break, and the hands grow finely sensitive. */
    BONE_WEAVE(Kind.LINEAGE) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.FALL_DAMAGE_MULTIPLIER, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            out.add(Attributes.KNOCKBACK_RESISTANCE, 0.2, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** Weaver's lineage in the flesh: stronger, and harder to wound. */
    FLESH_WEAVE(Kind.LINEAGE) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.MAX_HEALTH, 6.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** Weaver's lineage in the mind: sharper, and closed to whatever would cloud it. */
    MIND_WEAVE(Kind.LINEAGE) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 10 == 0) {
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.DARKNESS);
            }
        }
    },
    /** Weaver's lineage in the soul itself: sturdier, quicker to recover its essence. */
    SOUL_WEAVE(Kind.LINEAGE) {
        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return 1.25;
        }
    },
    /** The first shell of a soulbound mantle forged in the Underworld: skin like stone. */
    MARBLE_SHELL(Kind.RELIC, "onyx_shell", Rank.MASTER) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            shell(out, 4.0, 0.2);
        }
    },
    ONYX_SHELL(Kind.RELIC, "jade_shell", Rank.SAINT) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            shell(out, 6.0, 0.3);
        }
    },
    JADE_SHELL(Kind.RELIC) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            shell(out, 8.0, 0.4);
        }
    },
    /** A bound sword, merged into the soul. */
    BLESSING(Kind.RELIC) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ATTACK_DAMAGE, 3.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** A bound charm of seven black rings: whatever strikes its bearer is weakened in turn. */
    CURSE(Kind.RELIC) {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            if (source.getEntity() instanceof LivingEntity attacker && attacker != player) {
                Kit.quiet(attacker, MobEffects.WEAKNESS, 60, 0);
            }

            return amount;
        }
    },
    /** Earned by slaying a dragon: stronger, tougher, and hard to burn. */
    DRAGONSLAYER(Kind.EARNED) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADD_VALUE);
        }

        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION) ? amount * 0.6F : amount;
        }
    },
    /** Left by rings of ash: at home in great heat, and stronger for it. */
    MARK_OF_ASH(Kind.EARNED) {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FIRE) ? amount * 0.7F : amount;
        }

        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            return player.isOnFire() || player.isInLava() ? amount * 1.15F : amount;
        }
    },
    /** Marked by a Lord of Shadows: shadows lend their help, and power costs a little less. */
    MARK_OF_SHADOWS(Kind.EARNED) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            darksight(player, age);
        }

        @Override
        public double cost(ServerPlayer player, SoulData soul, Ability ability) {
            return 0.95;
        }
    },
    /** Cut loose from fate: no one remembers, nothing is destined, and no True Name can be earned. */
    FATELESS(Kind.EARNED) {
        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.LUCK, -1.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** Made a thrall: the will is not quite one's own. It passes in time. */
    ENTHRALLED(Kind.EARNED) {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 == 0) {
                Kit.quiet(player, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
                Kit.quiet(player, MobEffects.DIG_SLOWDOWN, 40, 0);
                if (player.level().getGameTime() >= soul.abilityState().getLong("EnthralledUntil")) {
                    soul.removeAttribute(this.id());
                }
            }
        }
    },
    /** An Echo's or Shadow's: a steed for a shadow warrior, stronger amid darkness. */
    DARK_DESTRIER(Kind.ECHO),
    /** An Echo's or Shadow's: it can take the shape of a weapon. */
    SOUL_WEAPON(Kind.ECHO),
    /** An Echo's or Shadow's: a beast of the soul, which grows with its master. */
    SOUL_BEAST(Kind.ECHO);

    /** Where an Attribute comes from. */
    public enum Kind {
        /** A trait the soul brings with it; rolled in the First Nightmare. */
        BORN,
        /** Grown into or won. */
        EARNED,
        /** Inherited through a lineage Memory. */
        LINEAGE,
        /** Bound from a relic. */
        RELIC,
        /** Belongs to Echoes and Shadows. */
        ECHO
    }

    public final Kind kind;
    @Nullable
    public final String evolvesTo;
    @Nullable
    public final Rank evolvesAt;

    SoulAttribute(Kind kind) {
        this(kind, null, null);
    }

    SoulAttribute(Kind kind, @Nullable String evolvesTo, @Nullable Rank evolvesAt) {
        this.kind = kind;
        this.evolvesTo = evolvesTo;
        this.evolvesAt = evolvesAt;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("attribute.nightmare_evolved." + this.id());
    }

    public Component description() {
        return Component.translatable("attribute.nightmare_evolved." + this.id() + ".desc");
    }

    @Nullable
    public static SoulAttribute byId(String id) {
        for (SoulAttribute attribute : values()) {
            if (attribute.id().equals(id)) {
                return attribute;
            }
        }

        return null;
    }

    /** Sees in darkness: night vision whenever the light around fails. */
    static void darksight(ServerPlayer player, int age) {
        if (age % 20 == 0 && AttributeKit.inDarkness(player)) {
            Kit.quiet(player, MobEffects.NIGHT_VISION, 260, 0);
        }
    }

    /** Crouched in darkness, a soul that commands shadows slips from its hunters' sight. */
    static void shroud(ServerPlayer player, int age) {
        if (age % 20 == 0 && player.isShiftKeyDown() && AttributeKit.inDarkness(player)) {
            player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24.0),
                m -> m.getTarget() == player && m.distanceToSqr(player) > 36.0).forEach(m -> m.setTarget(null));
        }
    }

    static void shell(Ability.Modifiers out, double armour, double steadiness) {
        out.add(Attributes.ARMOR, armour, AttributeModifier.Operation.ADD_VALUE);
        out.add(Attributes.KNOCKBACK_RESISTANCE, steadiness, AttributeModifier.Operation.ADD_VALUE);
    }
}
