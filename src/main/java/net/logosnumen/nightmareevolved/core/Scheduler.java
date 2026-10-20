package net.logosnumen.nightmareevolved.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Runs work on later server ticks: once after a delay, or once a tick for a number of ticks. Effects use it to unfold
 * over time, and attacks to land their later blows. Work runs at the end of its tick, and everything pending is dropped
 * when the server stops.
 */
public final class Scheduler {
    private record Task(long due, Runnable work) {
    }

    private static final class Repeat {
        private final IntConsumer step;
        private final long start;
        private final int ticks;
        private int done;

        private Repeat(IntConsumer step, long start, int ticks) {
            this.step = step;
            this.start = start;
            this.ticks = ticks;
        }
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static final List<Repeat> REPEATS = new ArrayList<>();
    /** The tick the server is in, counted at its start. */
    private static long now;

    private Scheduler() {
    }

    /** Runs {@code work} at the end of the tick {@code delayTicks} ticks from this one (at least the next). */
    public static void schedule(Runnable work, int delayTicks) {
        TASKS.add(new Task(now + Math.max(1, delayTicks), work));
    }

    /** Runs {@code step} once a tick for {@code ticks} ticks, starting next tick, passing the tick number (0 first). */
    public static void scheduleRecurring(IntConsumer step, int ticks) {
        if (ticks > 0) {
            REPEATS.add(new Repeat(step, now + 1, ticks));
        }
    }

    public static void onServerTickStart(ServerTickEvent.Pre event) {
        now++;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        // Work may schedule more work; take what is due now and run it after.
        List<Task> due = new ArrayList<>();
        TASKS.removeIf(task -> {
            if (task.due() <= now) {
                due.add(task);
                return true;
            }

            return false;
        });
        List<Repeat> repeats = new ArrayList<>();
        for (Repeat repeat : REPEATS) {
            if (repeat.start <= now) {
                repeats.add(repeat);
            }
        }

        REPEATS.removeIf(repeat -> repeat.start <= now && repeat.done + 1 >= repeat.ticks);
        due.forEach(task -> task.work().run());
        for (Repeat repeat : repeats) {
            repeat.step.accept(repeat.done++);
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
        REPEATS.clear();
    }
}
