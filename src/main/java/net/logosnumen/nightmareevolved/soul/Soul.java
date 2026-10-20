package net.logosnumen.nightmareevolved.soul;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.creature.CreatureProfile;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.network.SoulPayload;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.spell.SpellReplies;
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
import net.neoforged.neoforge.network.PacketDistributor;

/** Working with souls: reading one, raising its rank, growing the body to match, absorbing shards, and syncing. */
public final class Soul {
    private static final ResourceLocation HEALTH = NightmareEvolved.id("rank_health");
    private static final ResourceLocation DAMAGE = NightmareEvolved.id("rank_damage");
    private static final ResourceLocation ARMOR = NightmareEvolved.id("rank_armor");
    private static final ResourceLocation SPEED = NightmareEvolved.id("rank_speed");
    private static final ResourceLocation LEADEN = NightmareEvolved.id("flaw_leaden");

    private Soul() {
    }

    public static SoulData of(Player player) {
        return player.getData(ModAttachments.SOUL.get());
    }

    /** Raises (or sets) a soul's rank: the cores empty into the new ones, the body grows, and essence fills. */
    public static void setRank(ServerPlayer player, Rank rank) {
        SoulData soul = of(player);
        soul.setRank(rank);
        soul.setSaturation(0.0);
        applyStats(player);
        soul.setEssence(soul.maxEssence());
        player.setHealth(player.getMaxHealth());
        sync(player);
    }

    /** Puts the body in step with the soul's rank. */
    public static void applyStats(Player player) {
        Rank rank = of(player).rank();
        set(player, Attributes.MAX_HEALTH, HEALTH, SoulRules.health(rank), AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.ATTACK_DAMAGE, DAMAGE, SoulRules.damage(rank), AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.ARMOR, ARMOR, SoulRules.armor(rank), AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.MOVEMENT_SPEED, SPEED, SoulRules.speed(rank), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        Aspect aspect = Aspects.byId(of(player).aspect());
        boolean leaden = aspect != null && aspect.flaw() == Aspect.Flaw.LEADEN;
        set(player, Attributes.MOVEMENT_SPEED, LEADEN, leaden ? -0.1 : 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(Player player, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            if (amount != 0.0) {
                instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
            }
        }
    }

    /**
     * Absorbs shards of a creature rank into the soul's cores, one at a time until they are full; returns how many
     * were taken. An Aspirant has no core yet and can take none.
     */
    public static int absorb(ServerPlayer player, int creatureRank, int count) {
        SoulData soul = of(player);
        if (soul.capacity() <= 0.0) {
            Spell.say(player, "spell.nightmare_evolved.no_core");
            return 0;
        }

        if (soul.saturated()) {
            Spell.say(player, "spell.nightmare_evolved.already_saturated");
            return 0;
        }

        int taken = 0;
        while (taken < count && !soul.saturated()) {
            soul.setSaturation(soul.saturation() + SoulRules.shardValue(creatureRank));
            taken++;
        }

        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8F, 0.6F + creatureRank * 0.1F);
        if (soul.saturated()) {
            SpellReplies.saturated(player);
        }

        return taken;
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
        PacketDistributor.sendToPlayer(player, SoulPayload.of(of(player)));
    }
}
