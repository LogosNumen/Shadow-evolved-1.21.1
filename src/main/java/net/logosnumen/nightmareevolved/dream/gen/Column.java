package net.logosnumen.nightmareevolved.dream.gen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One column of the Dream Realm as it is built, bottom to top. The land is shaped here first, then the landmarks paint
 * over it, and only then is it written into the chunk. Empty entries are air.
 */
public final class Column {
    public static final int MIN_Y = -64;
    public static final int HEIGHT = 448;
    public static final int MAX_Y = MIN_Y + HEIGHT - 1;
    private final BlockState[] states = new BlockState[HEIGHT];
    public int x;
    public int z;
    /** The height the land was shaped to, before anything was built on it (may be below the world for open sky). */
    public double ground;

    public void reset(int x, int z) {
        this.x = x;
        this.z = z;
        this.ground = MIN_Y - 1;
        Arrays.fill(this.states, null);
    }

    @Nullable
    public BlockState get(int y) {
        return y < MIN_Y || y > MAX_Y ? null : this.states[y - MIN_Y];
    }

    public boolean solid(int y) {
        BlockState state = this.get(y);
        return state != null && !state.isAir() && state.getFluidState().isEmpty();
    }

    public void set(int y, @Nullable BlockState state) {
        if (y >= MIN_Y && y <= MAX_Y) {
            this.states[y - MIN_Y] = state;
        }
    }

    /** Sets a block only where there is nothing yet (air or open water's surface stays as it is otherwise). */
    public void setIfEmpty(int y, BlockState state) {
        BlockState here = this.get(y);
        if (here == null || here.isAir()) {
            this.set(y, state);
        }
    }

    /** Fills from one height to another, both inclusive, in either order. */
    public void fill(int from, int to, @Nullable BlockState state) {
        int lo = Math.max(MIN_Y, Math.min(from, to));
        int hi = Math.min(MAX_Y, Math.max(from, to));
        for (int y = lo; y <= hi; y++) {
            this.states[y - MIN_Y] = state;
        }
    }

    public void clear(int from, int to) {
        this.fill(from, to, null);
    }

    /** Replaces only the empty places in a span with a fluid or anything else that fills hollows. */
    public void flood(int from, int to, BlockState state) {
        int lo = Math.max(MIN_Y, Math.min(from, to));
        int hi = Math.min(MAX_Y, Math.max(from, to));
        for (int y = lo; y <= hi; y++) {
            BlockState here = this.states[y - MIN_Y];
            if (here == null || here.isAir()) {
                this.states[y - MIN_Y] = state;
            }
        }
    }

    /** The highest block that isn't air, or one below the world if the column is empty. */
    public int top() {
        for (int i = HEIGHT - 1; i >= 0; i--) {
            BlockState state = this.states[i];
            if (state != null && !state.isAir()) {
                return i + MIN_Y;
            }
        }

        return MIN_Y - 1;
    }

    /** The highest solid block (not air, not a fluid), or one below the world. */
    public int topSolid() {
        for (int i = HEIGHT - 1; i >= 0; i--) {
            BlockState state = this.states[i];
            if (state != null && !state.isAir() && state.getFluidState().isEmpty()) {
                return i + MIN_Y;
            }
        }

        return MIN_Y - 1;
    }

    /** The highest solid block at or below a height, or one below the world. */
    public int solidBelow(int y) {
        for (int i = Math.min(HEIGHT - 1, y - MIN_Y); i >= 0; i--) {
            BlockState state = this.states[i];
            if (state != null && !state.isAir() && state.getFluidState().isEmpty()) {
                return i + MIN_Y;
            }
        }

        return MIN_Y - 1;
    }
}
