package net.logosnumen.nightmareevolved.dream.gen.landmark;

import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Streets of buildings laid out on a grid, painted a column at a time: lots separated by streets, a building on each lot
 * with walls, floors, window slits and a door onto the street, ruined as much as the town's age asks for.
 */
public final class Town {
    /** How a town's buildings look. */
    public record Style(BlockState wall, BlockState trim, BlockState floor, BlockState roof, BlockState street, int lot, int gap, int minStoreys,
        int maxStoreys, double ruin) {
        public static Style of(BlockState wall, BlockState trim, BlockState floor, BlockState roof, BlockState street, int minStoreys, int maxStoreys,
            double ruin) {
            return new Style(wall, trim, floor, roof, street, 13, 4, minStoreys, maxStoreys, ruin);
        }
    }

    private static final int STOREY = 5;

    private Town() {
    }

    /**
     * Paints the town into a column whose ground is at {@code ground}. Returns false where the column is a street.
     *
     * @param ox the grid's origin
     * @param oz the grid's origin
     */
    public static boolean paint(Column column, int ox, int oz, int ground, long seed, Style style) {
        int pitch = style.lot + style.gap;
        int lx = Math.floorMod(column.x - ox, pitch);
        int lz = Math.floorMod(column.z - oz, pitch);
        int i = Math.floorDiv(column.x - ox, pitch);
        int j = Math.floorDiv(column.z - oz, pitch);
        if (lx >= style.lot || lz >= style.lot) {
            column.set(ground, style.street);
            return false;
        }

        double empty = Noise.hash(seed, i, j, 1);
        if (empty < 0.12) {
            // An empty lot: a yard, or rubble where a house fell long ago.
            column.set(ground, style.street);
            if (Noise.hash(seed, column.x, column.z, 2) < style.ruin * 0.3) {
                column.set(ground + 1, style.wall);
            }

            return true;
        }

        // The building's footprint, set in a little from the lot's edge.
        int inset = 1 + (int)(Noise.hash(seed, i, j, 3) * 2);
        int x0 = inset;
        int z0 = inset;
        int x1 = style.lot - 1 - (int)(Noise.hash(seed, i, j, 4) * 2);
        int z1 = style.lot - 1 - (int)(Noise.hash(seed, i, j, 5) * 2);
        column.set(ground, style.floor);
        if (lx < x0 || lx > x1 || lz < z0 || lz > z1) {
            return true;
        }

        int storeys = style.minStoreys + (int)(Noise.hash(seed, i, j, 6) * (style.maxStoreys - style.minStoreys + 1));
        int height = storeys * STOREY;
        // Ruin: the walls stand to a ragged height, lower the older the town.
        double age = Noise.hash(seed, column.x >> 1, column.z >> 1, 7);
        int standing = height;
        if (age < style.ruin) {
            standing = (int)(height * (0.25 + 0.75 * age / Math.max(0.01, style.ruin)));
        }

        boolean edge = lx == x0 || lx == x1 || lz == z0 || lz == z1;
        boolean corner = (lx == x0 || lx == x1) && (lz == z0 || lz == z1);
        if (edge) {
            BlockState wall = corner ? style.trim : style.wall;
            column.fill(ground + 1, ground + standing, wall);
            // A door onto the street, in the middle of the side that faces it.
            int mid = (x0 + x1) / 2;
            int midZ = (z0 + z1) / 2;
            boolean door = (lz == z1 && lx == mid) || (lx == x1 && lz == midZ && Noise.hash(seed, i, j, 8) < 0.5);
            if (door) {
                column.clear(ground + 1, ground + 2);
            }

            // Window slits on every floor.
            boolean window = !corner && ((lx + lz) % 3 == 1);
            for (int s = 0; s < storeys; s++) {
                int y = ground + s * STOREY + 2;
                if (window && y + 1 <= ground + standing && s > 0) {
                    column.clear(y, y + 1);
                }
            }

            if (standing == height) {
                column.set(ground + height + 1, (lx + lz) % 2 == 0 ? style.trim : null);
            }
        } else {
            column.clear(ground + 1, ground + height);
            for (int s = 1; s < storeys; s++) {
                int y = ground + s * STOREY;
                if (y <= ground + standing && Noise.hash(seed, column.x, column.z, 9 + s) > style.ruin * 0.6) {
                    column.set(y, style.floor);
                }
            }

            if (standing == height) {
                column.set(ground + height, style.roof);
            } else if (Noise.hash(seed, column.x, column.z, 20) < 0.3) {
                column.set(ground + 1, style.wall);
            }
        }

        return true;
    }

    /** A weathered stone town, much of it fallen. */
    public static Style ruinedStone(double ruin) {
        return Style.of(Blocks.STONE_BRICKS.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Blocks.STONE.defaultBlockState(),
            Blocks.STONE_BRICK_SLAB.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(), 1, 4, ruin);
    }
}
