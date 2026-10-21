package net.logosnumen.nightmareevolved.flaw;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/** Small helpers the Flaws share. */
final class FlawKit {
    private FlawKit() {
    }

    /** Pain that hurts but never kills: it stops a heart short of death. */
    static void pain(ServerPlayer player, float amount) {
        float allowed = Math.min(amount, player.getHealth() - 1.0F);
        if (allowed > 0.0F) {
            player.invulnerableTime = 0;
            player.hurt(player.damageSources().magic(), allowed);
        } else {
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.6F, 0.7F);
        }
    }

    static void say(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("flaw.nightmare_evolved.notice." + key, args), true);
    }

    static boolean inSunlight(Player player) {
        return player.level().isDay() && !player.level().isRaining() && player.level().canSeeSky(player.blockPosition().above())
            && player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, player.blockPosition().above()) >= 14;
    }
}
