package net.logosnumen.nightmareevolved.dream;

import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * A soul's dealings with the Dream Realm, kept in the soul:
 * <ul>
 * <li>its anchor, the Gateway it last passed through, where it appears each time it sleeps;</li>
 * <li>while it dreams, where its body lies asleep in the waking world and which body that is;</li>
 * <li>its belongings on either side: what it left with its sleeping body, and what it left in the Dream Realm when it
 * last woke (only a Master or higher crosses in body and takes everything along);</li>
 * <li>the tethers of those who cross in body: where in each world they return to.</li>
 * </ul>
 */
public final class DreamState {
    private final CompoundTag tag;

    private DreamState(CompoundTag tag) {
        this.tag = tag;
    }

    public static DreamState of(Player player) {
        return new DreamState(Soul.of(player).dream());
    }

    /** The anchor Gateway's id, or empty if the soul has none yet. */
    public String anchor() {
        return this.tag.getString("Anchor");
    }

    public void setAnchor(String gateway) {
        this.tag.putString("Anchor", gateway);
    }

    /** Whether the soul is in the Dream Realm now, and how: asleep (its body left behind) or crossed over in body. */
    public boolean dreaming() {
        return this.tag.contains("Return");
    }

    public boolean inBody() {
        return this.tag.getBoolean("InBody");
    }

    /** Where to return to on waking: the sleeping body's place, or the crosser's tether. */
    public CompoundTag returnPoint() {
        return this.tag.getCompound("Return");
    }

    public void setReturn(CompoundTag point, boolean inBody) {
        this.tag.put("Return", point);
        this.tag.putBoolean("InBody", inBody);
    }

    public void clearReturn() {
        this.tag.remove("Return");
        this.tag.remove("InBody");
    }

    @Nullable
    public UUID body() {
        CompoundTag point = this.returnPoint();
        return point.hasUUID("Body") ? point.getUUID("Body") : null;
    }

    /** What the soul left with its sleeping body. */
    public ListTag waking() {
        return this.tag.getList("Waking", Tag.TAG_COMPOUND);
    }

    public int wakingExperience() {
        return this.tag.getInt("WakingExperience");
    }

    public void setWaking(ListTag items, int experience) {
        this.tag.put("Waking", items);
        this.tag.putInt("WakingExperience", experience);
    }

    public void clearWaking() {
        this.tag.remove("Waking");
        this.tag.remove("WakingExperience");
    }

    /** What the soul left in the Dream Realm when it woke, waiting for its next dream. */
    public ListTag dreamt() {
        return this.tag.getList("Dreamt", Tag.TAG_COMPOUND);
    }

    public int dreamtExperience() {
        return this.tag.getInt("DreamtExperience");
    }

    public void setDreamt(ListTag items, int experience) {
        this.tag.put("Dreamt", items);
        this.tag.putInt("DreamtExperience", experience);
    }

    /** A Master's tether in the waking world, or a Saint's in the Dream Realm: where crossing over brings them back. */
    public CompoundTag tether(boolean realm) {
        return this.tag.getCompound(realm ? "RealmTether" : "Tether");
    }

    public void setTether(boolean realm, CompoundTag point) {
        this.tag.put(realm ? "RealmTether" : "Tether", point);
    }

    public int visits() {
        return this.tag.getInt("Visits");
    }

    public void visit() {
        this.tag.putInt("Visits", this.visits() + 1);
    }
}
