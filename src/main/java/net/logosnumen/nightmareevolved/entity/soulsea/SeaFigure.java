package net.logosnumen.nightmareevolved.entity.soulsea;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.soulsea.SoulSea;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Something that lives in a soul's sea: one of its suns, a Memory, a resting Echo, a shadow. Figures are never saved
 * with the world; the sea is rebuilt from the soul whenever its owner comes down into it, so the soul is always the one
 * source of truth. A figure can't be hurt or moved, and only its owner can touch it.
 */
public abstract class SeaFigure extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(SeaFigure.class, EntityDataSerializers.OPTIONAL_UUID);

    protected SeaFigure(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
    }

    @Nullable
    public UUID getOwner() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setOwner(UUID owner) {
        this.entityData.set(OWNER, Optional.ofNullable(owner));
    }

    public boolean ownedBy(Player player) {
        return player.getUUID().equals(this.getOwner());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.tickCount % 40 == 0) {
            // A figure only exists while its owner is down in their sea.
            ServerPlayer owner = this.getOwner() == null ? null : level.getServer().getPlayerList().getPlayer(this.getOwner());
            if (owner == null || !SoulSea.in(owner) || owner.distanceToSqr(this) > 160.0 * 160.0) {
                this.discard();
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !this.ownedBy(player)) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer server) {
            this.touched(server);
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** The owner touches this figure. Server side. */
    protected abstract void touched(ServerPlayer owner);

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    /** Whether an entity is a figure of this player's sea. */
    public static boolean of(Entity entity, Player player) {
        return entity instanceof SeaFigure figure && figure.ownedBy(player);
    }
}
