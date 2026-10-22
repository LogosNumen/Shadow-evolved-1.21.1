package net.logosnumen.nightmareevolved.creature.kind;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.Region;

/** Every kind of Nightmare Creature in the Dream Realm, region by region. */
public final class Kinds {
    private static final Map<String, Kind> BY_ID = new LinkedHashMap<>();

    static {
        ShoreKinds.ALL.forEach(Kinds::add);
        IsleKinds.ALL.forEach(Kinds::add);
    }

    private Kinds() {
    }

    private static void add(Kind kind) {
        if (BY_ID.put(kind.id, kind) != null) {
            throw new IllegalStateException("Two kinds named " + kind.id);
        }
    }

    public static List<Kind> all() {
        return List.copyOf(BY_ID.values());
    }

    @Nullable
    public static Kind byId(String id) {
        return BY_ID.get(id);
    }

    /** The kinds that haunt a region (its whole, not its parts), with what each haunt says of them there. */
    public static List<Kind.Haunt> haunts(Region region, List<Kind> into) {
        List<Kind.Haunt> haunts = new ArrayList<>();
        for (Kind kind : BY_ID.values()) {
            for (Kind.Haunt haunt : kind.haunts) {
                if (haunt.region() == region) {
                    haunts.add(haunt);
                    into.add(kind);
                }
            }
        }

        return haunts;
    }
}
