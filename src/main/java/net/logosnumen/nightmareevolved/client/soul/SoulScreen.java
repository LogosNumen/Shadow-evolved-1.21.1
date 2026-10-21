package net.logosnumen.nightmareevolved.client.soul;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.echo.EchoTrait;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.memory.item.MemoryItem;
import net.logosnumen.nightmareevolved.network.SeaPayload;
import net.logosnumen.nightmareevolved.network.SoulActionPayload;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.soulsea.SoulSea;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The soul as its owner tends it: every Memory (summon it, dismiss it, a layer of armour at a time if they like, see
 * how far it has mended), every Echo and Shadow (call it, send it back, make it a Shadow), and the Soul Sea itself
 * (go down into it, open what a Sovereign keeps there, see the Citadel bound to the soul).
 */
public class SoulScreen extends Screen {
    private static final int RUNE = 0xFF000000 | Spell.COLOUR;
    private static final int RUNE_DIM = 0xFF5E5280;
    private static final int TEXT = 0xFFE8E2F4;
    private static final int DIM = 0xFF7D7690;
    private static final int GOOD = 0xFF8FE3B0;
    private static final int WIDTH = 340;
    private static final int ROW = 26;
    private static Tab remembered = Tab.MEMORIES;
    private final List<Hit> hits = new ArrayList<>();
    private Tab tab = remembered;
    private double scroll;
    private List<Component> tooltip = List.of();

    private enum Tab {
        MEMORIES,
        ECHOES,
        SEA
    }

    private record Hit(int x0, int y0, int x1, int y1, Runnable action) {
        boolean in(double x, double y) {
            return x >= this.x0 && x < this.x1 && y >= this.y0 && y < this.y1;
        }
    }

    public SoulScreen() {
        super(Component.translatable("soul.nightmare_evolved.screen"));
    }

    private static void send(SoulActionPayload.Action action, UUID target, int mask) {
        PacketDistributor.sendToServer(new SoulActionPayload(action, target, mask));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.hits.clear();
        this.tooltip = List.of();
        int left = (this.width - WIDTH) / 2;
        int top = 34;
        int bottom = this.height - 26;
        g.fill(left - 10, top - 26, left + WIDTH + 10, bottom + 18, 0xD0100C18);
        g.renderOutline(left - 10, top - 26, WIDTH + 20, bottom - top + 44, RUNE_DIM);
        this.tabs(g, left, top - 20, mouseX, mouseY);
        g.enableScissor(left - 4, top, left + WIDTH + 4, bottom);
        switch (this.tab) {
            case MEMORIES -> this.memories(g, left, top, bottom, mouseX, mouseY);
            case ECHOES -> this.echoes(g, left, top, bottom, mouseX, mouseY);
            case SEA -> this.sea(g, left, top, mouseX, mouseY);
        }

        g.disableScissor();
        g.drawCenteredString(this.font, Component.translatable("soul.nightmare_evolved.screen.keys", AbilityKeys.soulSeaKey(), AbilityKeys.dismissKey())
            .withStyle(ChatFormatting.DARK_GRAY), this.width / 2, bottom + 6, DIM);
        if (!this.tooltip.isEmpty()) {
            g.renderComponentTooltip(this.font, this.tooltip, mouseX, mouseY);
        }
    }

    private void tabs(GuiGraphics g, int left, int y, int mouseX, int mouseY) {
        long echoesOut = ClientSea.echoes().stream().filter(SeaPayload.EchoEntry::out).count();
        Component[] labels = {
            Component.translatable("soul.nightmare_evolved.screen.memories", ClientSea.memories().size()),
            Component.translatable("soul.nightmare_evolved.screen.echoes", echoesOut, ClientSea.echoLimit()),
            Component.translatable("soul.nightmare_evolved.screen.sea")
        };
        int width = WIDTH / labels.length;
        for (int i = 0; i < labels.length; i++) {
            Tab target = Tab.values()[i];
            int x = left + i * width;
            boolean on = this.tab == target;
            boolean hover = mouseX >= x && mouseX < x + width - 4 && mouseY >= y && mouseY < y + 14;
            g.fill(x, y, x + width - 4, y + 14, on ? 0xFF2E2448 : hover ? 0xFF221B36 : 0xFF161220);
            g.drawCenteredString(this.font, labels[i], x + (width - 4) / 2, y + 3, on ? RUNE : DIM);
            this.hits.add(new Hit(x, y, x + width - 4, y + 14, () -> {
                this.tab = target;
                remembered = target;
                this.scroll = 0.0;
            }));
        }
    }

    private boolean button(GuiGraphics g, int x, int y, int w, Component label, boolean enabled, int mouseX, int mouseY, Runnable action) {
        boolean hover = enabled && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 12;
        g.fill(x, y, x + w, y + 12, hover ? 0xFF3A2E5A : enabled ? 0xFF241C38 : 0xFF16121F);
        g.renderOutline(x, y, w, 12, enabled ? RUNE_DIM : 0xFF2A2438);
        g.drawCenteredString(this.font, label, x + w / 2, y + 2, enabled ? TEXT : DIM);
        if (enabled) {
            this.hits.add(new Hit(x, y, x + w, y + 12, action));
        }

        return hover;
    }

    private int first(int top, int bottom, int count) {
        int visible = Math.max(1, (bottom - top) / ROW);
        this.scroll = Mth.clamp(this.scroll, 0.0, Math.max(0, count - visible));
        return (int)this.scroll;
    }

    // Memories.

    private void memories(GuiGraphics g, int left, int top, int bottom, int mouseX, int mouseY) {
        List<SeaPayload.MemoryEntry> memories = ClientSea.memories();
        if (memories.isEmpty()) {
            g.drawWordWrap(this.font, Component.translatable(ClientSoul.rank() == Rank.ASPIRANT ? "soul.nightmare_evolved.screen.no_sea"
                : "soul.nightmare_evolved.screen.no_memories"), left, top + 4, WIDTH, DIM);
            return;
        }

        float essence = ClientSoul.essence();
        int start = this.first(top, bottom, memories.size());
        for (int i = start; i < memories.size(); i++) {
            int y = top + (i - start) * ROW;
            if (y + ROW > bottom + ROW) {
                break;
            }

            this.memory(g, left, y, memories.get(i), essence, mouseX, mouseY);
        }
    }

    private void memory(GuiGraphics g, int left, int y, SeaPayload.MemoryEntry entry, float essence, int mouseX, int mouseY) {
        ItemStack icon = entry.icon();
        MemoryData data = SoulMemories.data(icon);
        MemoryDef def = data == null ? null : Memories.byId(data.def());
        boolean out = entry.out() != 0;
        if (out) {
            g.fill(left - 2, y - 1, left + WIDTH + 2, y + ROW - 3, 0x30409070);
        }

        g.renderItem(icon, left + 2, y + 3);
        if (mouseX >= left && mouseX < left + 20 && mouseY >= y && mouseY < y + 22) {
            List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), icon));
            lines.add(Component.translatable("soul.nightmare_evolved.screen.cost", Math.round(entry.cost())).withStyle(ChatFormatting.AQUA));
            this.tooltip = lines;
        }

        g.drawString(this.font, icon.getHoverName(), left + 24, y + 2, TEXT, false);
        if (data != null && def != null) {
            MutableComponent runes = Tier.byOrdinal(data.rank()).title().copy().append(" · ").append(Component.translatable("memory.nightmare_evolved.tier",
                MemoryItem.roman(data.tier()))).append(" · ").append(def.kind.title());
            g.drawString(this.font, runes.withStyle(ChatFormatting.GRAY), left + 24, y + 13, DIM, false);
        }

        int right = left + WIDTH;
        Component status;
        if (out) {
            status = Component.translatable(entry.out() == (1 << entry.pieces().size()) - 1 || entry.pieces().size() == 1
                ? "soul.nightmare_evolved.screen.summoned" : "soul.nightmare_evolved.screen.partly");
        } else if (entry.condition() < 0.999F) {
            status = Component.translatable("soul.nightmare_evolved.screen.mending", Math.round(entry.condition() * 100.0F));
        } else {
            status = Component.translatable("soul.nightmare_evolved.screen.resting");
        }

        g.drawString(this.font, status, right - 150 - this.font.width(status), y + 8, out ? GOOD : DIM, false);
        int x = right - 64;
        if (out) {
            this.button(g, x, y + 6, 62, Component.translatable("soul.nightmare_evolved.screen.dismiss"), true, mouseX, mouseY,
                () -> send(SoulActionPayload.Action.DISMISS, entry.id(), 0));
        } else {
            boolean afford = essence + 0.01F >= entry.cost();
            this.button(g, x, y + 6, 62, Component.translatable("soul.nightmare_evolved.screen.summon", Math.round(entry.cost())), afford, mouseX, mouseY,
                () -> send(SoulActionPayload.Action.SUMMON, entry.id(), 0));
        }

        if (entry.pieces().size() > 1) {
            // Each layer of armour on its own: lit if it is on, dim if it rests.
            for (int p = 0; p < entry.pieces().size(); p++) {
                int px = right - 146 + p * 18;
                boolean on = (entry.out() & 1 << p) != 0;
                g.fill(px - 1, y + 4, px + 17, y + 22, on ? 0x6040A070 : 0x40201830);
                g.renderItem(entry.pieces().get(p), px, y + 5);
                int piece = p;
                this.hits.add(new Hit(px - 1, y + 4, px + 17, y + 22,
                    () -> send(on ? SoulActionPayload.Action.DISMISS : SoulActionPayload.Action.SUMMON, entry.id(), 1 << piece)));
            }
        } else if (entry.relic()) {
            this.button(g, right - 130, y + 6, 60, Component.translatable("soul.nightmare_evolved.screen.bind"), !out, mouseX, mouseY,
                () -> send(SoulActionPayload.Action.BIND, entry.id(), 0));
        }
    }

    // Echoes.

    private void echoes(GuiGraphics g, int left, int top, int bottom, int mouseX, int mouseY) {
        List<SeaPayload.EchoEntry> echoes = ClientSea.echoes();
        if (echoes.isEmpty()) {
            g.drawWordWrap(this.font, Component.translatable("soul.nightmare_evolved.screen.no_echoes"), left, top + 4, WIDTH, DIM);
            return;
        }

        long now = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        long out = echoes.stream().filter(SeaPayload.EchoEntry::out).count();
        int start = this.first(top, bottom, echoes.size());
        for (int i = start; i < echoes.size(); i++) {
            int y = top + (i - start) * ROW;
            if (y + ROW > bottom + ROW) {
                break;
            }

            this.echo(g, left, y, echoes.get(i), now, out < ClientSea.echoLimit(), mouseX, mouseY);
        }
    }

    private void echo(GuiGraphics g, int left, int y, SeaPayload.EchoEntry entry, long now, boolean room, int mouseX, int mouseY) {
        if (entry.out()) {
            g.fill(left - 2, y - 1, left + WIDTH + 2, y + ROW - 3, 0x30409070);
        }

        EntityType<?> type = EntityType.byString(entry.type()).orElse(null);
        SpawnEggItem egg = type == null ? null : SpawnEggItem.byId(type);
        g.renderItem(egg != null ? new ItemStack(egg) : new ItemStack(Items.ECHO_SHARD), left + 2, y + 3);
        MutableComponent name = entry.name().copy();
        if (entry.shadow()) {
            name = Component.translatable("soul.nightmare_evolved.screen.shadow", name);
        }

        g.drawString(this.font, name, left + 24, y + 2, entry.shadow() ? 0xFFB9A7E8 : TEXT, false);
        CreatureRank rank = CreatureRank.values()[Mth.clamp(entry.rank(), 0, 6)];
        CreatureClass cls = CreatureClass.values()[Mth.clamp(entry.cls(), 0, 6)];
        MutableComponent runes = rank.title().copy().append(" ").append(cls.title());
        List<Component> traits = new ArrayList<>();
        for (String id : entry.traits()) {
            EchoTrait trait = EchoTrait.byId(id);
            if (trait != null) {
                runes.append(" [").append(trait.title()).append("]");
                traits.add(Component.literal("[").append(trait.title()).append("] ").append(trait.description()).withStyle(ChatFormatting.GRAY));
            }
        }

        g.drawString(this.font, runes.withStyle(ChatFormatting.GRAY), left + 24, y + 13, DIM, false);
        if (mouseX >= left && mouseX < left + 200 && mouseY >= y && mouseY < y + 22 && !traits.isEmpty()) {
            this.tooltip = traits;
        }

        int right = left + WIDTH;
        int barX = right - 150;
        g.fill(barX, y + 10, barX + 40, y + 13, 0xFF201828);
        g.fill(barX, y + 10, barX + Math.round(40 * entry.health()), y + 13, entry.health() > 0.5F ? 0xFF7AD08A : 0xFFD0A040);
        boolean asleep = entry.sleeps() > now;
        int x = right - 64;
        if (entry.out()) {
            this.button(g, x, y + 6, 62, Component.translatable("soul.nightmare_evolved.screen.dismiss"), true, mouseX, mouseY,
                () -> send(SoulActionPayload.Action.ECHO_DISMISS, entry.id(), 0));
        } else if (asleep) {
            this.button(g, x, y + 6, 62, Component.translatable("soul.nightmare_evolved.screen.asleep", (entry.sleeps() - now) / 20), false, mouseX, mouseY,
                () -> {
                });
        } else {
            boolean can = room && ClientSoul.essence() + 0.01F >= entry.cost();
            this.button(g, x, y + 6, 62, Component.translatable("soul.nightmare_evolved.screen.summon", Math.round(entry.cost())), can, mouseX, mouseY,
                () -> send(SoulActionPayload.Action.ECHO_SUMMON, entry.id(), 0));
        }

        if (entry.canShadow()) {
            this.button(g, right - 104, y + 6, 38, Component.translatable("soul.nightmare_evolved.screen.make_shadow"), !entry.out(), mouseX, mouseY,
                () -> send(SoulActionPayload.Action.ECHO_SHADOW, entry.id(), 0));
        }
    }

    // The sea.

    private void sea(GuiGraphics g, int left, int top, int mouseX, int mouseY) {
        Rank rank = ClientSoul.rank();
        int y = top + 4;
        if (rank == Rank.ASPIRANT) {
            g.drawWordWrap(this.font, Component.translatable("soul.nightmare_evolved.screen.no_sea"), left, y, WIDTH, DIM);
            return;
        }

        g.drawString(this.font, Component.translatable("soul.nightmare_evolved.screen.suns", ClientSoul.cores(), ClientSoul.fragments(), ClientSoul.capacity()),
            left, y, RUNE, false);
        y += 14;
        boolean inSea = Minecraft.getInstance().level != null && Minecraft.getInstance().level.dimension().equals(SoulSea.DIMENSION);
        Component go = Component.translatable(inSea ? "soul.nightmare_evolved.screen.rise" : "soul.nightmare_evolved.screen.sink");
        this.button(g, left, y, 200, go, true, mouseX, mouseY, () -> {
            send(SoulActionPayload.Action.SEA, net.minecraft.Util.NIL_UUID, 0);
            this.onClose();
        });
        y += 16;
        g.drawWordWrap(this.font, Component.translatable(inSea ? "soul.nightmare_evolved.screen.rise.desc" : "soul.nightmare_evolved.screen.sink.desc"),
            left, y, WIDTH, DIM);
        y += 34;
        g.drawString(this.font, Component.translatable("soul.nightmare_evolved.screen.echo_limit", ClientSea.echoLimit()), left, y, TEXT, false);
        y += 16;
        if (rank.atLeast(Rank.SAINT)) {
            String citadel = ClientSea.citadel();
            Component line = citadel.isEmpty() ? Component.translatable("soul.nightmare_evolved.screen.no_citadel")
                : Component.translatable("soul.nightmare_evolved.screen.citadel", Component.translatable("citadel.nightmare_evolved." + citadel));
            g.drawWordWrap(this.font, line, left, y, WIDTH, TEXT);
            y += 24;
        }

        if (ClientSea.vault()) {
            this.button(g, left, y, 200, Component.translatable("soul.nightmare_evolved.screen.vault"), true, mouseX, mouseY,
                () -> send(SoulActionPayload.Action.VAULT, net.minecraft.Util.NIL_UUID, 0));
            y += 16;
            g.drawWordWrap(this.font, Component.translatable("soul.nightmare_evolved.screen.vault.desc"), left, y, WIDTH, DIM);
        } else {
            g.drawWordWrap(this.font, Component.translatable("soul.nightmare_evolved.screen.vault_locked"), left, y, WIDTH, DIM);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Hit hit : List.copyOf(this.hits)) {
                if (hit.in(mouseX, mouseY)) {
                    hit.action().run();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.max(0.0, this.scroll - scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (AbilityKeys.isSoulKey(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
