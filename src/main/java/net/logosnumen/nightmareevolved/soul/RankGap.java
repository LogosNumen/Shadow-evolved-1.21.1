package net.logosnumen.nightmareevolved.soul;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

/**
 * The gap between ranks. A soul of a higher rank is a different order of being from one below it: blows from beneath it
 * barely mark it, and from far enough below they cannot even move it, while its own blows land with the weight of its
 * rank. Players and Nightmare Creatures stand on one ladder (a Sleeper with Dormant creatures, a Saint with Corrupted
 * ones). A player struck from below is guarded most of all; a creature struck by a lower player still takes enough
 * that the bold can fight upward.
 */
public final class RankGap {
    /** From this many ranks below, an attacker can no longer knock its target back. */
    public static final int UNMOVED = 3;
    /** The share of a blow a player takes from one, two and three or more ranks below. */
    private static final float[] PLAYER_GUARD = {0.6F, 0.3F, 0.12F};
    /** How much each rank of the gap takes from (or adds to) a blow on a creature, up to three ranks. */
    private static final float STEP = 0.25F;
    private static final int MAX_GAP = 3;

    private RankGap() {
    }

    /** A soul's place on the shared ladder, or -1 for things outside it (animals, mundane monsters). */
    public static int ladder(LivingEntity entity) {
        if (entity instanceof Player player) {
            return Math.max(0, Soul.of(player).rank().ladder);
        }

        return Creatures.ofNightmare(entity) ? Creatures.rankOf(entity).ordinal() : -1;
    }

    /** How many ranks the defender stands above the attacker (negative when below); 0 when either is off the ladder. */
    public static int gap(LivingEntity attacker, LivingEntity defender) {
        int a = ladder(attacker);
        int d = ladder(defender);
        return attacker == defender || a < 0 || d < 0 ? 0 : d - a;
    }

    /** What a blow from {@code attacker} is multiplied by when it lands on {@code defender}. */
    public static float multiplier(LivingEntity attacker, LivingEntity defender) {
        int gap = gap(attacker, defender);
        if (gap > 0) {
            if (defender instanceof Player) {
                return PLAYER_GUARD[Math.min(gap, PLAYER_GUARD.length) - 1];
            }

            return (float)Math.pow(1.0F - STEP, Math.min(gap, MAX_GAP));
        }

        return gap < 0 ? (float)Math.pow(1.0F + STEP, Math.min(-gap, MAX_GAP)) : 1.0F;
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            event.setAmount(event.getAmount() * multiplier(attacker, event.getEntity()));
        }
    }

    /** A blow from far enough below lands, but cannot move a higher soul. */
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity defender = event.getEntity();
        LivingEntity attacker = attacker(defender);
        if (attacker != null && gap(attacker, defender) >= UNMOVED) {
            event.setCanceled(true);
        }
    }

    @Nullable
    private static LivingEntity attacker(LivingEntity defender) {
        LivingEntity last = defender.getLastHurtByMob();
        return last != null && defender.tickCount - defender.getLastHurtByMobTimestamp() <= 1 ? last : null;
    }
}
