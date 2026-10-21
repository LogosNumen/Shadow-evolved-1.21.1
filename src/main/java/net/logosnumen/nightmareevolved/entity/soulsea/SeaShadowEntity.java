package net.logosnumen.nightmareevolved.entity.soulsea;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * One of Sunny's own shadows, standing silent beside him in his sea. It wears his shape, turns as he turns, and never
 * says anything aloud; touched, it only seems to.
 */
public class SeaShadowEntity extends SeaFigure {
    public static final int WHISPERS = 6;

    public SeaShadowEntity(EntityType<? extends SeaShadowEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void touched(ServerPlayer owner) {
        int line = this.random.nextInt(WHISPERS);
        owner.displayClientMessage(Component.translatable("soul_sea.nightmare_evolved.shadow." + line).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
        this.level().playSound(null, this.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.5F, 0.6F);
    }
}
