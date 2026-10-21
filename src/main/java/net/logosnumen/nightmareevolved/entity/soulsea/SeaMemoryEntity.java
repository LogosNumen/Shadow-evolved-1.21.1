package net.logosnumen.nightmareevolved.entity.soulsea;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soulsea.SoulSea;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A Memory as a little light circling slowly over the water around the sea's heart. Touching it summons it, or, if it
 * is already out, dismisses it. One that is out circles dim and small, an echo of itself.
 */
public class SeaMemoryEntity extends SeaFigure {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(SeaMemoryEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<UUID>> MEMORY = SynchedEntityData.defineId(SeaMemoryEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> OUT = SynchedEntityData.defineId(SeaMemoryEntity.class, EntityDataSerializers.BOOLEAN);
    private double centreX;
    private double centreZ;
    private double radius;
    private double phase;

    public SeaMemoryEntity(EntityType<? extends SeaMemoryEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ITEM, ItemStack.EMPTY);
        builder.define(MEMORY, Optional.empty());
        builder.define(OUT, false);
    }

    public void set(UUID memory, ItemStack stack, boolean out) {
        this.entityData.set(MEMORY, Optional.of(memory));
        this.entityData.set(ITEM, stack.copyWithCount(1));
        this.entityData.set(OUT, out);
        this.setCustomName(stack.getHoverName());
    }

    /** Sets the circle it travels: around the sea's heart, at a distance, starting at an angle. */
    public void orbit(double centreX, double centreZ, double radius, double phase) {
        this.centreX = centreX;
        this.centreZ = centreZ;
        this.radius = radius;
        this.phase = phase;
    }

    public ItemStack item() {
        return this.entityData.get(ITEM);
    }

    @Nullable
    public UUID memory() {
        return this.entityData.get(MEMORY).orElse(null);
    }

    public boolean out() {
        return this.entityData.get(OUT);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel && this.radius > 0.0) {
            // A full circle every two minutes.
            double angle = this.phase + this.tickCount * (Math.PI * 2.0 / 2400.0);
            this.setPos(this.centreX + Math.cos(angle) * this.radius, SoulSea.SURFACE_Y + 1.6 + Math.sin(this.tickCount * 0.03 + this.phase) * 0.2,
                this.centreZ + Math.sin(angle) * this.radius);
        }
    }

    @Override
    protected void touched(ServerPlayer owner) {
        UUID id = this.memory();
        MemoryRecord record = id == null ? null : Soul.of(owner).memory(id);
        if (record == null) {
            return;
        }

        if (record.anyOut()) {
            SoulMemories.dismiss(owner, id, -1, false);
        } else {
            SoulMemories.summon(owner, id, -1);
        }
    }
}
