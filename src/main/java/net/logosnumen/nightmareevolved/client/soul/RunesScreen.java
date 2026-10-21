package net.logosnumen.nightmareevolved.client.soul;

import java.util.ArrayList;
import java.util.List;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Aspect;
import net.logosnumen.nightmareevolved.aspect.Aspects;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.flaw.Flaw;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * The soul's runes, as the Spell shows them to their bearer: Name, Rank and Class, cores and fragments, the Aspect and
 * its Rank, every ability (those not yet woken shown dim), the Flaw, and the Attributes. Scrolls when long.
 */
public class RunesScreen extends Screen {
    private static final int RUNE = 0xFF000000 | Spell.COLOUR;
    private static final int TEXT = 0xFFE8E2F4;
    private static final int DIM = 0xFF7D7690;
    private static final int WIDTH = 300;
    private final List<Line> lines = new ArrayList<>();
    private double scroll;

    private record Line(FormattedCharSequence text, int colour, int indent) {
    }

    public RunesScreen() {
        super(Component.translatable("runes.nightmare_evolved.title"));
    }

    @Override
    protected void init() {
        this.lines.clear();
        Minecraft mc = Minecraft.getInstance();
        Rank rank = ClientSoul.rank();
        this.rune("runes.nightmare_evolved.name", mc.player == null ? Component.empty() : mc.player.getName());
        this.rune("runes.nightmare_evolved.rank", rank.title().copy().append(" (").append(rank.tier()).append(")"));
        Aspect aspect = Aspects.byId(ClientSoul.aspect());
        if (rank != Rank.ASPIRANT) {
            if (aspect != null && aspect.divine()) {
                this.rune("runes.nightmare_evolved.class", CreatureClass.values()[Mth.clamp(ClientSoul.cores() - 1, 0, 6)].title());
            }

            this.rune("runes.nightmare_evolved.cores", Component.literal(ClientSoul.cores() + "/" + (aspect != null && aspect.divine() ? 7 : 1)));
            this.rune("runes.nightmare_evolved.fragments", Component.literal(ClientSoul.fragments() + "/" + ClientSoul.capacity()));
            this.rune("runes.nightmare_evolved.essence", Component.literal(Math.round(ClientSoul.essence()) + "/" + Math.round(ClientSoul.maxEssence())));
        }

        if (aspect == null) {
            this.gap();
            this.text(Component.translatable("runes.nightmare_evolved.no_aspect"), DIM, 0);
            return;
        }

        this.gap();
        this.rune("runes.nightmare_evolved.aspect", aspect.title().copy().append(" (").append(aspect.tier().title()).append(")"));
        this.text(aspect.description(), TEXT, 8);
        this.text(Component.translatable("runes.nightmare_evolved.bearer", aspect.bearer()), DIM, 8);
        for (Ability ability : aspect.abilities()) {
            boolean awake = rank.atLeast(ability.stage.unlock);
            Component label = ability.innate ? Component.translatable("runes.nightmare_evolved.innate") : ability.stage.title();
            Component head = label.copy().append(": ").append(ability.title().copy().withStyle(awake ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
            if (ability.filled) {
                head = head.copy().append(Component.translatable("runes.nightmare_evolved.filled").withStyle(ChatFormatting.DARK_GRAY));
            }

            this.gap();
            this.text(head, awake ? RUNE : DIM, 0);
            this.text(ability.description(), awake ? TEXT : DIM, 8);
        }

        Flaw flaw = Flaw.byId(ClientSoul.flaw());
        if (flaw != null) {
            this.gap();
            this.rune("runes.nightmare_evolved.flaw", flaw.title());
            this.text(flaw.description(), TEXT, 8);
            if (!aspect.canonFlaw()) {
                this.text(Component.translatable("runes.nightmare_evolved.fitting_flaw"), DIM, 8);
            }
        }

        if (!ClientSoul.attributes().isEmpty()) {
            this.gap();
            this.text(Component.translatable("runes.nightmare_evolved.attributes"), RUNE, 0);
            for (String id : ClientSoul.attributes()) {
                SoulAttribute attribute = SoulAttribute.byId(id);
                if (attribute != null) {
                    this.text(Component.literal("[").append(attribute.title()).append("] ").append(attribute.description()), TEXT, 8);
                }
            }
        }
    }

    private void rune(String key, Component value) {
        this.text(Component.translatable(key, value), RUNE, 0);
    }

    private void text(Component text, int colour, int indent) {
        for (FormattedCharSequence line : this.font.split(text, WIDTH - indent)) {
            this.lines.add(new Line(line, colour, indent));
        }
    }

    private void gap() {
        this.lines.add(new Line(FormattedCharSequence.EMPTY, 0, 0));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (this.width - WIDTH) / 2;
        int top = 28;
        int bottom = this.height - 20;
        graphics.fill(left - 10, top - 10, left + WIDTH + 10, bottom + 6, 0xD0100C18);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, RUNE);
        int visible = (bottom - top) / 10;
        int start = (int)Mth.clamp(this.scroll, 0, Math.max(0, this.lines.size() - visible));
        for (int i = 0; i < visible && start + i < this.lines.size(); i++) {
            Line line = this.lines.get(start + i);
            graphics.drawString(this.font, line.text(), left + line.indent(), top + i * 10, line.colour(), false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.max(0.0, this.scroll - scrollY * 3.0);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
