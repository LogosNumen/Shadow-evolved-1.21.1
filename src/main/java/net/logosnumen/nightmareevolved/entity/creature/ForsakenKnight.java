package net.logosnumen.nightmareevolved.entity.creature;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Forsaken Knight, the Black Knight of the Ruined Cathedral. The darkness of the cathedral's hall obeys him: while
 * he stands in it his wounds close as fast as they are dealt, and he can step through it to stand behind his prey. He
 * never goes past the cathedral's doorway.
 */
public final class ForsakenKnight extends DreamCreature {
    /** How dark it must be for the darkness to answer him. */
    private static final int DARK = 5;

    public ForsakenKnight(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    private boolean inDarkness() {
        return this.level().getMaxLocalRawBrightness(this.blockPosition()) <= DARK;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 10 == 0 && this.inDarkness() && this.getHealth() < this.getMaxHealth() && this.isAlive()) {
            this.heal(6.0F);
            Vfx.smoke((ServerLevel)this.level(), this.position().add(0.0, 1.2, 0.0), Vfx.Palette.SHADOW.deep, 6, 0.4);
        }
    }

    @Override
    protected int weight(Move move) {
        if ("shadow_step".equals(move.id())) {
            return this.inDarkness() ? move.weight() : 0;
        }

        return move.weight();
    }

    @Override
    protected void special(ServerLevel level, Move move, int which, @Nullable LivingEntity target) {
        if (!"shadow_step".equals(move.id()) || target == null) {
            return;
        }

        // Out of the darkness behind his prey, without a sound.
        Vec3 behind = target.position().subtract(Vec3.directionFromRotation(0.0F, target.getYRot()).scale(1.8));
        Vfx.smoke(level, this.position().add(0.0, 1.0, 0.0), Vfx.Palette.SHADOW.deep, 24, 0.6);
        if (level.noCollision(this, this.getBoundingBox().move(behind.subtract(this.position())))) {
            this.teleportTo(behind.x, behind.y, behind.z);
            this.turnToward(target.getX(), target.getZ(), 180.0F);
            Vfx.smoke(level, behind.add(0.0, 1.0, 0.0), Vfx.Palette.SHADOW.deep, 24, 0.6);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.4F, 0.4F);
        }
    }
}
