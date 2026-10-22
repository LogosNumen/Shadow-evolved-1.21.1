package net.logosnumen.nightmareevolved.entity.creature;

import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * A being of the mist of the Hollow Mountains. It cannot be seen for the fog, only heard: it speaks in a human voice,
 * calling, pleading, trying to coax whoever hears it into opening their eyes and looking at it.
 */
public final class MistBeing extends DreamCreature {
    /** How many things it says (lang keys {@code creature.nightmare_evolved.mist_being.voice.N}). */
    private static final int VOICES = 8;

    public MistBeing(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 160 != 0 || this.random.nextInt(2) != 0) {
            return;
        }

        int line = this.random.nextInt(VOICES);
        for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(24.0), this::canStrike)) {
            player.displayClientMessage(Component.translatable("creature.nightmare_evolved.mist_being.voice." + line)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        }
    }
}
