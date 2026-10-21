package net.logosnumen.nightmareevolved.aspect;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.chat.Component;

/**
 * An Aspect from the novel: the power one of its Awakened carries, at its own Rank, with its abilities from Dormant to
 * Supreme and the Flaw that comes with it. {@code flaws} lists the Flaws that fit it: one when the novel names the
 * Flaw ({@code canonFlaw}), a few the mod judged fitting when the novel never says.
 */
public final class Aspect {
    private final String id;
    private final Tier tier;
    private final Vfx.Palette palette;
    private final List<Ability> abilities;
    private final List<String> flaws;
    private final boolean canonFlaw;
    private final List<String> signature;

    private Aspect(String id, Tier tier, Vfx.Palette palette, List<Ability> abilities, List<String> flaws, boolean canonFlaw, List<String> signature) {
        this.id = id;
        this.tier = tier;
        this.palette = palette;
        this.abilities = List.copyOf(abilities);
        this.flaws = List.copyOf(flaws);
        this.canonFlaw = canonFlaw;
        this.signature = List.copyOf(signature);
    }

    public static Builder of(String id, Tier tier, Vfx.Palette palette) {
        return new Builder(id, tier, palette);
    }

    public String id() {
        return this.id;
    }

    /** The Aspect's own Rank: its potential. */
    public Tier tier() {
        return this.tier;
    }

    public Vfx.Palette palette() {
        return this.palette;
    }

    public List<Ability> abilities() {
        return this.abilities;
    }

    public List<String> flaws() {
        return this.flaws;
    }

    /** Whether the novel itself names this Aspect's Flaw. */
    public boolean canonFlaw() {
        return this.canonFlaw;
    }

    /** Attributes the Aspect's bearer was born with in the novel; whoever wakes it is born with them too. */
    public List<String> signature() {
        return this.signature;
    }

    /** Whether the Aspect is Divine, and so can grow a soul up to seven cores. */
    public boolean divine() {
        return this.tier == Tier.DIVINE;
    }

    /** The ability on a step's key, or null if the Aspect has none there. */
    @Nullable
    public Ability at(Stage stage) {
        for (Ability ability : this.abilities) {
            if (!ability.innate && ability.stage == stage) {
                return ability;
            }
        }

        return null;
    }

    /** Every ability a soul of this Rank has woken, innate ones included. */
    public List<Ability> unlocked(Rank rank) {
        List<Ability> out = new ArrayList<>();
        for (Ability ability : this.abilities) {
            if (rank.atLeast(ability.stage.unlock)) {
                out.add(ability);
            }
        }

        return out;
    }

    @Nullable
    public Ability ability(String abilityId) {
        for (Ability ability : this.abilities) {
            if (ability.id.equals(abilityId)) {
                return ability;
            }
        }

        return null;
    }

    public Component title() {
        return Component.translatable("aspect.nightmare_evolved." + this.id);
    }

    public Component description() {
        return Component.translatable("aspect.nightmare_evolved." + this.id + ".desc");
    }

    /** Whose Aspect it is in the novel. */
    public Component bearer() {
        return Component.translatable("aspect.nightmare_evolved." + this.id + ".bearer");
    }

    public static final class Builder {
        private final String id;
        private final Tier tier;
        private final Vfx.Palette palette;
        private final List<Ability> abilities = new ArrayList<>();
        private final List<String> flaws = new ArrayList<>();
        private boolean canonFlaw;
        private final List<String> signature = new ArrayList<>();

        private Builder(String id, Tier tier, Vfx.Palette palette) {
            this.id = id;
            this.tier = tier;
            this.palette = palette;
        }

        public Builder with(Ability... abilities) {
            this.abilities.addAll(List.of(abilities));
            return this;
        }

        /** The Flaw the novel gives this Aspect. */
        public Builder flaw(String flaw) {
            this.flaws.clear();
            this.flaws.add(flaw);
            this.canonFlaw = true;
            return this;
        }

        /** Flaws that suit an Aspect whose Flaw the novel never names; one is rolled. */
        public Builder fittingFlaws(String... flaws) {
            this.flaws.clear();
            this.flaws.addAll(List.of(flaws));
            this.canonFlaw = false;
            return this;
        }

        /** Attributes that come with this Aspect, as they came to its bearer in the novel. */
        public Builder signature(String... attributes) {
            this.signature.addAll(List.of(attributes));
            return this;
        }

        public Aspect build() {
            return new Aspect(this.id, this.tier, this.palette, this.abilities, this.flaws, this.canonFlaw, this.signature);
        }
    }
}
