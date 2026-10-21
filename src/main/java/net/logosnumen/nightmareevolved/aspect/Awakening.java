package net.logosnumen.nightmareevolved.aspect;

import java.util.ArrayList;
import java.util.List;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.flaw.FlawEvents;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

/**
 * The Spell's gift after the First Nightmare: an Aspect, the Flaw that comes with it, and the Attributes the soul was
 * born with. Rarer Aspect Ranks are rarer to roll. A soul is born with one to three Attributes, or with its Aspect's own
 * when the novel gives it some (Shadow Slave brings Fated, the Mark of Divinity and Child of Shadows).
 */
public final class Awakening {
    /** How often an Aspect of each Rank is rolled, Dormant to Divine. */
    private static final double[] WEIGHT = {10.0, 10.0, 7.0, 4.0, 2.0, 1.2, 0.6};
    /** The chance a rare born Attribute (a lineage, half-divinity, fate) sticks when rolled. */
    private static final double RARE = 0.25;

    private Awakening() {
    }

    /** The weight an Aspect of a Rank is rolled with. */
    public static double weight(Tier tier) {
        return WEIGHT[tier.ordinal()];
    }

    public static Aspect rollAspect(RandomSource random) {
        List<Aspect> all = Aspects.all();
        double total = 0.0;
        for (Aspect aspect : all) {
            total += weight(aspect.tier());
        }

        double pick = random.nextDouble() * total;
        for (Aspect aspect : all) {
            pick -= weight(aspect.tier());
            if (pick <= 0.0) {
                return aspect;
            }
        }

        return all.get(all.size() - 1);
    }

    public static String rollFlaw(Aspect aspect, RandomSource random) {
        List<String> flaws = aspect.flaws();
        return flaws.isEmpty() ? "" : flaws.get(random.nextInt(flaws.size()));
    }

    private static boolean rare(SoulAttribute attribute) {
        return attribute == SoulAttribute.THE_FIRE || attribute == SoulAttribute.THE_BLOOD || attribute == SoulAttribute.NEPHILIM
            || attribute == SoulAttribute.DREAMSPAWN || attribute == SoulAttribute.FATED;
    }

    public static List<String> rollAttributes(Aspect aspect, RandomSource random) {
        if (!aspect.signature().isEmpty()) {
            return aspect.signature();
        }

        float roll = random.nextFloat();
        int count = roll < 0.5F ? 1 : roll < 0.85F ? 2 : 3;
        List<SoulAttribute> pool = new ArrayList<>();
        for (SoulAttribute attribute : SoulAttribute.values()) {
            if (attribute.kind == SoulAttribute.Kind.BORN) {
                pool.add(attribute);
            }
        }

        List<String> out = new ArrayList<>();
        while (out.size() < count && !pool.isEmpty()) {
            SoulAttribute candidate = pool.get(random.nextInt(pool.size()));
            if (rare(candidate) && random.nextDouble() > RARE) {
                continue;
            }

            pool.remove(candidate);
            out.add(candidate.id());
        }

        return out;
    }

    /** Rolls and grants an Aspect, its Flaw and the soul's Attributes, and tells the soul what it has become. */
    public static Aspect grant(ServerPlayer player) {
        Aspect aspect = rollAspect(player.getRandom());
        grant(player, aspect, rollFlaw(aspect, player.getRandom()), rollAttributes(aspect, player.getRandom()));
        return aspect;
    }

    public static void grant(ServerPlayer player, Aspect aspect, String flaw, List<String> attributes) {
        SoulData soul = Soul.of(player);
        Aspects.stopAll(player);
        soul.setAspect(aspect.id(), aspect.tier());
        soul.setFlaw(flaw);
        soul.clearAttributes();
        attributes.forEach(soul::addAttribute);
        Soul.evolveAttributes(soul);
        Soul.applyStats(player);
        soul.setEssence(soul.maxEssence());
        FlawEvents.hideName(player, Flaw.OVERLOOKED.id().equals(flaw));
        player.refreshDisplayName();
        player.refreshTabListName();
        announce(player, soul, aspect);
        Soul.sync(player);
    }

    /** The Spell names what woke: the Aspect and its Rank, what it is, its Flaw, the Attributes, and the first ability. */
    public static void announce(ServerPlayer player, SoulData soul, Aspect aspect) {
        Spell.say(player, "spell.nightmare_evolved.aspect_granted", aspect.title(), aspect.tier().title());
        player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_described", aspect.description()));
        Flaw flaw = Flaw.byId(soul.flaw());
        if (flaw != null) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_flaw", flaw.title(), flaw.description()));
        }

        Component names = Component.empty();
        boolean first = true;
        for (String id : soul.attributes()) {
            SoulAttribute attribute = SoulAttribute.byId(id);
            if (attribute != null) {
                names = names.copy().append(first ? Component.empty() : Component.literal(", ")).append(attribute.title());
                first = false;
            }
        }

        if (!first) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_attributes", names));
        }

        Ability dormant = aspect.at(Stage.DORMANT);
        if (dormant != null) {
            player.sendSystemMessage(Spell.line("spell.nightmare_evolved.aspect_ability", dormant.title()));
        }

        player.sendSystemMessage(Spell.choice("spell.nightmare_evolved.read_runes", "/spell runes"));
    }
}
