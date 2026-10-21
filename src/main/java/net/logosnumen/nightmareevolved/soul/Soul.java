package net.logosnumen.nightmareevolved.soul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Ctx;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureProfile;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.network.SoulPayload;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.spell.SpellReplies;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Working with souls: reading one, raising its Rank, shaping the body to match (its Rank, its saturation, and whatever
 * its abilities, Flaw and Attributes change), absorbing shards, and syncing to the client.
 */
public final class Soul {
    /** Every body change a soul makes is named under this, so the old ones can be cleared before the new are added. */
    private static final String PREFIX = "soul.";

    private Soul() {
    }

    public static SoulData of(Player player) {
        return player.getData(ModAttachments.SOUL.get());
    }

    /** The soul's Flaw and Attributes, as hooks. */
    public static List<SoulHooks> traits(SoulData soul) {
        List<SoulHooks> out = new ArrayList<>();
        Flaw flaw = Flaw.byId(soul.flaw());
        if (flaw != null) {
            out.add(flaw);
        }

        for (String id : soul.attributes()) {
            SoulAttribute attribute = SoulAttribute.byId(id);
            if (attribute != null) {
                out.add(attribute);
            }
        }

        return out;
    }

    /**
     * Everything a soul acts through beyond its Aspect: its Flaw and Attributes, and the enchantments of the Memories it
     * has summoned and carries.
     */
    public static List<SoulHooks> hooks(ServerPlayer player) {
        return SoulMemories.hooks(player);
    }

    /** Ids of every ability the soul has woken, for anything that needs to pick among them. */
    public static List<String> unlockedAbilityIds(ServerPlayer player) {
        SoulData soul = of(player);
        Aspect aspect = Aspects.byId(soul.aspect());
        List<String> out = new ArrayList<>();
        if (aspect != null) {
            for (Ability ability : aspect.unlocked(soul.rank())) {
                if (!ability.innate && ability.kind != Ability.Kind.PASSIVE) {
                    out.add(ability.id);
                }
            }
        }

        return out;
    }

    /** Raises (or sets) a soul's Rank: its cores begin to fill anew, Attributes grow, the body changes, essence fills. */
    public static void setRank(ServerPlayer player, Rank rank) {
        SoulData soul = of(player);
        Aspects.stopAll(player);
        soul.setRank(rank);
        soul.setFragments(0);
        evolveAttributes(soul);
        applyStats(player);
        soul.setEssence(soul.maxEssence());
        player.setHealth(player.getMaxHealth());
        sync(player);
    }

    /** Grows any Attribute whose stronger form the soul's Rank has now reached. */
    public static void evolveAttributes(SoulData soul) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String id : soul.attributes()) {
                SoulAttribute attribute = SoulAttribute.byId(id);
                if (attribute != null && attribute.evolvesTo != null && attribute.evolvesAt != null && soul.rank().atLeast(attribute.evolvesAt)) {
                    soul.replaceAttribute(id, attribute.evolvesTo);
                    changed = true;
                }
            }
        }
    }

    /** Body attributes a soul may change; old changes are cleared from each before new ones go on. */
    private static final List<Holder<Attribute>> TOUCHED = List.of(
        Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.MOVEMENT_SPEED,
        Attributes.ATTACK_SPEED, Attributes.LUCK, Attributes.SCALE, Attributes.KNOCKBACK_RESISTANCE, Attributes.JUMP_STRENGTH,
        Attributes.SWEEPING_DAMAGE_RATIO, Attributes.FALL_DAMAGE_MULTIPLIER, Attributes.BURNING_TIME, Attributes.BLOCK_INTERACTION_RANGE,
        Attributes.ENTITY_INTERACTION_RANGE, Attributes.STEP_HEIGHT, Attributes.SAFE_FALL_DISTANCE, Attributes.GRAVITY, Attributes.OXYGEN_BONUS,
        Attributes.WATER_MOVEMENT_EFFICIENCY, Attributes.ATTACK_KNOCKBACK, Attributes.MOVEMENT_EFFICIENCY, NeoForgeMod.CREATIVE_FLIGHT
    );

    private record Key(Holder<Attribute> attribute, AttributeModifier.Operation operation) {
    }

    /**
     * Shapes the body to the soul: its Rank (deepened by saturation and extra cores), then whatever the abilities in
     * effect, the Flaw and the Attributes add. Also grants or takes away flight. Called on any change to the soul.
     */
    public static void applyStats(Player player) {
        if (!(player instanceof ServerPlayer server)) {
            return;
        }

        SoulData soul = of(player);
        Map<Key, Double> sums = new HashMap<>();
        Ability.Modifiers out = (attribute, amount, operation) -> sums.merge(new Key(attribute, operation), amount, Double::sum);
        Rank rank = soul.rank();
        double saturation = soul.saturation();
        out.add(Attributes.MAX_HEALTH, SoulRules.health(rank, soul.cores(), saturation), AttributeModifier.Operation.ADD_VALUE);
        out.add(Attributes.ATTACK_DAMAGE, SoulRules.damage(rank, soul.cores(), saturation), AttributeModifier.Operation.ADD_VALUE);
        out.add(Attributes.ARMOR, SoulRules.armor(rank, soul.cores(), saturation), AttributeModifier.Operation.ADD_VALUE);
        out.add(Attributes.MOVEMENT_SPEED, SoulRules.speed(rank, saturation), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        boolean flight = false;
        for (Ctx ctx : Aspects.inEffect(server)) {
            ctx.ability().hooks.modifiers(ctx, out);
            flight |= ctx.ability().hooks.flight(ctx);
        }

        for (SoulHooks trait : hooks(server)) {
            trait.modifiers(server, soul, out);
        }

        if (flight) {
            out.add(NeoForgeMod.CREATIVE_FLIGHT, 1.0, AttributeModifier.Operation.ADD_VALUE);
        }

        List<Holder<Attribute>> clear = new ArrayList<>(TOUCHED);
        sums.keySet().forEach(key -> {
            if (!clear.contains(key.attribute())) {
                clear.add(key.attribute());
            }
        });
        for (Holder<Attribute> attribute : clear) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.getModifiers().stream()
                    .filter(m -> m.id().getNamespace().equals(NightmareEvolved.MOD_ID) && m.id().getPath().startsWith(PREFIX))
                    .map(AttributeModifier::id)
                    .toList()
                    .forEach(instance::removeModifier);
            }
        }

        sums.forEach((key, amount) -> {
            AttributeInstance instance = player.getAttribute(key.attribute());
            if (instance != null && amount != 0.0) {
                String name = key.attribute().unwrapKey().map(k -> k.location().getPath()).orElse("attribute").replace('/', '_');
                ResourceLocation id = NightmareEvolved.id(PREFIX + name + "." + key.operation().getSerializedName());
                instance.addPermanentModifier(new AttributeModifier(id, amount, key.operation()));
            }
        });
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * Absorbs shards of a creature Rank into the soul's cores, one at a time until they are full; returns how many were
     * taken. An Aspirant has no core to take them. A Divine Aspect does not stop at a full core: a new one is born,
     * painfully, up to seven, and the soul rises a class (Beast to Titan).
     */
    public static int absorb(ServerPlayer player, int creatureRank, int count) {
        SoulData soul = of(player);
        if (soul.capacity() <= 0) {
            Spell.say(player, "spell.nightmare_evolved.no_core");
            return 0;
        }

        double share = 1.0;
        for (SoulHooks trait : hooks(player)) {
            share *= trait.shards(player, soul);
        }

        for (Ctx ctx : Aspects.inEffect(player)) {
            if (ctx.ability().hooks.refusesShards(ctx)) {
                share = 0.0;
            }
        }

        if (share <= 0.0) {
            Spell.say(player, "spell.nightmare_evolved.shards_useless");
            return 0;
        }

        Aspect aspect = Aspects.byId(soul.aspect());
        boolean grows = aspect != null && aspect.divine();
        if (soul.saturated() && !(grows && soul.cores() < SoulRules.MAX_CORES)) {
            Spell.say(player, "spell.nightmare_evolved.already_saturated");
            return 0;
        }

        int taken = 0;
        while (taken < count) {
            if (soul.saturated()) {
                if (!grows || soul.cores() >= SoulRules.MAX_CORES) {
                    break;
                }

                newCore(player, soul);
            }

            int value = (int)Math.max(1, Math.round(SoulRules.shardValue(creatureRank, soul.rank().ladder) * share));
            soul.setFragments(soul.fragments() + value);
            taken++;
        }

        applyStats(player);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8F, 0.6F + creatureRank * 0.1F);
        if (soul.saturated() && !(grows && soul.cores() < SoulRules.MAX_CORES)) {
            SpellReplies.saturated(player);
        }

        return taken;
    }

    /**
     * Adds fragments straight to the cores, as a kill does for a soul that feeds on what it slays; grows new cores for a
     * Divine Aspect as they fill.
     */
    public static void feed(ServerPlayer player, int fragments) {
        SoulData soul = of(player);
        if (soul.capacity() <= 0 || fragments <= 0) {
            return;
        }

        Aspect aspect = Aspects.byId(soul.aspect());
        boolean grows = aspect != null && aspect.divine();
        int left = fragments;
        while (left > 0) {
            if (soul.saturated()) {
                if (!grows || soul.cores() >= SoulRules.MAX_CORES) {
                    break;
                }

                newCore(player, soul);
            }

            int room = soul.capacity() - soul.fragments();
            int add = Math.min(room, left);
            soul.setFragments(soul.fragments() + add);
            left -= add;
        }

        applyStats(player);
    }

    /** A Divine soul grows a new core: its soul tears to make room, and it rises a class. */
    private static void newCore(ServerPlayer player, SoulData soul) {
        soul.setCores(soul.cores() + 1);
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().magic(), Math.min(player.getHealth() - 1.0F, player.getMaxHealth() * 0.3F));
        Vfx.burst(player.serverLevel(), player.getBoundingBox().getCenter(), Vfx.Palette.SOUL, 30, 0.3);
        Vfx.flash(player.serverLevel(), player.getBoundingBox().getCenter(), Vfx.Palette.SOUL, 2.0F);
        Spell.say(player, "spell.nightmare_evolved.new_core", soulClass(soul).title());
    }

    /** The class a soul has reached by its cores (Beast for one, up to Titan for seven). */
    public static CreatureClass soulClass(SoulData soul) {
        CreatureClass[] classes = CreatureClass.values();
        return classes[Math.max(0, Math.min(classes.length - 1, soul.cores() - 1))];
    }

    /**
     * An entity's place on the shared ladder (Dormant/Sleeper 0 to Unholy/Divine 6), or -1 for anything outside it:
     * Aspirants, and creatures the Nightmare has no hold on.
     */
    public static int ladder(LivingEntity entity) {
        if (entity instanceof Player player) {
            return of(player).rank().ladder;
        }

        if (entity instanceof ColossusEntity colossus) {
            CreatureProfile profile = entity.getData(ModAttachments.CREATURE.get());
            return profile.present() ? profile.rank().ordinal() : colossus.kind().rank.ordinal();
        }

        CreatureProfile profile = entity.getExistingData(ModAttachments.CREATURE.get()).orElse(null);
        return profile != null && profile.present() ? profile.rank().ordinal() : -1;
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, SoulPayload.of(player, of(player)));
    }
}
