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
 * The Glass Hell: a vast plain of polished glass, flat and smooth for the most part, with tall ridges and deep
 * crevasses. Under the clear surface run countless tunnels down into the dark, where the hive lives.
 */
public final class GlassHellShaper implements Shaper {
    /** How thick the glass lies over the stone. */
    public static final int CRUST = 5;
    private final Noise plain;
    private final Noise ridges;
    private final Noise crevasses;
    private final Noise tunnelsA;
    private final Noise tunnelsB;
    private final Noise tint;
    private final BlockState glass = Blocks.GLASS.defaultBlockState();
    private final BlockState tinted = Blocks.TINTED_GLASS.defaultBlockState();
    private final BlockState under = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
    private final BlockState hive = Blocks.CUT_SANDSTONE.defaultBlockState();

    public GlassHellShaper(long seed) {
        this.plain = new Noise(Noise.mix(seed, "glass.plain"), 2);
        this.ridges = new Noise(Noise.mix(seed, "glass.ridges"), 2);
        this.crevasses = new Noise(Noise.mix(seed, "glass.crevasses"), 2);
        this.tunnelsA = new Noise(Noise.mix(seed, "glass.tunnels.a"), 2);
        this.tunnelsB = new Noise(Noise.mix(seed, "glass.tunnels.b"), 2);
        this.tint = new Noise(Noise.mix(seed, "glass.tint"), 1);
    }

    @Override
    public double ground(int x, int z, double edge) {
        double h = 72.0 + 1.5 * this.plain.at(x / 220.0, z / 220.0);
        double ridge = this.ridges.ridge(x / 270.0, z / 270.0);
        if (ridge > 0.9) {
            h += 15.0 * Noise.smooth((ridge - 0.9) / 0.1);
        }

        double crevasse = Math.abs(this.crevasses.at(x / 330.0, z / 330.0));
        if (crevasse < 0.014) {
            h -= 32.0 * Math.pow(1.0 - crevasse / 0.014, 0.4) * Noise.smooth(edge / 48.0);
        }

        return h;
    }

    @Override
    public void build(Column column, double height, RealmMap.Blend blend) {
        int x = column.x;
        int z = column.z;
        int top = Mth.floor(height);
        Ground.stone(column, top);
        column.fill(top - 24, top - CRUST, this.under);
        boolean streak = this.tint.at(x / 18.0, z / 18.0) > 0.55;
        column.fill(top - CRUST + 1, top, streak ? this.tinted : this.glass);
        // The hive's tunnels, just under the glass and deeper, seen through it from above.
        boolean a = Math.abs(this.tunnelsA.at(x / 42.0, z / 42.0)) < 0.06;
        boolean b = Math.abs(this.tunnelsB.at(x / 57.0, z / 57.0)) < 0.05;
        if (a) {
            column.fill(top - CRUST - 5, top - CRUST, null);
            column.set(top - CRUST - 6, this.hive);
        }

        if (b) {
            int deep = top - CRUST - 16;
            column.fill(deep, deep + 4, null);
            column.set(deep - 1, this.hive);
        }
    }
}
