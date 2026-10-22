package net.logosnumen.nightmareevolved.entity.creature;

import java.util.Set;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * A Chalice Knight of Ravenheart: a towering knight in rust-green armour under a helm shaped like a beast's head. It
 * fights with its blade until it is hard pressed, and then the beast inside it bursts out through the armour: faster,
 * fiercer, and far less guarded.
 */
public final class ChaliceKnight extends DreamCreature {
    private static final EntityDataAccessor<Boolean> BEAST = SynchedEntityData.defineId(ChaliceKnight.class, EntityDataSerializers.BOOLEAN);
    /** The moves only the beast makes; the rest are the knight's. */
    private static final Set<String> BEAST_MOVES = Set.of("rend", "maul", "pounce");
    private static final ResourceLocation SPEED = NightmareEvolved.id("chalice_beast_speed");
    private static final ResourceLocation FURY = NightmareEvolved.id("chalice_beast_fury");
    private static final ResourceLocation BARE = NightmareEvolved.id("chalice_beast_bare");

    public ChaliceKnight(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEAST, false);
    }

    /** Whether the beast has burst out of the armour. */
    @Override
    public boolean roused() {
        return this.entityData.get(BEAST);
    }

    @Override
    protected int weight(Move move) {
        return BEAST_MOVES.contains(move.id()) == this.roused() ? super.weight(move) : 0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && !this.roused() && this.isAlive() && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.burst();
        }

        return hurt;
    }

    private void burst() {
        this.entityData.set(BEAST, true);
        modify(this.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, 0.35);
        modify(this.getAttribute(Attributes.ATTACK_DAMAGE), FURY, 0.3);
        modify(this.getAttribute(Attributes.ARMOR), BARE, -0.4);
        ServerLevel level = (ServerLevel)this.level();
        Vfx.burst(level, this.position().add(0.0, this.getBbHeight() * 0.6, 0.0), Vfx.Palette.BLOOD, 24, 0.5);
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.4F, 1.2F);
        this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 0.6F);
    }

    private static void modify(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute != null && !attribute.hasModifier(id)) {
            attribute.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Beast", this.roused());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(BEAST, tag.getBoolean("Beast"));
    }
}
