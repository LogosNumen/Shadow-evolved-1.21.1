package net.logosnumen.nightmareevolved.entity.soulsea;

import net.logosnumen.nightmareevolved.memory.MemoryStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Where a Sovereign's Soul Sea keeps the things stored in it. Touching it opens the store. */
public class SeaHoardEntity extends SeaFigure {
    public SeaHoardEntity(EntityType<? extends SeaHoardEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void touched(ServerPlayer owner) {
        MemoryStorage.openVault(owner);
    }
}
