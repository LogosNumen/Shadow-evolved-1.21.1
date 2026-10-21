package net.logosnumen.nightmareevolved.entity.soulsea;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.echo.EchoRecord;
import net.logosnumen.nightmareevolved.echo.EchoTrait;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Something standing still on the water in a creature's shape: an Echo resting in its owner's soul (a Shadow, if
 * Sunny's Aspect has made it one), or, in Sunny's sea, the shadow of something he slew, standing guard over his soul.
 * Touching one reads out what it is.
 */
public class SeaEchoEntity extends SeaFigure {
    public static final int ECHO_STYLE = 0;
    public static final int SHADOW_STYLE = 1;
    public static final int SLAIN_STYLE = 2;
    private static final EntityDataAccessor<String> KIND = SynchedEntityData.defineId(SeaEchoEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> ECHO = SynchedEntityData.defineId(SeaEchoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> OUT = SynchedEntityData.defineId(SeaEchoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(SeaEchoEntity.class, EntityDataSerializers.INT);

    public SeaEchoEntity(EntityType<? extends SeaEchoEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, "");
        builder.define(ECHO, Optional.empty());
        builder.define(OUT, false);
        builder.define(STYLE, ECHO_STYLE);
    }

    /** What it is: a creature kind, the Echo it stands for (if any), whether that Echo is out, and its style. */
    public void set(String kind, @Nullable UUID echo, boolean out, int style) {
        this.entityData.set(KIND, kind);
        this.entityData.set(ECHO, Optional.ofNullable(echo));
        this.entityData.set(OUT, out);
        this.entityData.set(STYLE, style);
    }

    @Nullable
    public EntityType<?> kind() {
        return EntityType.byString(this.entityData.get(KIND)).orElse(null);
    }

    public String kindId() {
        return this.entityData.get(KIND);
    }

    @Nullable
    public UUID echo() {
        return this.entityData.get(ECHO).orElse(null);
    }

    public boolean out() {
        return this.entityData.get(OUT);
    }

    public int style() {
        return this.entityData.get(STYLE);
    }

    @Override
    protected void touched(ServerPlayer owner) {
        EntityType<?> kind = this.kind();
        Component name = kind == null ? Component.literal(this.kindId()) : kind.getDescription();
        if (this.style() == SLAIN_STYLE) {
            owner.displayClientMessage(Component.translatable("soul_sea.nightmare_evolved.slain_shadow", name).withStyle(ChatFormatting.DARK_GRAY), true);
            return;
        }

        EchoRecord record = this.echo() == null ? null : Soul.of(owner).echo(this.echo());
        if (record == null) {
            return;
        }

        MutableComponent traits = Component.empty();
        for (String id : record.traits) {
            EchoTrait trait = EchoTrait.byId(id);
            if (trait != null) {
                traits.append(" [").append(trait.title()).append("]");
            }
        }

        CreatureRank rank = CreatureRank.values()[Math.max(0, Math.min(6, record.rank))];
        CreatureClass cls = CreatureClass.values()[Math.max(0, Math.min(6, record.cls))];
        String key = record.shadow ? "soul_sea.nightmare_evolved.shadow_runes" : "soul_sea.nightmare_evolved.echo_runes";
        owner.displayClientMessage(Component.translatable(key, Echoes.name(record), rank.title(), cls.title(), Math.round(record.health * 100.0F))
            .append(traits).withStyle(ChatFormatting.GRAY), true);
    }
}
