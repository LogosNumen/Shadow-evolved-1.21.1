package net.logosnumen.nightmareevolved.client.soul;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The soul at a glance, in the top-left corner where chat never reaches: rank and Aspect, an essence bar, a diamond
 * for each soul core that fills as it saturates (and glows when every core is full), and the Aspect's abilities with
 * their keys and cooldowns.
 */
public final class SoulHud {
    private static final int ESSENCE = 0xFF4AA8FF;
    private static final int ESSENCE_DARK = 0xFF13304A;
    private static final int CORE = 0xFF000000 | Spell.COLOUR;
    private static final int PANEL = 0x90000000;

    private SoulHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.player.isSpectator()) {
            return;
        }

        Font font = mc.font;
        Rank rank = ClientSoul.rank();
        Aspect aspect = rank == Rank.ASPIRANT ? null : Aspects.byId(ClientSoul.aspect());
        int height = rank == Rank.ASPIRANT ? 14 : (aspect == null ? 36 : 36 + aspect.abilities().size() * 10 + 2);
        int x = 6;
        int y = 6;
        Component title = rank.title();
        if (aspect != null) {
            title = title.copy().append(Component.literal(" · ")).append(aspect.title());
        }

        int width = Math.max(124, font.width(title) + 8);
        if (aspect != null) {
            for (int i = 0; i < aspect.abilities().size(); i++) {
                Ability ability = aspect.abilities().get(i);
                int line = font.width(AbilityKeys.key(i)) + font.width(Aspects.title(aspect, ability)) + font.width(ability.unlock().title()) + 18;
                width = Math.max(width, line);
            }
        }
        graphics.fill(x - 3, y - 3, x - 3 + width, y - 3 + height, PANEL);
        graphics.drawString(font, title, x, y, 0xFF000000 | Spell.COLOUR, false);
        if (rank == Rank.ASPIRANT) {
            return;
        }

        // Essence.
        float max = Math.max(1.0F, ClientSoul.maxEssence());
        float fill = Mth.clamp(ClientSoul.essence() / max, 0.0F, 1.0F);
        int barY = y + 12;
        graphics.fill(x, barY, x + 96, barY + 5, ESSENCE_DARK);
        graphics.fill(x, barY, x + Math.round(96 * fill), barY + 5, ESSENCE);
        graphics.drawString(font, Integer.toString(Math.round(ClientSoul.essence())), x + 100, barY - 2, 0xFFB8DCFF, false);

        // Soul cores, each a diamond filled to its share of the saturation.
        int cores = rank.cores;
        float perCore = ClientSoul.capacity() / Math.max(1, cores);
        boolean full = ClientSoul.capacity() > 0.0F && ClientSoul.saturation() >= ClientSoul.capacity() - 0.001F;
        for (int i = 0; i < cores; i++) {
            float coreFill = perCore <= 0.0F ? 0.0F : Mth.clamp((ClientSoul.saturation() - i * perCore) / perCore, 0.0F, 1.0F);
            diamond(graphics, x + 4 + i * 11, y + 25, coreFill, full);
        }

        if (aspect != null) {
            long now = mc.level == null ? 0L : mc.level.getGameTime();
            for (int i = 0; i < aspect.abilities().size(); i++) {
                ability(graphics, font, aspect, i, rank, now, x, y + 37 + i * 10);
            }
        }
    }

    /** One ability's line: its key, its name, and whether it is waiting on rank, cooldown or essence. */
    private static void ability(GuiGraphics graphics, Font font, Aspect aspect, int slot, Rank rank, long now, int x, int y) {
        Ability ability = aspect.abilities().get(slot);
        Component key = AbilityKeys.key(slot);
        Component name = Aspects.title(aspect, ability);
        int colour;
        Component state;
        if (!rank.atLeast(ability.unlock())) {
            colour = 0xFF6E6880;
            state = ability.unlock().title();
        } else if (ClientSoul.readyAt(slot) > now) {
            colour = 0xFF9A93B0;
            state = Component.literal((ClientSoul.readyAt(slot) - now + 19) / 20 + "s");
        } else if (ClientSoul.essence() < ability.cost()) {
            colour = 0xFF9A93B0;
            state = Component.literal(Integer.toString((int)Math.ceil(ability.cost())));
        } else {
            colour = 0xFFE8DDFF;
            state = Component.empty();
        }

        int keyWidth = font.width(key) + 6;
        graphics.fill(x, y - 1, x + keyWidth, y + 8, 0xFF2A2340);
        graphics.drawString(font, key, x + 3, y, 0xFFE8DDFF, false);
        graphics.drawString(font, name, x + keyWidth + 3, y, colour, false);
        graphics.drawString(font, state, x + keyWidth + 6 + font.width(name), y, 0xFF8FA8D8, false);
    }

    /** A small diamond, filled from the bottom up. */
    private static void diamond(GuiGraphics graphics, int cx, int cy, float fill, boolean glow) {
        int filledFrom = cy + 4 - Math.round(fill * 9);
        for (int dy = -4; dy <= 4; dy++) {
            int half = 4 - Math.abs(dy);
            int y = cy + dy;
            int colour = y >= filledFrom ? CORE : 0xFF2A2340;
            if (glow) {
                colour = 0xFFE8DDFF;
            }

            graphics.fill(cx - half, y, cx + half + 1, y + 1, colour);
        }
    }
}
