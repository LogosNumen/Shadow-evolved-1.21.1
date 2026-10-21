package net.logosnumen.nightmareevolved.dream.law;

import java.util.ArrayList;
import java.util.List;
import net.logosnumen.nightmareevolved.dream.DreamBlocks;
import net.logosnumen.nightmareevolved.dream.DreamDamage;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Godgrave's sky: a veil of storm clouds that never lifts, and so no night. When the veil tears, the radiant white sky
 * behind it erases whatever stands under it into ash, with no exception and no salvation; then the storm closes the
 * wound. No one can say when it will tear, so life here grows at a frantic pace: ground the white sky burned bare is
 * green (vermilion) again within minutes.
 */
public final class Cloudveil {
    /** Ticks between the veil thinning and the white sky breaking through. */
    public static final int WARNING = 100;
    /** Ticks the white sky shines through before the storm closes it. */
    public static final int BURN = 160;
    /** How soon what burned grows back. */
    private static final int REGROWTH = 2400;
    private static final List<Tear> TEARS = new ArrayList<>();
    private static final List<Scar> SCARS = new ArrayList<>();

    /** A tear in the veil: where, how wide, and when it opened. */
    public record Tear(double x, double z, float radius, long start) {
        public boolean burning(long now) {
            return now >= this.start + WARNING && now < this.start + WARNING + BURN;
        }

        public boolean over(long now) {
            return now >= this.start + WARNING + BURN;
        }
    }

    private record Scar(BlockPos pos, long at) {
    }

    private Cloudveil() {
    }

    public static List<Tear> tears() {
        return TEARS;
    }

    /** Whether a place lies under open, burning white sky right now. */
    public static boolean burning(ServerLevel level, double x, double y, double z) {
        long now = level.getGameTime();
        for (Tear tear : TEARS) {
            double dx = x - tear.x;
            double dz = z - tear.z;
            if (tear.burning(now) && dx * dx + dz * dz < tear.radius * tear.radius && level.canSeeSky(BlockPos.containing(x, y + 1.0, z))) {
                return true;
            }
        }

        return false;
    }

    /** Runs the veil: tears open near the living now and then, burn, and close; what burned grows back. Returns true when the tears changed. */
    public static boolean tick(ServerLevel level, List<ServerPlayer> players) {
        long now = level.getGameTime();
        boolean changed = TEARS.removeIf(tear -> tear.over(now));
        RandomSource random = level.random;
        if (now % 200 == 0) {
            for (ServerPlayer player : players) {
                if (RealmMap.part(player.getBlockX(), player.getBlockY(), player.getBlockZ()) != Region.GODGRAVE || random.nextInt(14) != 0) {
                    continue;
                }

                boolean near = TEARS.stream().anyMatch(t -> Math.hypot(t.x - player.getX(), t.z - player.getZ()) < 200.0);
                if (!near) {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    double r = 16.0 + random.nextDouble() * 50.0;
                    TEARS.add(new Tear(player.getX() + Math.cos(a) * r, player.getZ() + Math.sin(a) * r, 14.0F + random.nextFloat() * 10.0F, now));
                    level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.AMBIENT, 1.2F, 0.3F);
                    changed = true;
                }
            }
        }

        for (Tear tear : TEARS) {
            if (now == tear.start + WARNING) {
                scorch(level, tear, now);
                level.playSound(null, BlockPos.containing(tear.x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)tear.x, (int)tear.z), tear.z),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 4.0F, 0.5F);
            }

            if (tear.burning(now) && now % 10 == 0) {
                erase(level, tear);
            }
        }

        SCARS.removeIf(scar -> {
            if (now < scar.at + REGROWTH) {
                return false;
            }

            if (level.isLoaded(scar.pos) && level.getBlockState(scar.pos).is(DreamBlocks.ASH.get())) {
                level.setBlock(scar.pos, DreamBlocks.VERMILION_TURF.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                if (level.getBlockState(scar.pos.above()).isAir() && level.random.nextInt(3) == 0) {
                    level.setBlock(scar.pos.above(), DreamBlocks.VERMILION_GRASS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }

            return true;
        });
        return changed;
    }

    /** Everything that lives under the tear and can see the sky is erased. */
    private static void erase(ServerLevel level, Tear tear) {
        AABB box = new AABB(tear.x - tear.radius, level.getMinBuildHeight(), tear.z - tear.radius, tear.x + tear.radius, level.getMaxBuildHeight(),
            tear.z + tear.radius);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = living.getX() - tear.x;
            double dz = living.getZ() - tear.z;
            if (dx * dx + dz * dz < tear.radius * tear.radius && level.canSeeSky(BlockPos.containing(living.getX(), living.getEyeY(), living.getZ()))) {
                living.invulnerableTime = 0;
                living.hurt(DreamDamage.source(level, DreamDamage.WHITE_SKY), 7.0F);
                level.sendParticles(ParticleTypes.WHITE_ASH, living.getX(), living.getY() + 1.0, living.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
            }
        }
    }

    /** The growth under the tear burns away to ash, to grow back soon. */
    private static void scorch(ServerLevel level, Tear tear, long now) {
        int r = (int)Math.ceil(tear.radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > tear.radius * tear.radius) {
                    continue;
                }

                int x = (int)Math.floor(tear.x) + dx;
                int z = (int)Math.floor(tear.z) + dz;
                if (!level.isLoaded(pos.set(x, 0, z))) {
                    continue;
                }

                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                pos.set(x, y, z);
                BlockState above = level.getBlockState(pos);
                if (above.is(DreamBlocks.VERMILION_GRASS.get()) || above.is(DreamBlocks.RED_MOSS_CARPET.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }

                pos.set(x, y - 1, z);
                if (level.getBlockState(pos).is(DreamBlocks.VERMILION_TURF.get())) {
                    level.setBlock(pos, DreamBlocks.ASH.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    SCARS.add(new Scar(pos.immutable(), now));
                }
            }
        }
    }

    /** Tears the veil open now, at a place (for operators). */
    public static void open(ServerLevel level, double x, double z, float radius) {
        TEARS.add(new Tear(x, z, radius, level.getGameTime()));
    }

    public static void reset() {
        TEARS.clear();
        SCARS.clear();
    }
}
