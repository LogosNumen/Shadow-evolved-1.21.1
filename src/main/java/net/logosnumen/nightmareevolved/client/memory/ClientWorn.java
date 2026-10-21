package net.logosnumen.nightmareevolved.client.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.logosnumen.nightmareevolved.network.WornPayload;

/** What each player in sight wears from their Memories, by entity id. */
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

    public static void clear() {
        WORN.clear();
    }
}
