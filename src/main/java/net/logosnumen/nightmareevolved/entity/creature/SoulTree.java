package net.logosnumen.nightmareevolved.entity.creature;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Soul Devouring Tree on the Ashen Barrow, whose black branches shade the whole barrow. Its hexes take hold of the
 * minds that come near: they forget why they should leave, and drift toward it to tend it, and it feeds on them. Those
 * who stand and look at it too long are drawn in.
 */
public final class SoulTree extends DreamCreature {
    /** How far its hex reaches. */
    private static final double HEX = 24.0;

    public SoulTree(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 40 != 0 || !this.isAlive()) {
            return;
        }

        // Whoever looks at it from near enough begins to be drawn in, a little at a time.
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(HEX * 0.6), this::canStrike)) {
            Vec3 toTree = this.getEyePosition().subtract(player.getEyePosition()).normalize();
            if (player.getViewVector(1.0F).dot(toTree) > 0.85) {
                this.draw(player, 0.15, 60);
            }
        }
    }

    @Override
    protected void special(ServerLevel level, Move move, int which, @Nullable LivingEntity target) {
        if (!"enthrall".equals(move.id())) {
            return;
        }

        for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(HEX), this::canStrike)) {
            this.draw(player, 0.4, 140);
            player.hurt(this.damageSources().indirectMagic(this, this), this.blow(0.5F));
            Vfx.beam(level, this.getEyePosition(), player.getEyePosition(), Vfx.Palette.BLOOD, 0.6);
        }
    }

    /** Pulls a mind toward the tree, dulling it. */
    private void draw(Player player, double pull, int ticks) {
        Vec3 toward = this.position().subtract(player.position()).multiply(1.0, 0.0, 1.0).normalize().scale(pull);
        player.push(toward.x, 0.0, toward.z);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, ticks, 0), this);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks / 2, 1), this);
    }
}
