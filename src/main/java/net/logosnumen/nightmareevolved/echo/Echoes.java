package net.logosnumen.nightmareevolved.echo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.memory.Family;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Echoes, as the novel has them: magical copies of slain Nightmare Creatures, made of essence and physical only when
 * summoned, with a little will of their own and no soul. They never change; they heal while they rest; and if one is
 * killed, it is gone for good. The Spell gives them rarely, more often for rarer prey.
 *
 * <p>Sunny's Aspect can turn an Echo into a Shadow, if the matching shadow stands in his Soul Sea: a Shadow grows with
 * his Shadow Core, and a badly hurt one sleeps instead of dying.
 */
public final class Echoes {
    /** Marks a summoned Echo's body with its record's id. */
    public static final String TAG = "nightmare_evolved_echo";
    public static final int MAX = 64;
    /** The fragments it takes to turn an Echo into a Shadow. */
    public static final int SHADOW_COST = 100;
    private static final int[] LIMIT = {0, 1, 2, 3, 5, 8, 10, 12};

    private Echoes() {
    }

    public static boolean isEcho(Entity entity) {
        return entity.getPersistentData().hasUUID(TAG);
    }

    /** How many Echoes a soul can have out at once, by its Rank. */
    public static int limit(SoulData soul) {
        return LIMIT[soul.rank().ordinal()];
    }

    /** The essence an Echo costs to summon: more for higher Ranks and classes, much more above its owner's Rank. */
    public static double cost(EchoRecord record, SoulData soul) {
        double base = 2.0 * Math.pow(1 + record.rank, 1.5) * (1.0 + 0.3 * record.cls);
        return base * Math.pow(1.5, Math.max(0, record.rank - Math.max(0, soul.rank().ladder)));
    }

    /** "Carapace Scavenger": an Echo is called by what it was. */
    public static MutableComponent name(EchoRecord record) {
        Optional<EntityType<?>> type = EntityType.byString(record.type);
        return type.<MutableComponent>map(t -> t.getDescription().copy()).orElse(Component.literal(record.type));
    }

    // Receiving.

    /** Whether a slain creature can leave an Echo at all: not a boss of the old world, and not something already bound. */
    public static boolean echoable(LivingEntity victim) {
        return victim instanceof Mob && !(victim instanceof EnderDragon) && !(victim instanceof WitherBoss) && !Bound.bound(victim) && !isEcho(victim);
    }

    /** The Spell's rarer reward for a kill: an Echo, more often for rarer prey and a guardian's one time in four. */
    public static void onKill(ServerPlayer player, LivingEntity victim) {
        SoulData soul = Soul.of(player);
        if (soul.rank() == Rank.ASPIRANT || !Creatures.feedsSouls(victim) || !echoable(victim)) {
            return;
        }

        boolean nightmare = Creatures.ofNightmare(victim);
        boolean guardian = nightmare && Creatures.profile(victim).guardian();
        int rank = Creatures.rankOf(victim).ordinal();
        int cls = Creatures.classOf(victim).ordinal();
        double chance = guardian ? 0.25 : nightmare ? (0.01 + 0.005 * cls) / (1.0 + 0.25 * rank) : 0.0015;
        if (player.getRandom().nextDouble() < chance) {
            grant(player, victim, true);
        }
    }

    /** Freezes a slain creature into an Echo in the player's soul. */
    @Nullable
    public static EchoRecord grant(ServerPlayer player, LivingEntity victim, boolean announce) {
        SoulData soul = Soul.of(player);
        if (soul.rank() == Rank.ASPIRANT || !(victim instanceof Mob)) {
            return null;
        }

        if (soul.echoes().size() >= MAX) {
            Spell.say(player, "spell.nightmare_evolved.echo_overflow");
            return null;
        }

        CompoundTag snapshot = new CompoundTag();
        victim.saveWithoutId(snapshot);
        for (String key : new String[] {"UUID", "Pos", "Motion", "Rotation", "Health", "FallDistance", "Fire", "Air", "OnGround", "HurtTime",
            "DeathTime", "HurtByTimestamp", "active_effects", "Brain", "leash", "Passengers", "AbsorptionAmount"}) {
            snapshot.remove(key);
        }

        CompoundTag attachments = snapshot.getCompound("neoforge:attachments");
        CompoundTag kept = new CompoundTag();
        if (attachments.contains("nightmare_evolved:creature")) {
            kept.put("nightmare_evolved:creature", attachments.get("nightmare_evolved:creature"));
        }

        snapshot.put("neoforge:attachments", kept);
        snapshot.remove("neoforge:persistent_data");
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        int rank = Creatures.rankOf(victim).ordinal();
        int cls = Creatures.classOf(victim).ordinal();
        EchoRecord record = new EchoRecord(UUID.randomUUID(), type, snapshot, rank, cls, roll(victim, cls, player.getRandom()));
        soul.echoes().add(record);
        soul.seaChanged();
        if (announce) {
            Spell.say(player, "spell.nightmare_evolved.echo_received", name(record));
        }

        return record;
    }

    /** The Attributes the Spell finds in a creature: one or two, more for greater classes; rarely one of the Echo's own. */
    private static List<String> roll(LivingEntity victim, int cls, RandomSource random) {
        List<EchoTrait> pool = new ArrayList<>(switch (Family.of(victim)) {
            case UNDEAD -> List.of(EchoTrait.STALWART, EchoTrait.RELENTLESS, EchoTrait.STRONG, EchoTrait.ARMORED);
            case ARTHROPOD -> List.of(EchoTrait.VENOMOUS, EchoTrait.SWIFT, EchoTrait.ARMORED, EchoTrait.KEEN_SENSES);
            case BEAST -> List.of(EchoTrait.SWIFT, EchoTrait.STRONG, EchoTrait.KEEN_SENSES, EchoTrait.RELENTLESS);
            case HUMAN -> List.of(EchoTrait.BATTLE_MASTER, EchoTrait.SWIFT, EchoTrait.STRONG, EchoTrait.SHADOW_SWORN);
            case CONSTRUCT -> List.of(EchoTrait.ARMORED, EchoTrait.STALWART, EchoTrait.STRONG);
            case SPIRIT, ENDER -> List.of(EchoTrait.SHADOW_SWORN, EchoTrait.SWIFT, EchoTrait.KEEN_SENSES);
            default -> List.of(EchoTrait.STRONG, EchoTrait.ARMORED, EchoTrait.SWIFT, EchoTrait.RELENTLESS);
        });
        java.util.Collections.shuffle(pool, new java.util.Random(random.nextLong()));
        int count = Math.min(pool.size(), 1 + (cls >= 1 ? 1 : 0) + (cls >= 4 ? 1 : 0));
        List<String> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add(pool.get(i).id());
        }

        if (random.nextFloat() < 0.1F) {
            EchoTrait[] own = {EchoTrait.DARK_DESTRIER, EchoTrait.SOUL_WEAPON, EchoTrait.SOUL_BEAST};
            out.add(own[random.nextInt(own.length)].id());
        }

        return out;
    }

    // Summoning and dismissing.

    /** Calls an Echo out of the soul, beside its owner. Returns whether it came. */
    public static boolean summon(ServerPlayer player, UUID id) {
        SoulData soul = Soul.of(player);
        EchoRecord record = soul.echo(id);
        if (record == null || record.out || !player.isAlive()) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        if (level.getGameTime() < record.sleepsUntil) {
            tell(player, "echo_sleeping", name(record), (record.sleepsUntil - level.getGameTime()) / 20);
            return false;
        }

        long out = soul.echoes().stream().filter(e -> e.out).count();
        if (out >= limit(soul)) {
            tell(player, "echo_limit", limit(soul));
            return false;
        }

        double cost = cost(record, soul);
        if (!soul.spend(cost)) {
            tell(player, "echo_no_essence", name(record));
            return false;
        }

        double angle = Math.toRadians(player.getYRot() + 90.0 + soul.echoes().indexOf(record) * 40.0);
        Vec3 at = player.position().add(Math.cos(angle) * 2.0, 0.0, Math.sin(angle) * 2.0);
        CompoundTag tag = record.snapshot.copy();
        tag.putString("id", record.type);
        Entity entity = EntityType.loadEntityRecursive(tag, level, e -> {
            e.moveTo(at.x, at.y, at.z, player.getYRot(), 0.0F);
            return e;
        });
        if (!(entity instanceof Mob mob)) {
            soul.setEssence(soul.essence() + cost);
            tell(player, "echo_lost_shape", name(record));
            return false;
        }

        mob.getPersistentData().putUUID(TAG, record.id);
        mob.setCanPickUpLoot(false);
        mob.setPersistenceRequired();
        for (String t : record.traits) {
            EchoTrait trait = EchoTrait.byId(t);
            if (trait != null) {
                trait.apply(mob, player);
            }
        }

        if (record.shadow) {
            grow(mob, soul);
        }

        if (mob.getType().is(EntityTypeTags.UNDEAD) && mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            // An Echo is made of essence, not dead flesh: the sun has nothing to burn.
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(MemoryItems.ECHO_VEIL.get()));
            mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
        }

        mob.setHealth(Math.max(1.0F, record.health * mob.getMaxHealth()));
        level.addFreshEntity(mob);
        Bound.bind(mob, player, -1, "echo", true);
        record.out = true;
        record.body = mob.getUUID();
        soul.seaChanged();
        Vfx.smoke(level, mob.getBoundingBox().getCenter(), record.shadow ? 0x08080C : 0x6A7AA0, 20, mob.getBbWidth());
        Vfx.glyphs(level, mob.getEyePosition(), mob.getBbWidth(), record.shadow ? Vfx.Palette.SHADOW : Vfx.Palette.SOUL, 6);
        level.playSound(null, mob.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
        return true;
    }

    /** A Shadow is as strong as the Shadow Core it is bound to: stronger with every core its master grows. */
    private static void grow(Mob mob, SoulData soul) {
        double grown = 0.15 * Math.max(0, soul.cores() - 1) + 0.1 * Math.max(0, soul.rank().ladder);
        for (var attribute : List.of(Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE)) {
            AttributeInstance instance = mob.getAttribute(attribute);
            if (instance != null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(net.logosnumen.nightmareevolved.NightmareEvolved.id("shadow.growth"), grown,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    /** Sends an Echo back into the soul; its essence comes back if it is near, and it keeps its wounds until healed. */
    public static void dismiss(ServerPlayer player, UUID id, boolean quiet) {
        SoulData soul = Soul.of(player);
        EchoRecord record = soul.echo(id);
        if (record == null || !record.out) {
            return;
        }

        Entity body = record.body == null ? null : player.serverLevel().getEntity(record.body);
        if (body instanceof Mob mob && mob.isAlive()) {
            record.health = Math.max(0.05F, mob.getHealth() / mob.getMaxHealth());
            if (mob.distanceToSqr(player) < 32.0 * 32.0) {
                soul.setEssence(soul.essence() + cost(record, soul));
            }

            if (!quiet) {
                Vfx.smoke(player.serverLevel(), mob.getBoundingBox().getCenter(), record.shadow ? 0x08080C : 0x6A7AA0, 16, mob.getBbWidth());
            }

            mob.getPersistentData().remove(TAG);
            mob.discard();
        }

        record.out = false;
        record.body = null;
        soul.seaChanged();
    }

    public static void dismissAll(ServerPlayer player) {
        for (EchoRecord record : List.copyOf(Soul.of(player).echoes())) {
            if (record.out) {
                dismiss(player, record.id, true);
            }
        }
    }

    /** An Echo's body is slain: an Echo is gone for good; a Shadow falls into a long sleep instead. */
    public static void slain(Mob mob) {
        UUID owner = Bound.owner(mob);
        ServerPlayer player = owner == null || !(mob.level() instanceof ServerLevel level) ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            slain(player, mob);
        }
    }

    /** As above, for an owner already known. */
    public static void slain(ServerPlayer player, Mob mob) {
        SoulData soul = Soul.of(player);
        EchoRecord record = soul.echo(mob.getPersistentData().getUUID(TAG));
        if (record == null) {
            return;
        }

        record.out = false;
        record.body = null;
        if (record.shadow) {
            record.health = 0.1F;
            record.sleepsUntil = player.level().getGameTime() + 6000L;
            Spell.say(player, "spell.nightmare_evolved.shadow_sleeps", name(record));
        } else {
            soul.echoes().remove(record);
            Spell.say(player, "spell.nightmare_evolved.echo_destroyed", name(record));
        }

        soul.seaChanged();
    }

    /** Echoes at rest in the soul heal: a tenth of what they lack every twenty seconds. */
    public static void heal(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        for (EchoRecord record : soul.echoes()) {
            if (!record.out && record.health < 1.0F) {
                record.health = Math.min(1.0F, record.health + 0.1F);
                soul.seaChanged();
            }
        }
    }

    /** After a restart nothing is out, whatever the soul last remembered. */
    public static void settle(ServerPlayer player) {
        SoulData soul = Soul.of(player);
        for (EchoRecord record : soul.echoes()) {
            if (record.out) {
                record.out = false;
                record.body = null;
                soul.seaChanged();
            }
        }
    }

    // Shadows.

    /** The shadows of the slain standing in a Shadow Slave's Soul Sea, by creature kind. */
    public static ListTag standing(SoulData soul) {
        return soul.abilityState().getList("Shadows", Tag.TAG_STRING);
    }

    /** Whether Sunny's Aspect could make this Echo a Shadow now; null if so, or why not. */
    @Nullable
    public static Component whyNotShadow(SoulData soul, EchoRecord record) {
        if (!"shadow_slave".equals(soul.aspect())) {
            return Component.translatable("echo.nightmare_evolved.shadow.not_aspect");
        } else if (record.shadow) {
            return Component.translatable("echo.nightmare_evolved.shadow.already");
        } else if (standing(soul).stream().noneMatch(t -> t.getAsString().equals(record.type))) {
            return Component.translatable("echo.nightmare_evolved.shadow.no_match");
        } else if (soul.echoes().stream().filter(e -> e.shadow).count() >= Math.max(1, soul.cores())) {
            return Component.translatable("echo.nightmare_evolved.shadow.no_core");
        } else if (soul.fragments() < SHADOW_COST) {
            return Component.translatable("echo.nightmare_evolved.shadow.no_fragments", SHADOW_COST);
        }

        return null;
    }

    /**
     * Turns an Echo into a Shadow: the matching shadow standing in the Soul Sea dissolves into dark light, and the
     * Echo, wreathed in black flame, takes a living shadow in place of its weave. Costs a hundred fragments.
     */
    public static boolean makeShadow(ServerPlayer player, UUID id) {
        SoulData soul = Soul.of(player);
        EchoRecord record = soul.echo(id);
        if (record == null) {
            return false;
        }

        Component why = whyNotShadow(soul, record);
        if (why != null) {
            player.displayClientMessage(why, true);
            return false;
        }

        ListTag standing = standing(soul);
        for (int i = 0; i < standing.size(); i++) {
            if (standing.getString(i).equals(record.type)) {
                standing.remove(i);
                break;
            }
        }

        soul.abilityState().put("Shadows", standing);
        soul.setFragments(soul.fragments() - SHADOW_COST);
        record.shadow = true;
        record.health = 1.0F;
        soul.seaChanged();
        Spell.say(player, "spell.nightmare_evolved.shadow_made", name(record));
        Vfx.burst(player.serverLevel(), Vfx.chest(player), Vfx.Palette.SHADOW, 30, 0.25);
        Soul.applyStats(player);
        return true;
    }

    private static void tell(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("echo.nightmare_evolved." + key, args), true);
    }
}
