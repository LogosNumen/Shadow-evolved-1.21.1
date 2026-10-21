package net.logosnumen.nightmareevolved.client.soulsea;

import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/** A slim bar under the crosshair while the player meditates toward their Soul Sea. */
public final class MeditationOverlay {
    private static long started = -1L;
    private static long length;
    private static ResourceKey<Level> where;

    private MeditationOverlay() {
    }

    public static void start(int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (ticks <= 0 || mc.level == null) {
            started = -1L;
            return;
        }

        started = System.currentTimeMillis();
        length = ticks * 50L;
        where = mc.level.dimension();
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (started < 0L || mc.level == null || mc.options.hideGui) {
            return;
        }

        long elapsed = System.currentTimeMillis() - started;
        if (elapsed > length + 1500L || !mc.level.dimension().equals(where)) {
            started = -1L;
            return;
        }

        float progress = Mth.clamp((float)elapsed / length, 0.0F, 1.0F);
        Font font = mc.font;
        int width = graphics.guiWidth();
        int bar = 120;
        int x = (width - bar) / 2;
        int y = graphics.guiHeight() / 2 + 28;
        graphics.drawCenteredString(font, Component.translatable("soul_sea.nightmare_evolved.meditating_bar"), width / 2, y - 11, 0xFFE8E2F4);
        graphics.fill(x - 1, y - 1, x + bar + 1, y + 4, 0xAA100C18);
        graphics.fill(x, y, x + (int)(bar * progress), y + 3, 0xFF000000 | Spell.COLOUR);
    }
}
