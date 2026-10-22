package net.logosnumen.nightmareevolved.entity.creature;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.dream.life.Wildlife;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

/**
 * A Millipede Queen of the Burned Forest: enormous, armoured in chitin nothing pierces, mother of an endless brood. In a
 * fight she does not stay wholly in one moment: she slips into the space between two of them and back, and while she
 * is there, blows pass through where she seems to be.
 */
public final class MillipedeQueen extends DreamCreature {
    private static final EntityDataAccessor<Boolean> BETWEEN = SynchedEntityData.defineId(MillipedeQueen.class, EntityDataSerializers.BOOLEAN);
    /** How long she stays in each moment, and how long between them. */
    private static final int PRESENT = 50;
    private static final int APART = 30;
    /** How many of her young may swarm around her before she stops bringing forth more. */
    private static final int BROOD = 14;
    private int shift;

    public MillipedeQueen(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BETWEEN, false);
    }

    /** Whether she is between two moments now. */
    public boolean between() {
        return this.entityData.get(BETWEEN);
    }

    @Override
    public int tint() {
        return this.between() ? 0x70B8C8FF : 0xFFFFFFFF;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.between() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        boolean fighting = this.getTarget() != null;
        if (++this.shift >= (this.between() ? APART : PRESENT) || this.between() && !fighting) {
            this.shift = 0;
            boolean apart = fighting && !this.between();
            this.entityData.set(BETWEEN, apart);
            ServerLevel level = (ServerLevel)this.level();
            Vfx.flash(level, this.position().add(0.0, this.getBbHeight() * 0.5, 0.0), Vfx.Palette.RUNE, 3.0F);
            this.playSound(apart ? SoundEvents.ENDERMAN_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT, 1.5F, 0.5F);
        }
    }

    @Override
    protected void special(ServerLevel level, Move move, int which, @Nullable LivingEntity target) {
        // Her brood: young come forth from beneath her to join the swarm.
        Kind young = Kinds.byId("black_millipede");
        if (young == null || level.getEntitiesOfClass(DreamCreature.class, this.getBoundingBox().inflate(32.0), c -> c.kind() == young).size() >= BROOD) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            BlockPos at = BlockPos.containing(this.getX() + Math.cos(angle) * 4.0, this.getY(), this.getZ() + Math.sin(angle) * 4.0);
            BlockPos floor = Wildlife.floor(level, at.getX(), at.getY(), at.getZ(), 4, 6);
            DreamCreature child = floor == null ? null : Wildlife.spawn(level, young, floor, MobSpawnType.MOB_SUMMONED);
            if (child != null) {
                child.setTarget(target);
                Vfx.smoke(level, child.position().add(0.0, 0.4, 0.0), Vfx.Palette.SHADOW.deep, 12, 0.4);
            }
        }

        this.playSound(SoundEvents.SILVERFISH_AMBIENT, 2.0F, 0.4F);
    }
}
