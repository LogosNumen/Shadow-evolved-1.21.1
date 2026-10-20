package net.logosnumen.nightmareevolved.colossus;

import net.minecraft.network.chat.Component;

/**
 * How one colossus differs from the rest of its kind: its colouring, its size, whether it is an elder, and whatever
 * parts its body plan lets vary (horns, eyes, crystals, weapons, segments and so on). Everything is drawn from a single
 * seed, so the server and every client agree on it without sending more than that number.
 */
public record ColossusVariant(ColossusKind kind, int seed) {
    /** A stable whole number in [0, options) for a named feature. */
    public int pick(String feature, int options) {
        return Math.floorMod(this.hash(feature), Math.max(1, options));
    }

    /** A stable number in [0, 1) for a named feature. */
    public float roll(String feature) {
        return (this.hash(feature) >>> 8) / (float)(1 << 24);
    }

    public boolean chance(String feature, float odds) {
        return this.roll(feature) < odds;
    }

    public int palette() {
        return this.pick("palette", ColossusKind.PALETTES);
    }

    /** About one in twelve is an elder: larger, tougher and more ornamented than the rest. */
    public boolean elder() {
        return this.chance("elder", 0.08F);
    }

    /** Body scale, from a little under to a good deal over the kind's usual size. */
    public float size() {
        return 0.9F + this.roll("size") * 0.25F + (this.elder() ? 0.2F : 0.0F);
    }

    /** "Mossback Cairn Colossus", or "Elder Mossback Cairn Colossus". */
    public Component name() {
        Component epithet = Component.translatable("colossus.nightmare_evolved." + this.kind.id + ".palette." + this.palette());
        Component kindName = Component.translatable("entity.nightmare_evolved." + this.kind.id);
        return Component.translatable(this.elder() ? "colossus.nightmare_evolved.elder_name" : "colossus.nightmare_evolved.name", epithet, kindName);
    }

    private int hash(String feature) {
        int h = this.seed * 0x9E3779B1 ^ feature.hashCode() * 0x85EBCA6B;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        h *= 0x297A2D39;
        return h ^ h >>> 15;
    }
}
