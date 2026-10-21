package net.logosnumen.nightmareevolved.entity.soulsea;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * One of a soul's cores, burning over its sea like a sun. It grows and brightens as it fills. Most are bright suns in
 * the colour of their Aspect; Sunny's Shadow Core is a black sun that gives no light, and Mordret's is pale and marked.
 * Touching it reads out how full it is.
 */
public class SeaSunEntity extends SeaFigure {
    private static final EntityDataAccessor<Integer> INDEX = SynchedEntityData.defineId(SeaSunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COUNT = SynchedEntityData.defineId(SeaSunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FILL = SynchedEntityData.defineId(SeaSunEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> LOOK = SynchedEntityData.defineId(SeaSunEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> COLOUR = SynchedEntityData.defineId(SeaSunEntity.class, EntityDataSerializers.INT);

    public SeaSunEntity(EntityType<? extends SeaSunEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(INDEX, 0);
        builder.define(COUNT, 1);
        builder.define(FILL, 0.0F);
        builder.define(LOOK, "");
        builder.define(COLOUR, 0xFFE6B0);
    }

    /** Which core, of how many, how full, and how it looks: "shadow", "nothing", "light" or plain. */
    public void set(int index, int count, float fill, String look, int colour) {
        this.entityData.set(INDEX, index);
        this.entityData.set(COUNT, count);
        this.entityData.set(FILL, Mth.clamp(fill, 0.0F, 1.0F));
        this.entityData.set(LOOK, look);
        this.entityData.set(COLOUR, colour);
    }

    public int index() {
        return this.entityData.get(INDEX);
    }

    public int count() {
        return this.entityData.get(COUNT);
    }

    public float fill() {
        return this.entityData.get(FILL);
    }

    public String look() {
        return this.entityData.get(LOOK);
    }

    public int colour() {
        return this.entityData.get(COLOUR);
    }

    @Override
    protected void touched(ServerPlayer owner) {
        int percent = Math.round(this.fill() * 100.0F);
        String key = this.fill() >= 1.0F ? "soul_sea.nightmare_evolved.core_full" : "soul_sea.nightmare_evolved.core_fill";
        owner.displayClientMessage(Component.translatable(key, this.index() + 1, this.count(), percent).withStyle(ChatFormatting.AQUA), true);
        this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.7F + 0.6F * this.fill());
    }
}
