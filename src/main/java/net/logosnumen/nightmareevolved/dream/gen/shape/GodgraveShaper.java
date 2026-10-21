package net.logosnumen.nightmareevolved.dream.gen.shape;

import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.gen.Column;
import net.logosnumen.nightmareevolved.dream.gen.Ground;
import net.logosnumen.nightmareevolved.dream.gen.Noise;
import net.logosnumen.nightmareevolved.dream.gen.Shaper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

/**
 * Godgrave's ground: the deadliest of its three roads, a twilit plain under a carpet of ash. The dead god's skeleton
 * that lies across it is built over this land (see {@code Godgrave}).
 */
public final class GodgraveShaper implements Shaper {
    private final Noise land;
    private final Noise drift;

    public GodgraveShaper(long seed) {
        this.land = new Noise(Noise.mix(seed, "godgrave.land"), 3);
        this.drift = new Noise(Noise.mix(seed, "godgrave.drift"), 1);
    }

    @Override
    public double ground(int x, int z, double edge) {
        return 68.0 + 3.0 * this.land.at(x / 180.0, z / 180.0) + 1.2 * this.land.at(x / 37.0, 2.0, z / 37.0);
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int top = Mth.floor(height);
        Ground.lay(column, top, Blocks.COARSE_DIRT.defaultBlockState(), DreamBlocks.ASH.get().defaultBlockState(), 4);
        column.fill(top - 2, top, DreamBlocks.ASH.get().defaultBlockState());
        int layers = (int)Math.round(3.0 + 3.0 * this.drift.at(column.x / 17.0, column.z / 17.0));
        if (layers > 0) {
            column.set(top + 1, DreamBlocks.ASH_LAYER.get().depth(layers));
        }
    }
}
