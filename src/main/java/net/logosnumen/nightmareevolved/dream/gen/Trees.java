package net.logosnumen.nightmareevolved.dream.gen;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Trees grown a column at a time. Each tree stands in a cell of a grid; a column looks at the cells around it and
 * paints whatever trunk or crown reaches it.
 */
public final class Trees {
    private Trees() {
    }

    /** The shape of a tree's crown. */
    public enum Crown {
        /** A rounded crown. */
        ROUND,
        /** A narrow cone, like a spruce. */
        CONE,
        /** A wide, flat crown on a tall bare trunk. */
        CANOPY,
        /** A crown that hangs down around the trunk. */
        WEEPING,
        /** No crown at all: a dead, broken trunk. */
        DEAD
    }

    public record Tree(int x, int z, int base, int height, int radius, BlockState log, BlockState leaves, Crown crown) {
    }

    /** Gives the tree growing in a grid cell, or none. */
    @FunctionalInterface
    public interface Planter {
        @Nullable
        Tree at(int cellX, int cellZ);
    }

    public static BlockState persistent(BlockState leaves) {
        return leaves.hasProperty(LeavesBlock.PERSISTENT) ? leaves.setValue(LeavesBlock.PERSISTENT, true) : leaves;
    }

    public static BlockState upright(BlockState log) {
        return log.hasProperty(RotatedPillarBlock.AXIS) ? log.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : log;
    }

    /** Paints into a column every tree of a grid (of {@code cell}-block cells) that reaches it. */
    public static void paint(Column column, int cell, Planter planter) {
        int cx = Math.floorDiv(column.x, cell);
        int cz = Math.floorDiv(column.z, cell);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Tree tree = planter.at(cx + dx, cz + dz);
                if (tree != null) {
                    paint(column, tree);
                }
            }
        }
    }

    public static void paint(Column column, Tree tree) {
        int dx = column.x - tree.x;
        int dz = column.z - tree.z;
        double d = Math.sqrt(dx * dx + dz * dz);
        int top = tree.base + tree.height;
        if (dx == 0 && dz == 0) {
            column.fill(tree.base, top - (tree.crown == Crown.DEAD ? 0 : 1), upright(tree.log));
        }

        if (tree.crown == Crown.DEAD || d > tree.radius + 0.5) {
            return;
        }

        BlockState leaves = persistent(tree.leaves);
        int r = tree.radius;
        switch (tree.crown) {
            case ROUND -> {
                int centre = top - r / 2;
                double reach = Math.sqrt(Math.max(0.0, r * r - d * d));
                for (int y = (int)Math.floor(centre - reach * 0.75); y <= (int)Math.ceil(centre + reach * 0.85); y++) {
                    column.setIfEmpty(y, leaves);
                }
            }
            case CONE -> {
                int bottom = top - tree.height * 2 / 3;
                for (int y = bottom; y <= top + 1; y++) {
                    double width = r * (1.0 - (double)(y - bottom) / (top + 2 - bottom));
                    if (d <= width + 0.3 && ((y - bottom) % 3 != 2 || d < width * 0.5)) {
                        column.setIfEmpty(y, leaves);
                    }
                }
            }
            case CANOPY -> {
                double fall = d / Math.max(1.0, r);
                int y0 = top - 1 - (int)(fall * 1.5);
                for (int y = y0; y <= top - (fall > 0.7 ? 1 : 0); y++) {
                    column.setIfEmpty(y, leaves);
                }
            }
            case WEEPING -> {
                int centre = top - 1;
                double reach = Math.sqrt(Math.max(0.0, r * r - d * d));
                int hang = (int)Math.round((d / r) * tree.height * 0.6);
                for (int y = centre - hang; y <= centre + (int)(reach * 0.5); y++) {
                    if (d > r * 0.55 || y >= centre - 1) {
                        column.setIfEmpty(y, leaves);
                    }
                }
            }
            default -> {
            }
        }
    }
}
