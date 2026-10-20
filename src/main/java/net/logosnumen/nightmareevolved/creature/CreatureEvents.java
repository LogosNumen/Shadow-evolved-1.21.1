package net.logosnumen.nightmareevolved.creature;

import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.item.ModItems;
import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * How Nightmare Creatures fight and what they leave: the gap in rank between attacker and defender changes how much a
 * blow lands, slain creatures leave soul shards of their rank, and (until Gates and the Dream Realm bring creatures of
 * their own) a few monsters born in the waking world are touched by the Nightmare, stronger the farther from the
 * world's centre they walk.
 */
public final class CreatureEvents {
    /** How likely a monster born in the waking world is to be touched by the Nightmare. */
    private static final float TOUCHED = 0.08F;

    private CreatureEvents() {
    }

    /**
     * Each rank a defender stands above its attacker takes off two fifths of what lands; each rank below adds a
     * quarter. Anything outside the ladder (Aspirants, ordinary creatures) is untouched by it.
     */
    public static float gapMultiplier(int attacker, int defender) {
        if (attacker < 0 || defender < 0 || attacker == defender) {
            return 1.0F;
        }

        int gap = defender - attacker;
        return gap > 0 ? (float)Math.pow(0.6, gap) : 1.0F + 0.25F * -gap;
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            float multiplier = gapMultiplier(Soul.ladder(attacker), Soul.ladder(event.getEntity()));
            if (multiplier != 1.0F) {
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }

    /** A slain Nightmare Creature leaves soul shards of its rank: a guardian many, a colossus a great many. */
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!Creatures.ofNightmare(entity) || entity.getLastHurtByMob() == null && event.getSource().getEntity() == null) {
            return;
        }

        CreatureRank rank = Creatures.rankOf(entity);
        RandomSource random = entity.getRandom();
        int count;
        if (entity instanceof ColossusEntity colossus) {
            count = 7 + (colossus.variant().elder() ? 4 : 0);
        } else if (Creatures.profile(entity).guardian()) {
            count = 3 + rank.ordinal();
        } else {
            count = 1 + (random.nextFloat() < 0.3F ? 1 : 0);
        }

        ItemStack shards = ModItems.shard(rank, count);
        event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), shards));
    }

    /** A few monsters born in the waking world are touched by the Nightmare. */
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (event.getSpawnType() != MobSpawnType.NATURAL || !(mob instanceof Enemy) || mob instanceof ColossusEntity || Creatures.ofNightmare(mob)
            || Nightmares.isNightmare(mob.level()) || mob.getRandom().nextFloat() >= TOUCHED) {
            return;
        }

        double far = Math.sqrt(mob.getX() * mob.getX() + mob.getZ() * mob.getZ());
        CreatureRank rank = far < 1500 ? CreatureRank.DORMANT : far < 4000 ? CreatureRank.AWAKENED : far < 8000 ? CreatureRank.FALLEN : CreatureRank.CORRUPTED;
        CreatureClass creatureClass = CreatureClass.values()[mob.getRandom().nextInt(3)];
        Creatures.raise(mob, rank, creatureClass, false, -1L);
    }
}
