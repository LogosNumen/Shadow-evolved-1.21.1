package net.logosnumen.nightmareevolved.entity.soulsea;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.dream.DreamTravel;
import net.logosnumen.nightmareevolved.soulsea.SoulSea;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;

/**
 * A body left behind by its soul: sitting still while its mind is down in its Soul Sea, or asleep in its bed while
 * its soul is in the Dream Realm. It draws the eyes of whatever hunts nearby, and the moment it is struck, its soul is
 * torn back into it.
 */
public final class VesselEntity extends LivingEntity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(VesselEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DREAMING = SynchedEntityData.defineId(VesselEntity.class, EntityDataSerializers.BOOLEAN);
    private int away;

    public VesselEntity(EntityType<? extends VesselEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
        builder.define(DREAMING, false);
    }

    @Nullable
    public UUID owner() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setOwner(Player player) {
        this.entityData.set(OWNER, Optional.of(player.getUUID()));
        this.setCustomName(Component.translatable("soul_sea.nightmare_evolved.vessel", player.getDisplayName()));
    }

    /** Whether this body lies asleep while its soul is in the Dream Realm, rather than sitting while it is in its Soul Sea. */
    public boolean dreaming() {
        return this.entityData.get(DREAMING);
    }

    /** Lays the body down asleep in its bed, its soul away in the Dream Realm. */
    public void dream(Player owner, BlockPos bed) {
        this.entityData.set(DREAMING, true);
        this.setCustomName(Component.translatable("dream.nightmare_evolved.sleeper", owner.getDisplayName()));
        if (this.level().getBlockState(bed).getBlock() instanceof BedBlock) {
            this.startSleeping(bed);
        }
    }

    /** The soul came back: the body rises from its bed and is gone, its owner standing in its place. */
    public void wakeAndVanish() {
        if (this.isSleeping()) {
            this.stopSleeping();
        }

        this.discard();
    }

    private boolean away(ServerPlayer player) {
        return this.dreaming() ? DreamTravel.dreaming(player) : SoulSea.in(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level) || this.tickCount % 20 != 0) {
            return;
        }

        UUID owner = this.owner();
        ServerPlayer player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (player != null && !this.away(player)) {
            // Its mind came back some other way (a respawn, a command); the body has no reason to sit here.
            if (++this.away > 5) {
                this.wakeAndVanish();
            }

            return;
        }

        this.away = 0;
        if (player != null) {
            SoulSea.lure(this);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isInvulnerableTo(source)) {
            return false;
        }

        if (this.dreaming() ? DreamTravel.struck(this, source, amount) : SoulSea.struck(this, source, amount)) {
            this.wakeAndVanish();
        }

        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID owner = this.owner();
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }

        tag.putBoolean("Dreaming", this.dreaming());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(OWNER, tag.hasUUID("Owner") ? Optional.of(tag.getUUID("Owner")) : Optional.empty());
        this.entityData.set(DREAMING, tag.getBoolean("Dreaming"));
    }
}
