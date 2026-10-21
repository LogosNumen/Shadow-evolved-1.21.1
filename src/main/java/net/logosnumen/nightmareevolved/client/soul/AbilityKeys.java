package net.logosnumen.nightmareevolved.client.soul;

import com.mojang.blaze3d.platform.InputConstants;
import net.logosnumen.nightmareevolved.network.AbilityPayload;
import net.logosnumen.nightmareevolved.network.SoulActionPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * The keys that call on a soul's Aspect, one for each step of Ascension (Z, X, C, V and B by default: Dormant,
 * Awakened, Ascended, Transformation and Supreme); R to read the soul's runes; J to tend its Memories, Echoes and Soul
 * Sea; K to sink into the Soul Sea or rise from it; and G to dismiss the Memory in hand.
 */
public final class AbilityKeys {
    private static final String CATEGORY = "key.categories.nightmare_evolved";
    private static final int[] DEFAULTS = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B};
    private static final KeyMapping[] KEYS = new KeyMapping[DEFAULTS.length];
    private static final KeyMapping RUNES = new KeyMapping(
        "key.nightmare_evolved.runes", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY
    );

    private static final KeyMapping SOUL = new KeyMapping(
        "key.nightmare_evolved.soul", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY
    );
    private static final KeyMapping SEA = new KeyMapping(
        "key.nightmare_evolved.soul_sea", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY
    );
    private static final KeyMapping DISMISS = new KeyMapping(
        "key.nightmare_evolved.dismiss", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY
    );

    private static final KeyMapping CROSS = new KeyMapping(
        "key.nightmare_evolved.cross", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, CATEGORY
    );

    static {
        for (int i = 0; i < DEFAULTS.length; i++) {
            KEYS[i] = new KeyMapping("key.nightmare_evolved.ability_" + (i + 1), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, DEFAULTS[i], CATEGORY);
        }
    }

    private AbilityKeys() {
    }

    public static void onRegister(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : KEYS) {
            event.register(key);
        }

        event.register(RUNES);
        event.register(SOUL);
        event.register(SEA);
        event.register(DISMISS);
        event.register(CROSS);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        for (int slot = 0; slot < KEYS.length; slot++) {
            while (KEYS[slot].consumeClick()) {
                PacketDistributor.sendToServer(new AbilityPayload(slot));
            }
        }

        while (RUNES.consumeClick()) {
            mc.setScreen(new RunesScreen());
        }

        while (SOUL.consumeClick()) {
            mc.setScreen(new SoulScreen());
        }

        while (SEA.consumeClick()) {
            PacketDistributor.sendToServer(SoulActionPayload.of(SoulActionPayload.Action.SEA));
        }

        while (DISMISS.consumeClick()) {
            PacketDistributor.sendToServer(SoulActionPayload.of(SoulActionPayload.Action.DISMISS_HELD));
        }

        while (CROSS.consumeClick()) {
            PacketDistributor.sendToServer(SoulActionPayload.of(SoulActionPayload.Action.CROSS));
        }
    }

    public static Component soulSeaKey() {
        return SEA.getTranslatedKeyMessage();
    }

    public static Component dismissKey() {
        return DISMISS.getTranslatedKeyMessage();
    }

    /** Whether a key press is the one that opens the Soul screen, so pressing it again closes it. */
    public static boolean isSoulKey(int keyCode, int scanCode) {
        return SOUL.matches(keyCode, scanCode);
    }

    /** The key bound to a step's slot, as the player has it set. */
    public static Component key(int slot) {
        return KEYS[Math.max(0, Math.min(KEYS.length - 1, slot))].getTranslatedKeyMessage();
    }
}
