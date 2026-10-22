package net.logosnumen.nightmareevolved.entity.creature;

import java.util.Optional;
import java.util.UUID;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The creature of Reckoning Island, that the Sanctuary's people say twists the sight of those who meet it. It becomes a
 * copy of its foe: their look, the weapon in their hand, and the strength of their blows, while staying as strong as
 * a Fallen Devil. Left alone a while, it loses the shape it took.
 */
public final class MirrorBeast extends DreamCreature {
    private static final EntityDataAccessor<Optional<UUID>> MIRRORED = SynchedEntityData.defineId(MirrorBeast.class, EntityDataSerializers.OPTIONAL_UUID);
    /** How long it keeps a shape with no one to fight. */
    private static final int FORGET = 300;
    private int alone;

    public MirrorBeast(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level, kind);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MIRRORED, Optional.empty());
    }

    /** Whose shape it wears, if anyone's. */
    public Optional<UUID> mirrored() {
        return this.entityData.get(MIRRORED);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = this.getTarget();
        if (target instanceof Player player && player.isAlive()) {
            this.alone = 0;
            if (!this.mirrored().equals(Optional.of(player.getUUID()))) {
                this.become(player);
            }
        } else if (this.mirrored().isPresent() && ++this.alone > FORGET) {
            this.entityData.set(MIRRORED, Optional.empty());
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            Vfx.flash((ServerLevel)this.level(), this.position().add(0.0, 1.0, 0.0), Vfx.Palette.STEEL, 1.5F);
        }
    }

    /** Takes a player's shape and arms. */
    private void become(Player player) {
        this.entityData.set(MIRRORED, Optional.of(player.getUUID()));
        this.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem().copy());
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        Vfx.flash((ServerLevel)this.level(), this.position().add(0.0, 1.0, 0.0), Vfx.Palette.STEEL, 2.0F);
        this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.5F, 0.6F);
    }

    /** Its blows land as hard as its foe's would, if that is harder than its own. */
    @Override
    protected float blow(float factor) {
        float own = super.blow(factor);
        if (this.getTarget() instanceof Player player && this.mirrored().equals(Optional.of(player.getUUID()))) {
            return Math.max(own, (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor);
        }

        return own;
    }
}
