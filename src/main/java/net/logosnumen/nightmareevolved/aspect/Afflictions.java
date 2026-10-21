package net.logosnumen.nightmareevolved.aspect;

import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.core.ModAttachments;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Harm and marks an ability leaves behind on a creature, ticking once a second: blood that won't stop, wounds that
 * fester and grow, decay that piles up with every blow, soul fire, and weak points laid bare. Each is credited to the
 * player who caused it.
 */
public final class Afflictions {
    /** Bleeding: a steady loss each second. */
    public static final String BLEED = "bleed";
    /** A festering wound: small at first, a quarter worse each second. */
    public static final String FESTER = "fester";
    /** Decay: each stack does a little harm each second; stacks pile up with each blow. */
    public static final String CORROSION = "corrosion";
    /** Soul fire: burns the soul, so armour does not help. */
    public static final String SOUL_FIRE = "soul_fire";
    /** A weak point laid bare: the marker and their allies hit it far harder. */
    public static final String MARKED = "marked";

    private static final String BANISHED = "nightmare_evolved_banished";

    private Afflictions() {
    }

    /** Lays (or renews) an affliction. {@code amount} is harm per second (or the mark's extra share of damage). */
    public static void afflict(LivingEntity target, ServerPlayer source, String type, double amount, int seconds) {
        CompoundTag all = target.getData(ModAttachments.AFFLICTION).copy();
        CompoundTag entry = all.getCompound(type);
        long until = target.level().getGameTime() + seconds * 20L;
        if (type.equals(CORROSION)) {
            entry.putInt("Stacks", Math.min(20, entry.getInt("Stacks") + 1));
        }

        entry.putDouble("Amount", Math.max(entry.getDouble("Amount"), amount));
        entry.putLong("Until", Math.max(entry.getLong("Until"), until));
        entry.putUUID("Source", source.getUUID());
        all.put(type, entry);
        target.setData(ModAttachments.AFFLICTION, all);
    }

    public static boolean has(LivingEntity target, String type) {
        if (!target.hasData(ModAttachments.AFFLICTION)) {
            return false;
        }

        CompoundTag entry = target.getData(ModAttachments.AFFLICTION).getCompound(type);
        return entry.getLong("Until") > target.level().getGameTime();
    }

    @Nullable
    private static ServerPlayer source(ServerLevel level, CompoundTag entry) {
        UUID id = entry.hasUUID("Source") ? entry.getUUID("Source") : null;
        return id == null ? null : level.getServer().getPlayerList().getPlayer(id);
    }

    private static DamageSource harm(ServerLevel level, @Nullable ServerPlayer source) {
        return source == null ? level.damageSources().magic() : level.damageSources().source(DamageTypes.INDIRECT_MAGIC, source, source);
    }

    /**
     * Banishes a creature into a reflection for a while: it vanishes from sight and can neither act nor be harmed. It
     * comes back by itself when the time is up (see {@link #onEntityTick}), even across a restart.
     */
    public static void banish(Mob mob, int ticks) {
        mob.getPersistentData().putLong(BANISHED, mob.level().getGameTime() + ticks);
        mob.setInvisible(true);
        mob.setNoAi(true);
        mob.setInvulnerable(true);
    }

    private static void returnFromReflection(ServerLevel level, Mob mob) {
        mob.getPersistentData().remove(BANISHED);
        mob.setInvisible(false);
        mob.setNoAi(false);
        mob.setInvulnerable(false);
        Vfx.shards(level, mob.getBoundingBox().getCenter(), Vfx.Palette.STEEL, 14);
    }

    /** Once a second, each affliction does its harm; spent ones are cleared. Banished creatures come back on time. */
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || !(living.level() instanceof ServerLevel level) || living.tickCount % 20 != 0) {
            return;
        }

        if (living instanceof Mob mob && mob.getPersistentData().contains(BANISHED) && level.getGameTime() >= mob.getPersistentData().getLong(BANISHED)) {
            returnFromReflection(level, mob);
        }

        if (!living.hasData(ModAttachments.AFFLICTION)) {
            return;
        }

        CompoundTag all = living.getData(ModAttachments.AFFLICTION).copy();
        if (all.isEmpty()) {
            return;
        }

        long now = level.getGameTime();
        Vec3 at = living.getBoundingBox().getCenter();
        for (String type : all.getAllKeys().toArray(String[]::new)) {
            CompoundTag entry = all.getCompound(type);
            if (entry.getLong("Until") <= now) {
                all.remove(type);
                continue;
            }

            ServerPlayer source = source(level, entry);
            double amount = entry.getDouble("Amount");
            switch (type) {
                case BLEED -> {
                    living.invulnerableTime = 0;
                    living.hurt(harm(level, source), (float)amount);
                    Vfx.burst(level, at, Vfx.Palette.BLOOD, 4, 0.12);
                }
                case FESTER -> {
                    living.invulnerableTime = 0;
                    living.hurt(harm(level, source), (float)amount);
                    entry.putDouble("Amount", amount * 1.25);
                    Vfx.smoke(level, at, 0x3A1010, 4, living.getBbWidth() * 0.4);
                }
                case CORROSION -> {
                    living.invulnerableTime = 0;
                    living.hurt(harm(level, source), (float)(amount * entry.getInt("Stacks")));
                    Vfx.burst(level, at, Vfx.Palette.ACID, 3, 0.08);
                }
                case SOUL_FIRE -> {
                    living.invulnerableTime = 0;
                    living.hurt(harm(level, source), (float)amount);
                    Vfx.embers(level, at, Vfx.Palette.LIGHT, 5, living.getBbWidth() * 0.5);
                }
                case MARKED -> Vfx.glyph(level, at.add(0.0, living.getBbHeight() * 0.6, 0.0), Vfx.Palette.RUNE.bright);
                default -> {
                }
            }
        }

        living.setData(ModAttachments.AFFLICTION, all);
    }

    /** A marked creature takes more from its marker and the marker's allies. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasData(ModAttachments.AFFLICTION) || !(target.level() instanceof ServerLevel level) || !has(target, MARKED)) {
            return;
        }

        CompoundTag entry = target.getData(ModAttachments.AFFLICTION).getCompound(MARKED);
        ServerPlayer marker = source(level, entry);
        if (marker != null && event.getSource().getEntity() instanceof LivingEntity attacker && Targets.ally(attacker, marker)) {
            event.setAmount((float)(event.getAmount() * (1.0 + entry.getDouble("Amount"))));
        }
    }
}
