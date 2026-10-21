package net.logosnumen.nightmareevolved.client.soul;

import java.util.List;
import net.logosnumen.nightmareevolved.network.SeaPayload;

/** The local player's Memories, Echoes and Soul Sea, as the server last described them. */
public final class ClientSea {
    private static SeaPayload sea = SeaPayload.EMPTY;

    private ClientSea() {
    }

    public static void set(SeaPayload payload) {
        sea = payload;
    }

    public static void clear() {
        sea = SeaPayload.EMPTY;
    }

    public static List<SeaPayload.MemoryEntry> memories() {
        return sea.memories();
    }

    public static List<SeaPayload.EchoEntry> echoes() {
        return sea.echoes();
    }

    public static boolean vault() {
        return sea.vault();
    }

    public static String citadel() {
        return sea.citadel();
    }

    public static int echoLimit() {
        return sea.echoLimit();
    }
}
