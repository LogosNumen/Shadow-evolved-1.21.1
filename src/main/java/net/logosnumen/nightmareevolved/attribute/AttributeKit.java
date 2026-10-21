package net.logosnumen.nightmareevolved.attribute;

import net.logosnumen.nightmareevolved.nightmare.Nightmares;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;

/** Small helpers the Attributes share. */
final class AttributeKit {
    private AttributeKit() {
    }

    /** Whether a player stands in darkness: little light from sky or torch where they are. */
    static boolean inDarkness(ServerPlayer player) {
        var pos = player.blockPosition().above();
        int sky = player.level().getBrightness(LightLayer.SKY, pos) - player.level().getSkyDarken();
        int block = player.level().getBrightness(LightLayer.BLOCK, pos);
        return Math.max(sky, block) <= 7;
    }

    /** Whether a player walks the dream side: inside a Nightmare (and, later, the Dream Realm). */
    static boolean dreaming(ServerPlayer player) {
        return Nightmares.isNightmare(player.level());
    }

    /** Foes close by that are hunting the player. */
    static int hunters(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius), m -> m.getTarget() == player).size();
    }

    static boolean low(LivingEntity target, float share) {
        return target.getHealth() <= target.getMaxHealth() * share;
    }
}
