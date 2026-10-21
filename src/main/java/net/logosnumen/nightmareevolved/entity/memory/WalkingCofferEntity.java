package net.logosnumen.nightmareevolved.entity.memory;

import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryStorage;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Covetous Coffer, set down to walk after its owner on its own. Its owner can open it, or (crouching) take it back
 * up; anyone else who tries to open it is bitten.
 */
public final class WalkingCofferEntity extends PathfinderMob {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(WalkingCofferEntity.class, EntityDataSerializers.ITEM_STACK);

    public WalkingCofferEntity(EntityType<? extends WalkingCofferEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public ItemStack getItem() {
        return this.entityData.get(ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(ITEM, stack.copyWithCount(1));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }

        if (this.tickCount % 20 == 0 && SoulMemories.checkLoose(this.getItem(), this)) {
            return;
        }

        MemoryData data = SoulMemories.data(this.getItem());
        ServerPlayer owner = data == null ? null : level.getServer().getPlayerList().getPlayer(data.owner());
        if (owner == null || owner.level() != level || this.tickCount % 10 != 0) {
            return;
        }

        double distance = this.distanceToSqr(owner);
        if (distance > 24.0 * 24.0) {
            this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        } else if (distance > 3.0 * 3.0) {
            this.getNavigation().moveTo(owner, 1.2);
        } else {
            this.getNavigation().stop();
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer server)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = this.getItem();
        MemoryData data = SoulMemories.data(stack);
        if (data == null) {
            return InteractionResult.PASS;
        }

        if (!server.getUUID().equals(data.owner())) {
            if (data.has(Enchant.LURID_TRUNK)) {
                server.hurt(this.damageSources().mobAttack(this), (float)(4.0 + 2.0 * SoulMemories.power(server, data)));
                server.displayClientMessage(Component.translatable("memory.nightmare_evolved.covetous_coffer.bitten"), true);
                this.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.0F, 0.8F);
            }

            return InteractionResult.CONSUME;
        }

        if (server.isShiftKeyDown()) {
            if (!server.getInventory().add(stack.copy())) {
                server.drop(stack.copy(), false);
            }

            Vfx.burst((ServerLevel)this.level(), this.position().add(0.0, 0.5, 0.0), Vfx.Palette.EARTH, 8, 0.05);
            this.discard();
            return InteractionResult.CONSUME;
        }

        MemoryStorage.openCoffer(server, this);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.getItem().isEmpty()) {
            tag.put("Coffer", this.getItem().save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setItem(ItemStack.parse(this.registryAccess(), tag.getCompound("Coffer")).orElse(ItemStack.EMPTY));
    }
}
