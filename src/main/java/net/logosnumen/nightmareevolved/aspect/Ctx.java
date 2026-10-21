package net.logosnumen.nightmareevolved.aspect;

import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * What an ability works with: who is using it, where, their soul, the Aspect and ability in play, and {@code power},
 * how hard it hits at the soul's Rank (1 for a Sleeper with a Dormant Aspect, rising with each Rank).
 */
public record Ctx(ServerPlayer player, ServerLevel level, SoulData soul, Aspect aspect, Ability ability, double power) {
    public Vfx.Palette palette() {
        return this.aspect.palette();
    }

    public long now() {
        return this.level.getGameTime();
    }
}
