package net.logosnumen.nightmareevolved.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Builds the ground a trial is fought on, out in the Nightmare's void: a broken island adrift in the dark, tapering
 * away beneath, strewn with fallen pillars, half walls and lights in its own stone.
 */
public final class Arenas {
    public static final int GROUND = 64;

    /** Where a built arena's player arrives, and how far its ground reaches. */
    public record Layout(BlockPos centre, Vec3 arrival, int radius) {
    }

    private Arenas() {
    }

    public static Layout build(ServerLevel level, Trial trial, BlockPos centre, RandomSource random) {
        return island(level, trial, centre, random);
    }

    private static float noise(int x, int z, int seed) {
        int h = x * 374761393 + z * 668265263 + seed * 1442695041;
        h = (h ^ h >>> 13) * 1274126177;
        return ((h ^ h >>> 16) & 0xFFFF) / 65536.0F;
    }

    private static void put(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    /** The ground's surface block at a column, mixing in patches of a second block. */
    private static BlockState surface(Trial.Theme theme, int x, int z, int seed) {
        return noise(x / 3, z / 3, seed) > 0.62F ? theme.patch().defaultBlockState() : theme.ground().defaultBlockState();
    }

    private static Layout island(ServerLevel level, Trial trial, BlockPos centre, RandomSource random) {
        Trial.Theme theme = trial.theme;
        int radius = 22 + trial.depth() * 2;
        int seed = random.nextInt();
        for (int x = -radius - 3; x <= radius + 3; x++) {
            for (int z = -radius - 3; z <= radius + 3; z++) {
                float edge = radius - 2.5F + noise(x / 4, z / 4, seed) * 5.0F;
                float d = Mth.sqrt(x * x + z * z);
                if (d > edge) {
                    continue;
                }

                int depth = 1 + Math.min(14, Math.round((edge - d) * 0.55F + noise(x, z, seed + 1) * 2.0F));
                int top = GROUND + (noise(x / 6, z / 6, seed + 2) > 0.8F ? 1 : 0);
                put(level, centre.offset(x, top - GROUND, z), surface(theme, x, z, seed));
                for (int y = 1; y <= depth; y++) {
                    put(level, centre.offset(x, top - GROUND - y, z), theme.deep().defaultBlockState());
                }
            }
        }

        ruins(level, theme, centre, radius, 6 + trial.depth(), random);
        return new Layout(centre, Vec3.atBottomCenterOf(centre.above()), radius);
    }

    /** Fallen pillars and broken walls, some crowned with a light, kept clear of the arena's middle. */
    private static void ruins(ServerLevel level, Trial.Theme theme, BlockPos centre, int radius, int count, RandomSource random) {
        BlockState ruin = theme.ruin().defaultBlockState();
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Mth.TWO_PI;
            double r = 7.0 + random.nextDouble() * (radius - 10.0);
            BlockPos base = centre.offset(Mth.floor(Math.cos(a) * r), 1, Mth.floor(Math.sin(a) * r));
            if (random.nextBoolean()) {
                int height = 2 + random.nextInt(5);
                for (int y = 0; y < height; y++) {
                    put(level, base.above(y), ruin);
                }

                if (random.nextFloat() < 0.6F) {
                    put(level, base.above(height), theme.light().defaultBlockState());
                }
            } else {
                boolean alongX = random.nextBoolean();
                int length = 3 + random.nextInt(4);
                for (int s = 0; s < length; s++) {
                    int height = 1 + random.nextInt(3);
                    for (int y = 0; y < height; y++) {
                        put(level, alongX ? base.offset(s, y, 0) : base.offset(0, y, s), ruin);
                    }
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            double a = i * Mth.TWO_PI / 6 + random.nextDouble() * 0.4;
            put(level, centre.offset(Mth.floor(Math.cos(a) * 5), 1, Mth.floor(Math.sin(a) * 5)), theme.light().defaultBlockState());
        }
    }

    private static void disc(ServerLevel level, Trial.Theme theme, BlockPos at, int radius, int seed) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    put(level, at.offset(x, 0, z), surface(theme, at.getX() + x, at.getZ() + z, seed));
                    int depth = 1 + Math.max(0, radius - Mth.floor(Mth.sqrt(x * x + z * z))) / 2;
                    for (int y = 1; y <= depth; y++) {
                        put(level, at.offset(x, -y, z), theme.deep().defaultBlockState());
                    }
                }
            }
        }
    }
}
