package net.logosnumen.nightmareevolved.client.soul;

import com.mojang.blaze3d.platform.InputConstants;
import net.logosnumen.nightmareevolved.network.AbilityPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** The two keys that call on a soul's Aspect: Z for its first ability and X for its second, by default. */
public final class AbilityKeys {
    private static final String CATEGORY = "key.categories.nightmare_evolved";
    private static final KeyMapping[] KEYS = {
        new KeyMapping("key.nightmare_evolved.ability_1", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY),
        new KeyMapping("key.nightmare_evolved.ability_2", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY)
    };

    private AbilityKeys() {
    }

    public static void onRegister(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : KEYS) {
            event.register(key);
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        for (int slot = 0; slot < KEYS.length; slot++) {
            while (KEYS[slot].consumeClick()) {
                PacketDistributor.sendToServer(new AbilityPayload(slot));
            }
        }
    }

    /** The key bound to a slot, as the player has it set. */
    public static Component key(int slot) {
        return KEYS[slot].getTranslatedKeyMessage();
    }
}
