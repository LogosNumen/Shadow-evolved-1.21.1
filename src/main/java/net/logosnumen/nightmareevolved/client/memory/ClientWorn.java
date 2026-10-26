package net.logosnumen.nightmareevolved.client.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.logosnumen.nightmareevolved.network.WornPayload;

/** What each player in sight wears from their Memories, and the form they have taken, by entity id. */
public final class ClientWorn {
    private static final Map<Integer, List<String>> WORN = new HashMap<>();

    private ClientWorn() {
    }

    public static void set(WornPayload payload) {
        if (payload.looks().isEmpty()) {
            WORN.remove(payload.entity());
        } else {
            WORN.put(payload.entity(), List.copyOf(payload.looks()));
        }
    }

    public static List<String> of(int entity) {
        return WORN.getOrDefault(entity, List.of());
    }

    /** The Transformation a player has taken, by its ability id, or empty if they wear their own body. */
    public static String form(int entity) {
        for (String look : of(entity)) {
            if (look.startsWith(WornPayload.FORM)) {
                return look.substring(WornPayload.FORM.length());
            }
        }

        return "";
    }

    public static void clear() {
        WORN.clear();
    }
}
