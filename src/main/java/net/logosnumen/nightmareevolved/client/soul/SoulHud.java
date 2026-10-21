package net.logosnumen.nightmareevolved.client.soul;

import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.aspect.Stage;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.SoulRules;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The soul at a glance, in the top-left corner where chat never reaches: Rank and Aspect (and class, for a Divine
 * Aspect that grows cores), an essence bar, a diamond for each soul core filling with fragments, and the Aspect's
 * ability on each step's key, with whether it is locked, resting, held, passive or waiting on essence.
 */
public final class SoulHud {
    private static final int ESSENCE = 0xFF4AA8FF;
    private static final int ESSENCE_DARK = 0xFF13304A;
    private static final int CORE = 0xFF000000 | Spell.COLOUR;
    private static final int PANEL = 0x90000000;
    private static final int LINE = 10;

    private SoulHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.player.isSpectator() || mc.screen instanceof RunesScreen) {
            return;
        }

        Font font = mc.font;
        Rank rank = ClientSoul.rank();
        Aspect aspect = rank == Rank.ASPIRANT ? null : Aspects.byId(ClientSoul.aspect());
        int lines = 0;
        if (aspect != null) {
            for (Stage stage : Stage.values()) {
                if (aspect.at(stage) != null && rank.atLeast(stage.unlock)) {
                    lines++;
                }
            }
        }

        int x = 6;
        int y = 6;
        Component title = rank.title();
        if (aspect != null) {
            title = title.copy().append(Component.literal(" · ")).append(aspect.title());
            if (aspect.divine()) {
                title = title.copy().append(Component.literal(" · ")).append(CreatureClass.values()[Mth.clamp(ClientSoul.cores() - 1, 0, 6)].title());
            }
        }

        int width = Math.max(130, font.width(title) + 8);
        int height = rank == Rank.ASPIRANT ? 14 : 36 + (lines > 0 ? lines * LINE + 2 : 0);
        graphics.fill(x - 3, y - 3, x - 3 + width, y - 3 + height, PANEL);
        graphics.drawString(font, title, x, y, 0xFF000000 | Spell.COLOUR, false);
        if (rank == Rank.ASPIRANT) {
            return;
        }

        float max = Math.max(1.0F, ClientSoul.maxEssence());
        float fill = Mth.clamp(ClientSoul.essence() / max, 0.0F, 1.0F);
        int barY = y + 12;
        graphics.fill(x, barY, x + 96, barY + 5, ESSENCE_DARK);
        graphics.fill(x, barY, x + Math.round(96 * fill), barY + 5, ESSENCE);
        graphics.drawString(font, Integer.toString(Math.round(ClientSoul.essence())), x + 100, barY - 2, 0xFFB8DCFF, false);

        int cores = ClientSoul.cores();
        for (int i = 0; i < cores; i++) {
            float coreFill = Mth.clamp((ClientSoul.fragments() - i * (float)SoulRules.FRAGMENTS_PER_CORE) / SoulRules.FRAGMENTS_PER_CORE, 0.0F, 1.0F);
            diamond(graphics, x + 4 + i * 11, y + 25, coreFill, coreFill >= 1.0F);
        }

        graphics.drawString(font, ClientSoul.fragments() + "/" + ClientSoul.capacity(), x + 8 + cores * 11, y + 21, 0xFF8F86A8, false);
        if (aspect == null) {
            return;
        }

        long now = mc.level == null ? 0L : mc.level.getGameTime();
        int row = 0;
        for (Stage stage : Stage.values()) {
            Ability ability = aspect.at(stage);
            if (ability != null && rank.atLeast(stage.unlock)) {
                line(graphics, font, ability, stage.ordinal(), now, x, y + 37 + row * LINE);
                row++;
            }
        }
    }

    private static void line(GuiGraphics graphics, Font font, Ability ability, int slot, long now, int x, int y) {
        Component key = AbilityKeys.key(slot);
        Component name = ability.title();
        int colour = 0xFFE8DDFF;
        Component state = Component.empty();
        if (ability.kind == Ability.Kind.PASSIVE) {
            colour = 0xFFB0A8C8;
            state = Component.translatable("hud.nightmare_evolved.passive");
        } else if (ClientSoul.held(ability.id)) {
            colour = 0xFFFFE08A;
            state = Component.translatable("hud.nightmare_evolved.held");
        } else if (ClientSoul.readyAt(slot) > now) {
            colour = 0xFF9A93B0;
            state = Component.literal((ClientSoul.readyAt(slot) - now + 19) / 20 + "s");
        } else if (ClientSoul.essence() < ability.cost) {
            colour = 0xFF9A93B0;
            state = Component.literal(Integer.toString((int)Math.ceil(ability.cost)));
        }

        int keyWidth = font.width(key) + 6;
        graphics.fill(x, y - 1, x + keyWidth, y + 8, 0xFF2A2340);
        graphics.drawString(font, key, x + 3, y, 0xFFE8DDFF, false);
        graphics.drawString(font, name, x + keyWidth + 3, y, colour, false);
        graphics.drawString(font, state, x + keyWidth + 6 + font.width(name), y, 0xFF8FA8D8, false);
    }

    /** A small diamond, filled from the bottom up, and glowing when full. */
    private static void diamond(GuiGraphics graphics, int cx, int cy, float fill, boolean glow) {
        int filledFrom = cy + 4 - Math.round(fill * 9);
        for (int dy = -4; dy <= 4; dy++) {
            int half = 4 - Math.abs(dy);
            int y = cy + dy;
            int colour = glow ? 0xFFE8DDFF : y >= filledFrom ? CORE : 0xFF2A2340;
            graphics.fill(cx - half, y, cx + half + 1, y + 1, colour);
        }
    }
}
