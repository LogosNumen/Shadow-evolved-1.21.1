package net.logosnumen.nightmareevolved.client.dream;

import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.law.DarkSea;
import net.logosnumen.nightmareevolved.dream.law.RealmLaws;
import net.logosnumen.nightmareevolved.network.RealmPayload;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * What the client knows of the Dream Realm around the camera: which region it is in (and so which sky to draw), the
 * state of the laws the server sends (the Dark Sea's seal, the Crushing, Godgrave's torn veil), and the region's own
 * weather, which here belongs to the region rather than to the world.
 */
public final class ClientRealm {
    private static Region region = Region.FORGOTTEN_SHORE;
    private static boolean sealed;
    private static float pressure;
    private static float shownPressure;
    private static boolean rattling;
    private static List<RealmPayload.Tear> tears = List.of();
    private static float storm;

    private ClientRealm() {
    }

    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && DreamRealm.is(mc.level);
    }

    public static Region region() {
        return region;
    }

    public static Region.Sky sky() {
        return region.sky;
    }

    public static boolean sealed() {
        return sealed;
    }

    /** The Crushing's pressure as the screen shows it, easing toward what the server says. */
    public static float pressure() {
        return shownPressure;
    }

    public static boolean rattling() {
        return rattling;
    }

    public static List<RealmPayload.Tear> tears() {
        return tears;
    }

    public static void set(RealmPayload payload) {
        sealed = payload.sealed();
        pressure = payload.pressure();
        rattling = payload.rattling();
        tears = List.copyOf(payload.tears());
    }

    public static void clear() {
        sealed = false;
        pressure = 0.0F;
        shownPressure = 0.0F;
        rattling = false;
        tears = List.of();
        storm = 0.0F;
    }

    /** Whether the camera is under the Dark Sea. */
    public static boolean underDarkSea(Camera camera, float partialTick) {
        if (!active() || sealed) {
            return false;
        }

        Vec3 at = camera.getPosition();
        ClientLevel level = Minecraft.getInstance().level;
        return level != null && at.y < DarkSea.level(level.getDayTime(), partialTick) && DarkSea.reaches(at.x, at.z);
    }

    @Nullable
    private static Region regionAt(ClientLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey().map(key -> Region.byBiome(key.location())).orElse(null);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || !DreamRealm.is(level)) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Region here = regionAt(level, camera.getBlockPosition());
        if (here != null) {
            region = here;
        }

        shownPressure += (pressure - shownPressure) * 0.15F;
        // The Stormsea's storms are its own; everywhere else in the realm the sky keeps its region's weather.
        boolean stormsea = region.sky == Region.Sky.STORM;
        float target = stormsea && RealmLaws.stormy(level.getGameTime()) ? 1.0F : 0.0F;
        storm += (target - storm) * 0.02F;
        level.setRainLevel(storm);
        level.setThunderLevel(storm * 0.8F);
    }
}
