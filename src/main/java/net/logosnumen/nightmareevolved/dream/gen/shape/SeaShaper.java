package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The realm's two seas. The Stormsea is endless rolling water over a deep floor, broken by rare spires of black rock.
 * The Bone Sea is shallower and still, its water poison, with the ribs of colossal dead things arching out of it.
 */
public final class SeaShaper implements Shaper {
    private final long seed;
    private final boolean bones;
    private final Noise floor;
    private final Noise detail;
    private final Noise spires;

    public SeaShaper(long seed, boolean bones) {
        this.seed = seed;
        this.bones = bones;
        String name = bones ? "bone_sea" : "stormsea";
        this.floor = new Noise(Noise.mix(seed, name + ".floor"), 3);
        this.detail = new Noise(Noise.mix(seed, name + ".detail"), 2);
        this.spires = new Noise(Noise.mix(seed, name + ".spires"), 2);
    }

    @Override
    public double ground(int x, int z, double edge) {
        double deep = Noise.smooth(edge / 260.0);
        double floor = this.bones ? 44.0 + 5.0 * this.floor.at(x / 220.0, z / 220.0) : 36.0 + 9.0 * this.floor.at(x / 320.0, z / 320.0);
        return Mth.lerp(deep, 58.0, floor) + 2.0 * this.detail.at(x / 50.0, z / 50.0);
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int top = Mth.floor(height);
        BlockState bed = this.bones ? Blocks.MUD.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
        Ground.lay(column, top, Blocks.SAND.defaultBlockState(), bed, 3);
        if (this.bones) {
            this.ribs(column, top);
        } else {
            this.spires(column, top);
        }

        Ground.sea(column, Ground.SEA_LEVEL);
    }

    /** Rare spires of black rock standing out of the Stormsea. */
    private void spires(Column column, int top) {
        int cell = 280;
        int ci = Math.floorDiv(column.x, cell);
        int cj = Math.floorDiv(column.z, cell);
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int i = ci + di;
                int j = cj + dj;
                if (Noise.hash(this.seed, i, j, 141) > 0.2) {
                    continue;
                }

                double sx = i * cell + cell * Noise.hash(this.seed, i, j, 142);
                double sz = j * cell + cell * Noise.hash(this.seed, i, j, 143);
                double r = 6.0 + 16.0 * Noise.hash(this.seed, i, j, 144);
                double dx = column.x - sx;
                double dz = column.z - sz;
                double d = Math.sqrt(dx * dx + dz * dz) / (r * (1.0 + 0.25 * this.spires.at(column.x / 9.0, column.z / 9.0)));
                if (d < 1.0) {
                    int peak = Ground.SEA_LEVEL + 8 + (int)(44.0 * Noise.hash(this.seed, i, j, 145));
                    int y = top + (int)((peak - top) * Math.pow(1.0 - d, 0.7));
                    column.fill(top, y, Blocks.DEEPSLATE.defaultBlockState());
                    column.set(y, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
                }
            }
        }
    }

    /** The ribs of colossal dead things, arching up out of the poisoned water. */
    private void ribs(Column column, int top) {
        int cell = 160;
        int ci = Math.floorDiv(column.x, cell);
        int cj = Math.floorDiv(column.z, cell);
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int i = ci + di;
                int j = cj + dj;
                if (Noise.hash(this.seed, i, j, 151) > 0.4) {
                    continue;
                }

                double bx = i * cell + cell * Noise.hash(this.seed, i, j, 152);
                double bz = j * cell + cell * Noise.hash(this.seed, i, j, 153);
                double angle = Noise.hash(this.seed, i, j, 154) * Math.PI;
                double radius = 18.0 + 26.0 * Noise.hash(this.seed, i, j, 155);
                double along = (column.x - bx) * Math.cos(angle) + (column.z - bz) * Math.sin(angle);
                double across = -(column.x - bx) * Math.sin(angle) + (column.z - bz) * Math.cos(angle);
                if (Math.abs(across) > 2.0 || Math.abs(along) > radius + 2.0) {
                    continue;
                }

                double base = Ground.SEA_LEVEL - 12;
                double outer = radius + 2.0;
                double inner = radius - 1.5;
                double hiOuter = Math.sqrt(Math.max(0.0, outer * outer - along * along));
                double hiInner = Math.sqrt(Math.max(0.0, inner * inner - along * along));
                column.fill((int)(base + hiInner), (int)(base + hiOuter), Blocks.BONE_BLOCK.defaultBlockState());
            }
        }
    }
}
