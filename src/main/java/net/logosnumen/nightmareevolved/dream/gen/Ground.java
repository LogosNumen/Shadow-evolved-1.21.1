package net.logosnumen.nightmareevolved.dream.gen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Laying down ordinary ground: bedrock at the bottom, stone (deepslate deep down), then soil and a surface. */
public final class Ground {
    public static final int SEA_LEVEL = 63;
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public static final BlockState STONE = Blocks.STONE.defaultBlockState();
    public static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    public static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    public static final BlockState WATER = Blocks.WATER.defaultBlockState();

    private Ground() {
    }

    /** Stone from the bottom of the world to a height, with bedrock under it all. */
    public static void stone(Column column, int top) {
        stone(column, top, true);
    }

    public static void stone(Column column, int top, boolean bedrock) {
        int deepTop = Math.min(top, -1);
        column.fill(Column.MIN_Y, deepTop, DEEPSLATE);
        if (top >= 0) {
            column.fill(0, top, STONE);
        }

        if (bedrock) {
            column.set(Column.MIN_Y, BEDROCK);
        }
    }

    /** Ground to a height: stone, then {@code depth} blocks of soil, the topmost of them the surface. */
    public static void lay(Column column, int top, BlockState soil, BlockState surface, int depth) {
        stone(column, top);
        if (depth > 0) {
            column.fill(top - depth + 1, top, soil);
            column.set(top, surface);
        }
    }

    /** Fills a column's empty places with water up to the sea, above its highest solid block. */
    public static void sea(Column column, int level) {
        int floor = column.topSolid();
        if (floor < level && floor > Column.MIN_Y + 4) {
            column.flood(floor + 1, level, WATER);
        }
    }
}
