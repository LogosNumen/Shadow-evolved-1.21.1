package net.logosnumen.nightmareevolved.soulsea;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The parts of a Soul Sea built of blocks, raised (or taken down) as the soul changes:
 * <ul>
 * <li>A Saint's soul surpasses its vessel: the Citadel bound to it stands in the middle of its sea, as a likeness.</li>
 * <li>Effie's soul raises an island out of her sea, green and growing.</li>
 * </ul>
 */
public final class SeaBuilder {
    /** The Citadel's likeness fits inside the innermost ring of Memories. */
    private static final int CITADEL = 3;
    private static final int ISLAND = 5;

    private SeaBuilder() {
    }

    public static void raise(ServerPlayer player, ServerLevel sea, int region) {
        SoulData soul = Soul.of(player);
        SeaRegions regions = SeaRegions.get(player.server);
        CompoundTag built = regions.built(region);
        int cx = (int)Math.floor(SeaRegions.centerX(region));
        int cz = (int)Math.floor(SeaRegions.centerZ(region));
        int y = (int)SoulSea.SURFACE_Y;

        String citadel = soul.rank().atLeast(Rank.SAINT) ? soul.citadel() : "";
        if (!citadel.equals(built.getString("Citadel"))) {
            clear(sea, cx, y, cz, CITADEL + 1, 16);
            if (!citadel.isEmpty()) {
                citadel(sea, cx, y, cz, citadel);
            }

            regions.markBuilt(region, "Citadel", citadel);
        }

        String island = "raised_by_wolves".equals(soul.aspect()) ? "garden" : "";
        int ix = cx - 14;
        int iz = cz - 8;
        if (!island.equals(built.getString("Island"))) {
            clear(sea, ix, y, iz, ISLAND + 1, 9);
            if (!island.isEmpty()) {
                island(sea, ix, y, iz);
            }

            regions.markBuilt(region, "Island", island);
        }
    }

    private static void clear(ServerLevel sea, int cx, int y, int cz, int radius, int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= height; dy++) {
                    sea.setBlock(new BlockPos(cx + dx, y + dy, cz + dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** What a Citadel's likeness is built of, by the Citadel: white stone for Bright Castle, ivory for the Ivory Tower, and so on. */
    private record Stone(BlockState wall, BlockState trim, BlockState roof, BlockState light) {
    }

    private static Stone stone(String citadel) {
        return switch (citadel) {
            case "bright_castle" -> new Stone(Blocks.QUARTZ_BRICKS.defaultBlockState(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(),
                Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState());
            case "ravenheart" -> new Stone(Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.OXIDIZED_COPPER.defaultBlockState(),
                Blocks.WARPED_WART_BLOCK.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState());
            case "crimson_spire" -> new Stone(Blocks.RED_NETHER_BRICKS.defaultBlockState(), Blocks.CHISELED_RED_SANDSTONE.defaultBlockState(),
                Blocks.NETHER_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState());
            case "ivory_tower" -> new Stone(DreamBlocks.IVORY.get().defaultBlockState(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(),
                DreamBlocks.IVORY.get().defaultBlockState(), Blocks.END_ROD.defaultBlockState());
            case "sanctuary_of_noctis" -> new Stone(DreamBlocks.WHITE_JADE.get().defaultBlockState(), Blocks.CALCITE.defaultBlockState(),
                Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState());
            case "night_temple" -> new Stone(DreamBlocks.EBONY.get().defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                DreamBlocks.BLACK_JADE.get().defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState());
            case "black_skull" -> new Stone(Blocks.BLACKSTONE.defaultBlockState(), Blocks.BONE_BLOCK.defaultBlockState(),
                Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
            case "vanishing_lake" -> new Stone(Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), DreamBlocks.RED_MOSS.get().defaultBlockState(),
                Blocks.DARK_OAK_PLANKS.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
            case "red_hill" -> new Stone(Blocks.RED_SANDSTONE.defaultBlockState(), Blocks.CUT_RED_SANDSTONE.defaultBlockState(),
                Blocks.RED_STAINED_GLASS.defaultBlockState(), Blocks.GLOWSTONE.defaultBlockState());
            case "bastion" -> new Stone(Blocks.STONE_BRICKS.defaultBlockState(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
                Blocks.BLUE_TERRACOTTA.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
            case "sorrow" -> new Stone(Blocks.SMOOTH_STONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(),
                Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState());
            case "rivergate" -> new Stone(Blocks.STONE_BRICKS.defaultBlockState(), Blocks.PRISMARINE_BRICKS.defaultBlockState(),
                Blocks.DARK_PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState());
            case "night_garden" -> new Stone(Blocks.DARK_OAK_PLANKS.defaultBlockState(), Blocks.DARK_OAK_LOG.defaultBlockState(),
                Blocks.AZALEA_LEAVES.defaultBlockState(), Blocks.OCHRE_FROGLIGHT.defaultBlockState());
            case "black_obelisk" -> new Stone(Blocks.OBSIDIAN.defaultBlockState(), Blocks.CRYING_OBSIDIAN.defaultBlockState(),
                Blocks.OBSIDIAN.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState());
            default -> new Stone(Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                Blocks.DEEPSLATE_TILES.defaultBlockState(), Blocks.LANTERN.defaultBlockState());
        };
    }

    /** A small keep: a walled court with a gate to the south, and a tower at its heart with a light at the top. */
    private static void citadel(ServerLevel sea, int cx, int y, int cz, String name) {
        Stone stone = stone(name);
        for (int dx = -CITADEL; dx <= CITADEL; dx++) {
            for (int dz = -CITADEL; dz <= CITADEL; dz++) {
                boolean edge = Math.abs(dx) == CITADEL || Math.abs(dz) == CITADEL;
                set(sea, cx + dx, y, cz + dz, stone.trim);
                if (edge) {
                    boolean gate = dz == CITADEL && Math.abs(dx) <= 0;
                    for (int h = 1; h <= 3; h++) {
                        if (!(gate && h <= 2)) {
                            set(sea, cx + dx, y + h, cz + dz, stone.wall);
                        }
                    }

                    if ((dx + dz) % 2 == 0) {
                        set(sea, cx + dx, y + 4, cz + dz, stone.wall);
                    }
                }
            }
        }

        for (int h = 1; h <= 9; h++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean window = h % 3 == 2 && dx == 0 && dz != 0;
                    set(sea, cx + dx, y + h, cz + dz, window ? Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState() : stone.wall);
                }
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(sea, cx + dx, y + 10, cz + dz, stone.roof);
            }
        }

        set(sea, cx, y + 11, cz, stone.roof);
        set(sea, cx, y + 12, cz, stone.light);
    }

    /** Effie's island: a green mound rising out of the still water, with a tree and something growing. */
    private static void island(ServerLevel sea, int cx, int y, int cz) {
        for (int dx = -ISLAND; dx <= ISLAND; dx++) {
            for (int dz = -ISLAND; dz <= ISLAND; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > ISLAND + 0.3) {
                    continue;
                }

                int top = d < 2.0 ? 2 : d < 4.0 ? 1 : 0;
                for (int h = 0; h < top; h++) {
                    set(sea, cx + dx, y + h, cz + dz, Blocks.DIRT.defaultBlockState());
                }

                set(sea, cx + dx, y + top, cz + dz, Blocks.GRASS_BLOCK.defaultBlockState());
                if (d > 1.5 && (dx * 7 + dz * 3) % 5 == 0) {
                    set(sea, cx + dx, y + top + 1, cz + dz, ((dx + dz) & 1) == 0 ? Blocks.POPPY.defaultBlockState() : Blocks.CORNFLOWER.defaultBlockState());
                } else if (d > 1.5 && (dx * 5 - dz * 2) % 7 == 0) {
                    set(sea, cx + dx, y + top + 1, cz + dz, Blocks.SHORT_GRASS.defaultBlockState());
                }
            }
        }

        int ground = y + 3;
        for (int h = 0; h < 5; h++) {
            set(sea, cx, ground + h, cz, Blocks.OAK_LOG.defaultBlockState());
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int h = 3; h <= 6; h++) {
                    int spread = h >= 6 ? 1 : 2;
                    if (Math.abs(dx) <= spread && Math.abs(dz) <= spread && !(dx == 0 && dz == 0 && h < 5)
                        && !(Math.abs(dx) == 2 && Math.abs(dz) == 2)) {
                        set(sea, cx + dx, ground + h, cz + dz, Blocks.OAK_LEAVES.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
                    }
                }
            }
        }
    }

    private static void set(ServerLevel sea, int x, int y, int z, BlockState state) {
        sea.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
