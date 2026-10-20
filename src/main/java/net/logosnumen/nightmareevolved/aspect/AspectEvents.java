package net.logosnumen.nightmareevolved.aspect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** The Flaws that weigh on a soul in a fight. Leaden's slowness is part of the soul's stats instead. */
public final class AspectEvents {
    /** Light at or above this counts as bright, for a Light-Shy soul. */
    private static final int BRIGHT = 12;

    private AspectEvents() {
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            event.setAmount(event.getAmount() * outgoing(attacker));
        }

        if (event.getEntity() instanceof ServerPlayer victim && Aspects.hasFlaw(victim, Aspect.Flaw.BRITTLE)) {
            event.setAmount(event.getAmount() * 7.0F / 6.0F);
        }
    }

    /** How much of their strength a soul's Flaw lets into their blows here and now. */
    static float outgoing(ServerPlayer player) {
        Aspect aspect = Aspects.of(player);
        if (aspect == null) {
            return 1.0F;
        }

        return switch (aspect.flaw()) {
            case LIGHT_SHY -> player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition())) >= BRIGHT ? 0.8F : 1.0F;
            case DOUSED -> player.isInWaterRainOrBubble() ? 0.75F : 1.0F;
            default -> 1.0F;
        };
    }
}
