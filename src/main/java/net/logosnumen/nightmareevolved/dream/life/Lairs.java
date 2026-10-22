package net.logosnumen.nightmareevolved.dream.life;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * The lairs of the Dream Realm's named creatures: the Carapace Demon on the Ashen Barrow, and the rest as their regions
 * come. When a soul comes near a lair whose keeper is not there, the keeper is there: once, at first, and again a week
 * after it is slain. A keeper stays close to its lair, its name shows over its head, and it never fails to leave the
 * Memory the Spell keeps for it.
 */
public final class Lairs {
    /** How near a soul must come for a lair's keeper to be there. */
    private static final double NEAR = 96.0;
    /** How long a slain keeper stays gone: seven days. */
    public static final long RETURN = 7L * 24000L;
    /** How long a keeper may be missing from its loaded lair before it is taken for lost and replaced. */
    private static final long LOST = 1200L;

    private Lairs() {
    }

    /** Where a kind's lair is: its place, offset, on the ground. */
    public static BlockPos lairOf(ServerLevel realm, Kind kind) {
        Kind.Lair lair = kind.lair;
        if (lair == null) {
            throw new IllegalArgumentException(kind.id + " keeps no lair");
        }

        int x = lair.place().x() + lair.dx();
        int z = lair.place().z() + lair.dz();
        int top = realm.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (lair.y() == Kind.Lair.BENEATH) {
            // Down through the land to its underside.
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top - 1, z);
            while (pos.getY() > realm.getMinBuildHeight() && !realm.isEmptyBlock(pos)) {
                pos.move(0, -1, 0);
            }

            return new BlockPos(x, pos.getY() - (int)Math.ceil(kind.height) - 1, z);
        }

        return new BlockPos(x, lair.y() != Kind.Lair.GROUND ? lair.y() : top, z);
    }

    static void visit(ServerLevel realm, ServerPlayer player) {
        Records records = null;
        for (Kind kind : Kinds.all()) {
            if (kind.lair == null) {
                continue;
            }

            int x = kind.lair.place().x() + kind.lair.dx();
            int z = kind.lair.place().z() + kind.lair.dz();
            if (Math.hypot(player.getX() - x, player.getZ() - z) > NEAR || !realm.isLoaded(new BlockPos(x, 64, z))) {
                continue;
            }

            if (records == null) {
                records = Records.get(realm.getServer());
            }

            Records.Keeper keeper = records.keepers.computeIfAbsent(kind.id, id -> new Records.Keeper());
            long now = realm.getGameTime();
            if (keeper.alive != null) {
                Entity entity = realm.getEntity(keeper.alive);
                if (entity != null && entity.isAlive()) {
                    keeper.missingSince = 0L;
                    continue;
                }

                if (keeper.missingSince == 0L) {
                    keeper.missingSince = now;
                }

                if (now - keeper.missingSince < LOST) {
                    continue;
                }
            }

            if (keeper.slainAt > 0L && now - keeper.slainAt < RETURN) {
                continue;
            }

            DreamCreature creature = summon(realm, kind);
            if (creature != null) {
                keeper.alive = creature.getUUID();
                keeper.slainAt = 0L;
                keeper.missingSince = 0L;
                records.setDirty();
            }
        }
    }

    /** Brings a kind's keeper to its lair now. */
    @Nullable
    public static DreamCreature summon(ServerLevel realm, Kind kind) {
        BlockPos at = lairOf(realm, kind);
        boolean aloft = kind.motion == Kind.Motion.FLY || kind.motion == Kind.Motion.HOVER || kind.motion == Kind.Motion.SWIM || kind.has(Kind.Trait.HANGING);
        DreamCreature creature = null;
        // The lair's very spot may be taken (a coral ridge, a fallen stone): look outward from it for room.
        for (int ring = 0; ring <= 8 && creature == null; ring += 2) {
            int samples = ring == 0 ? 1 : 8;
            for (int i = 0; i < samples && creature == null; i++) {
                double angle = i * Math.PI * 2.0 / samples;
                int x = at.getX() + (int)Math.round(Math.cos(angle) * ring);
                int z = at.getZ() + (int)Math.round(Math.sin(angle) * ring);
                BlockPos spot = aloft ? new BlockPos(x, at.getY(), z) : Wildlife.floor(realm, x, at.getY(), z, 4, 24);
                if (spot != null) {
                    creature = Wildlife.spawn(realm, kind, spot, MobSpawnType.STRUCTURE);
                }
            }
        }

        if (creature == null) {
            return null;
        }

        creature.keepLair(kind.id);
        Creatures.profile(creature).set(kind.rank, kind.creatureClass, true, -1L);
        creature.setCustomName(Component.translatable("creature.nightmare_evolved.named", creature.getType().getDescription(),
            Creatures.title(kind.rank, kind.creatureClass)));
        creature.setCustomNameVisible(true);
        creature.restrictTo(creature.blockPosition(), 24);
        return creature;
    }

    /** A keeper slain: its lair stands empty for a while. */
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof DreamCreature creature && creature.keepsLair() && creature.level() instanceof ServerLevel level) {
            Records records = Records.get(level.getServer());
            Records.Keeper keeper = records.keepers.computeIfAbsent(creature.kind().id, id -> new Records.Keeper());
            if (creature.getUUID().equals(keeper.alive)) {
                keeper.alive = null;
                keeper.slainAt = level.getGameTime();
                records.setDirty();
            }
        }
    }

    /** What the world knows of its lairs: who keeps each, and when each keeper was last slain. */
    static final class Records extends SavedData {
        private static final String NAME = "nightmare_evolved_lairs";
        final Map<String, Keeper> keepers = new HashMap<>();

        static final class Keeper {
            @Nullable
            UUID alive;
            long slainAt;
            long missingSince;
        }

        static Records get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(Records::new, Records::load, null), NAME);
        }

        private static Records load(CompoundTag tag, HolderLookup.Provider provider) {
            Records records = new Records();
            for (String id : tag.getAllKeys()) {
                CompoundTag k = tag.getCompound(id);
                Keeper keeper = new Keeper();
                keeper.alive = k.hasUUID("Alive") ? k.getUUID("Alive") : null;
                keeper.slainAt = k.getLong("SlainAt");
                records.keepers.put(id, keeper);
            }

            return records;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            this.keepers.forEach((id, keeper) -> {
                CompoundTag k = new CompoundTag();
                if (keeper.alive != null) {
                    k.putUUID("Alive", keeper.alive);
                }

                k.putLong("SlainAt", keeper.slainAt);
                tag.put(id, k);
            });
            return tag;
        }
    }
}
