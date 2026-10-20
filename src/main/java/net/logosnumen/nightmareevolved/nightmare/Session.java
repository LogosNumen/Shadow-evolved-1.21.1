package net.logosnumen.nightmareevolved.nightmare;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

/** One player's trial while it runs: its arena, what stage it is at, and the creatures still standing. */
final class Session {
    enum State {
        /** The moment after arriving, before anything comes. */
        GATHERING,
        WAVES,
        GUARDIAN,
        /** Crossing to the Gateway. */
        RUNNING,
        /** Won; the Spell is about to send the soul home, raised. */
        ENDING
    }

    final UUID player;
    final Trial trial;
    final Arenas.Layout layout;
    final long id;
    final Set<UUID> creatures = new HashSet<>();
    final ServerBossEvent bar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    State state = State.GATHERING;
    int wave;
    int waveSize;
    long nextAt;
    @Nullable
    UUID guardian;

    Session(UUID player, Trial trial, Arenas.Layout layout, long id) {
        this.player = player;
        this.trial = trial;
        this.layout = layout;
        this.id = id;
    }
}
