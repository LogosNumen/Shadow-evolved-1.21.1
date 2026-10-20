package net.logosnumen.nightmareevolved.vfx;

import java.util.function.IntConsumer;
import net.logosnumen.nightmareevolved.core.Scheduler;
import net.logosnumen.nightmareevolved.vfx.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Shapes for ability effects, drawn with the mod's tinted particles: bursts, wave rings, rune circles, blade arcs,
 * beams, pillars, domes, cones, spirals, wings and smoke. Most are drawn in light and many unfold over a few ticks, so
 * a cut sweeps, a wave races out and a pillar climbs. Every Element has its own palette, so an effect reads as the
 * Aspect that cast it.
 */
public final class Vfx {
    /** An Element's colours: its main hue, a bright core, and a deep tone for shadows and smoke. */
    public enum Palette {
        SHADOW(0xA569FF, 0xE6D2FF, 0x2A1B44),
        RUNE(0x78D2FF, 0xE6F8FF, 0x2E6E96),
        LIGHT(0xFFCD6E, 0xFFF6DC, 0xFF823C),
        DRAGON(0xBE6EFF, 0xFF9A3C, 0x3C1E5A),
        SOUL(0x5AEBD2, 0xD2FFF5, 0x1E6E64),
        FIRE(0xFF8A3C, 0xFFE6A0, 0x9A2A14),
        STEEL(0xC8CCD8, 0xFFFFFF, 0x50525E),
        EARTH(0xFFA845, 0xFFE2A8, 0x4A3420),
        ACID(0x9CFF5A, 0xE6FFC8, 0x2E5A1E),
        BLOOD(0xFF4A4A, 0xFFC8B8, 0x5A0E18),
        FROST(0x8AD8FF, 0xEAF8FF, 0x2A5A7A);

        public final int main;
        public final int bright;
        public final int deep;

        Palette(int main, int bright, int deep) {
            this.main = main;
            this.bright = bright;
            this.deep = deep;
        }
    }

    private Vfx() {
    }

    private static void emit(ServerLevel level, TintedParticleOptions.Type type, int colour, float scale, double x, double y, double z, double vx, double vy, double vz) {
        level.sendParticles(new TintedParticleOptions(type, colour, scale, 0), x, y, z, 0, vx, vy, vz, 1.0);
    }

    private static void flat(ServerLevel level, TintedParticleOptions.Type type, int colour, double radius, int lifetime, Vec3 at, float yaw, float roll) {
        level.sendParticles(new TintedParticleOptions(type, colour, (float)radius, lifetime, yaw, roll), at.x, at.y, at.z, 0, 0, 0, 0, 1.0);
    }

    /** Runs {@code step} once a tick for {@code ticks} ticks, starting next tick, with the tick number (0 first). */
    public static void over(int ticks, IntConsumer step) {
        Scheduler.scheduleRecurring(step, ticks);
    }

    /** Runs an effect after a delay. */
    public static void later(int ticks, Runnable effect) {
        Scheduler.schedule(effect, ticks);
    }

    /** The middle of an entity's body, where most effects belong. */
    public static Vec3 chest(Entity entity) {
        return entity.position().add(0.0, entity.getBbHeight() * 0.6, 0.0);
    }

    /** An entity's facing as a flat particle's yaw. */
    public static float yawOf(Entity entity) {
        return entity.getYRot() * Mth.DEG_TO_RAD;
    }

    /** One glowing mote, drifting up at the given speed: for abilities that draw their own shapes point by point. */
    public static void mote(ServerLevel level, Vec3 at, int colour, double rise) {
        emit(level, ModParticles.MOTE.get(), colour, 1.0F, at.x, at.y, at.z, 0, rise, 0);
    }

    /** A larger, brighter mote: a point that should catch the eye. */
    public static void glow(ServerLevel level, Vec3 at, int colour, float size) {
        emit(level, ModParticles.MOTE.get(), colour, size, at.x, at.y, at.z, 0, 0, 0);
    }

    /** One spark flying off at the given velocity; fast sparks draw as streaks. */
    public static void spark(ServerLevel level, Vec3 at, int colour, Vec3 velocity) {
        emit(level, ModParticles.SPARK.get(), colour, 1.0F, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z);
    }

    /** One floating rune. */
    public static void glyph(ServerLevel level, Vec3 at, int colour) {
        emit(level, ModParticles.GLYPH.get(), colour, 1.4F, at.x, at.y, at.z, 0, 0.015, 0);
    }

    /** A burst outward from a point: glowing motes and streaking sparks, with a flash at its heart. */
    public static void burst(ServerLevel level, Vec3 at, Palette palette, int count, double speed) {
        RandomSource random = level.random;
        flash(level, at, palette, 1.0F);
        for (int i = 0; i < count; i++) {
            Vec3 dir = randomDirection(random).scale(speed * (0.5 + random.nextDouble() * 0.7));
            boolean spark = i % 2 == 0;
            emit(level, spark ? ModParticles.SPARK.get() : ModParticles.MOTE.get(), spark ? palette.bright : palette.main,
                spark ? 1.2F : 1.1F, at.x, at.y, at.z, dir.x, dir.y, dir.z);
        }
    }

    /** A ring of light that grows from a point and fades, with a bright core: a hit landing, a spell taking hold. */
    public static void flash(ServerLevel level, Vec3 at, Palette palette, float size) {
        emit(level, ModParticles.PULSE.get(), palette.main, size, at.x, at.y, at.z, 0, 0, 0);
        emit(level, ModParticles.MOTE.get(), palette.bright, 2.2F * size, at.x, at.y, at.z, 0, 0, 0);
        emit(level, ModParticles.MOTE.get(), palette.main, 3.6F * size, at.x, at.y, at.z, 0, 0, 0);
    }

    /** A circle of light lying on the ground (or tipped by {@code roll}), holding for its lifetime. */
    public static void halo(ServerLevel level, Vec3 centre, double radius, int colour, int lifetime) {
        flat(level, ModParticles.HALO.get(), colour, radius, lifetime, centre.add(0.0, 0.06, 0.0), 0.0F, 0.0F);
    }

    /** A turning circle of runes on the ground. */
    public static void sigil(ServerLevel level, Vec3 centre, double radius, Palette palette, int lifetime) {
        flat(level, ModParticles.SIGIL.get(), palette.main, radius, lifetime, centre.add(0.0, 0.07, 0.0), level.random.nextFloat() * Mth.TWO_PI, 0.0F);
    }

    /** A circle of runes standing upright, facing along {@code facing}, as a ward wall or a spell drawn in the air. */
    public static void sigilFacing(ServerLevel level, Vec3 centre, double radius, Palette palette, Vec3 facing, int lifetime) {
        float yaw = (float)Math.atan2(facing.z, facing.x);
        flat(level, ModParticles.SIGIL.get(), palette.main, radius, lifetime, centre, yaw, Mth.HALF_PI);
    }

    /** A circle of light on the ground, with motes rising off its rim. */
    public static void ring(ServerLevel level, Vec3 centre, double radius, Palette palette, int points) {
        halo(level, centre, radius, palette.main, 22);
        int motes = Math.max(6, points / 2);
        for (int i = 0; i < motes; i++) {
            double a = i * Mth.TWO_PI / motes;
            emit(level, ModParticles.MOTE.get(), i % 3 == 0 ? palette.bright : palette.main, 0.9F,
                centre.x + Math.cos(a) * radius, centre.y + 0.1, centre.z + Math.sin(a) * radius, 0, 0.03, 0);
        }
    }

    /** Runes floating in a circle around a point, over a faint circle of light: a ward, a brand, a sigil taking hold. */
    public static void glyphs(ServerLevel level, Vec3 centre, double radius, Palette palette, int count) {
        flat(level, ModParticles.HALO.get(), palette.main, radius * 1.15, 26, centre, 0.0F, 0.0F);
        for (int i = 0; i < count; i++) {
            double a = i * Mth.TWO_PI / count;
            emit(level, ModParticles.GLYPH.get(), i % 2 == 0 ? palette.bright : palette.main, 1.6F,
                centre.x + Math.cos(a) * radius, centre.y + 0.2, centre.z + Math.sin(a) * radius, 0, 0.015, 0);
        }
    }

    /** A wave racing outward along the ground from a centre, as from a roar or a stomp, kicking up dust behind it. */
    public static void shockwave(ServerLevel level, Vec3 centre, double radius, Palette palette) {
        Vec3 ground = centre.add(0.0, 0.08, 0.0);
        flat(level, ModParticles.RING.get(), palette.main, radius, 14, ground, 0.0F, 0.0F);
        later(2, () -> flat(level, ModParticles.RING.get(), palette.bright, radius * 0.7, 12, ground.add(0.0, 0.02, 0.0), 0.0F, 0.0F));
        int points = Math.max(16, (int)(radius * 6));
        double speed = radius / 9.0;
        RandomSource random = level.random;
        for (int i = 0; i < points; i++) {
            double a = i * Mth.TWO_PI / points;
            double cx = Math.cos(a);
            double cz = Math.sin(a);
            emit(level, ModParticles.SPARK.get(), palette.bright, 1.3F,
                centre.x + cx * 0.6, centre.y + 0.25, centre.z + cz * 0.6, cx * speed, 0.02, cz * speed);
            if (i % 6 == 0) {
                double r = random.nextDouble() * radius * 0.6;
                emit(level, ModParticles.WISP.get(), palette.deep, 1.0F + random.nextFloat() * 0.6F,
                    centre.x + cx * r, centre.y + 0.3, centre.z + cz * r, cx * 0.03, 0.02, cz * 0.03);
            }
        }
    }

    /** A line of light from one point to another: a tether, a bolt's path, a drawn soul. */
    public static void beam(ServerLevel level, Vec3 from, Vec3 to, Palette palette, double spacing) {
        Vec3 step = to.subtract(from);
        int points = Math.max(2, (int)(step.length() / spacing));
        Vec3 along = step.normalize().scale(0.12);
        for (int i = 0; i <= points; i++) {
            Vec3 at = from.add(step.scale((double)i / points));
            emit(level, ModParticles.MOTE.get(), palette.bright, 0.8F, at.x, at.y, at.z, 0, 0, 0);
            if (i % 2 == 0) {
                emit(level, ModParticles.MOTE.get(), palette.main, 1.8F, at.x, at.y, at.z, 0, 0, 0);
            }

            if (i % 4 == 0) {
                emit(level, ModParticles.SPARK.get(), palette.bright, 1.0F, at.x, at.y, at.z, along.x, along.y, along.z);
            }
        }

        flash(level, to, palette, 0.7F);
    }

    /** A sweeping arc of light in front of an entity, at chest height: a blade's cut or a tail's sweep. */
    public static void slash(ServerLevel level, LivingEntity user, double reach, Palette palette, double arcDegrees) {
        slash(level, user, reach, palette, arcDegrees, 0.0);
    }

    /**
     * A cut of light in front of an entity, tipped by {@code rollDegrees} for a diagonal stroke. A full circle spins
     * round the entity over three ticks. Sparks fly off the blade's edge.
     */
    public static void slash(ServerLevel level, LivingEntity user, double reach, Palette palette, double arcDegrees, double rollDegrees) {
        Vec3 centre = user.position().add(0.0, user.getBbHeight() * 0.55, 0.0);
        float yaw = yawOf(user);
        float roll = (float)Math.toRadians(rollDegrees);
        if (arcDegrees >= 300.0) {
            over(3, tick -> {
                float turn = yaw + tick * Mth.TWO_PI / 3.0F;
                flat(level, ModParticles.SWEEP.get(), tick % 2 == 0 ? palette.main : palette.bright, reach, 8, centre, turn, roll);
            });
            flat(level, ModParticles.RING.get(), palette.main, reach * 1.1, 12, user.position().add(0.0, 0.1, 0.0), 0.0F, 0.0F);
        } else {
            flat(level, ModParticles.SWEEP.get(), palette.main, reach, 8, centre, yaw, roll);
            later(1, () -> flat(level, ModParticles.SWEEP.get(), palette.bright, reach * 0.82, 6, centre, yaw, roll));
        }

        int sparks = (int)Math.max(6, Math.min(arcDegrees, 200.0) / 12);
        RandomSource random = level.random;
        for (int i = 0; i < sparks; i++) {
            double a = yaw + Math.toRadians((random.nextDouble() - 0.5) * Math.min(arcDegrees, 170.0));
            double dx = -Math.sin(a);
            double dz = Math.cos(a);
            double lift = (random.nextDouble() - 0.5) * 0.3 + Math.sin(roll) * (random.nextDouble() - 0.5);
            emit(level, ModParticles.SPARK.get(), palette.bright, 1.1F,
                centre.x + dx * reach, centre.y + lift, centre.z + dz * reach, dx * 0.25 + dz * 0.1, 0.03, dz * 0.25 - dx * 0.1);
        }
    }

    /** A column of light climbing from the ground over a few ticks, from a circle at its foot. */
    public static void pillar(ServerLevel level, Vec3 base, double height, Palette palette) {
        halo(level, base, 0.7, palette.main, 20);
        int steps = 4;
        over(steps, tick -> {
            double from = height * tick / steps;
            for (double y = from; y < from + height / steps; y += 0.2) {
                emit(level, ModParticles.MOTE.get(), y > height * 0.7 ? palette.bright : palette.main, 1.3F, base.x, base.y + y, base.z, 0, 0.05, 0);
            }

            RandomSource random = level.random;
            for (int i = 0; i < 3; i++) {
                double a = random.nextDouble() * Mth.TWO_PI;
                emit(level, ModParticles.SPARK.get(), palette.bright, 1.0F, base.x + Math.cos(a) * 0.35, base.y + from, base.z + Math.sin(a) * 0.35, 0, 0.35, 0);
            }
        });
    }

    /** A dome of light forming over a point from the ground up, ringed where it meets the ground. */
    public static void dome(ServerLevel level, Vec3 centre, double radius, Palette palette, int points) {
        halo(level, centre, radius, palette.main, 30);
        double golden = Math.PI * (3 - Math.sqrt(5));
        int steps = 4;
        over(steps, tick -> {
            for (int i = points * tick / steps; i < points * (tick + 1) / steps; i++) {
                double y = (double)i / points;
                double r = Math.sqrt(1 - y * y);
                double a = golden * i;
                emit(level, ModParticles.MOTE.get(), i % 5 == 0 ? palette.bright : palette.main, 1.1F,
                    centre.x + Math.cos(a) * r * radius, centre.y + y * radius, centre.z + Math.sin(a) * r * radius, 0, 0, 0);
            }
        });
    }

    /** A cone of particles flung forward from a point, as breath or a blast. Fire uses embers; anything else, light. */
    public static void cone(ServerLevel level, Vec3 origin, Vec3 direction, double length, double halfAngleDegrees, Palette palette, int count, boolean fire) {
        RandomSource random = level.random;
        Vec3 dir = direction.normalize();
        double spread = Math.tan(Math.toRadians(halfAngleDegrees));
        for (int i = 0; i < count; i++) {
            Vec3 jitter = randomDirection(random).scale(spread * random.nextDouble());
            Vec3 v = dir.add(jitter).normalize().scale(length / 12.0 * (0.6 + random.nextDouble() * 0.6));
            int colour = random.nextInt(4) == 0 ? palette.bright : (fire ? palette.deep : palette.main);
            if (fire) {
                emit(level, ModParticles.EMBER.get(), colour, 2.6F, origin.x, origin.y, origin.z, v.x, v.y, v.z);
                if (i % 4 == 0) {
                    emit(level, ModParticles.MOTE.get(), palette.main, 2.4F, origin.x, origin.y, origin.z, v.x * 0.8, v.y * 0.8, v.z * 0.8);
                }
            } else {
                emit(level, i % 3 == 0 ? ModParticles.SPARK.get() : ModParticles.MOTE.get(), colour, 1.5F, origin.x, origin.y, origin.z, v.x, v.y, v.z);
            }
        }
    }

    /** Two strands of light climbing around a point over a few ticks, as power gathering or a soul being drawn up. */
    public static void spiral(ServerLevel level, Vec3 centre, double radius, double height, Palette palette, int points) {
        int steps = 5;
        over(steps, tick -> {
            for (int i = points * tick / steps; i < points * (tick + 1) / steps; i++) {
                double t = (double)i / points;
                for (int strand = 0; strand < 2; strand++) {
                    double a = t * Mth.TWO_PI * 2.5 + strand * Math.PI;
                    emit(level, ModParticles.MOTE.get(), (i + strand) % 4 == 0 ? palette.bright : palette.main, 1.1F,
                        centre.x + Math.cos(a) * radius, centre.y + t * height, centre.z + Math.sin(a) * radius, 0, 0.02, 0);
                }
            }
        });
    }

    /** Smoke in the given colour; dark smoke reads as shadow. */
    public static void smoke(ServerLevel level, Vec3 at, int colour, int count, double spread) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            emit(level, ModParticles.WISP.get(), colour, 1.4F + random.nextFloat() * 1.2F,
                at.x + (random.nextDouble() - 0.5) * spread * 2, at.y + (random.nextDouble() - 0.5) * spread,
                at.z + (random.nextDouble() - 0.5) * spread * 2, 0, 0.01, 0);
        }
    }

    /**
     * Light gathering around an entity and rising off it: a blessing, a buff, a gathered soul. It stays below the eyes
     * and out at arm's length, so on yourself it frames the view instead of filling it.
     */
    public static void aura(ServerLevel level, Entity entity, Palette palette, int count) {
        RandomSource random = level.random;
        double w = Math.max(0.6, entity.getBbWidth() * 1.2);
        halo(level, entity.position(), w, palette.main, 18);
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Mth.TWO_PI;
            boolean spark = i % 3 == 0;
            emit(level, spark ? ModParticles.SPARK.get() : ModParticles.MOTE.get(), i % 2 == 0 ? palette.bright : palette.main,
                0.8F, entity.getX() + Math.cos(a) * w, entity.getY() + random.nextDouble() * entity.getBbHeight() * 0.7,
                entity.getZ() + Math.sin(a) * w, 0, spark ? 0.12 : 0.02 + random.nextDouble() * 0.02, 0);
        }
    }

    /** Cinders drifting up from a point. */
    public static void embers(ServerLevel level, Vec3 at, Palette palette, int count, double spread) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            emit(level, ModParticles.EMBER.get(), random.nextInt(3) == 0 ? palette.bright : palette.main, 2.0F,
                at.x + (random.nextDouble() - 0.5) * spread * 2, at.y + random.nextDouble() * spread, at.z + (random.nextDouble() - 0.5) * spread * 2,
                0, 0.025, 0);
        }
    }

    /** Slivers of light thrown off a point, as a ward or a scale breaking. */
    public static void shards(ServerLevel level, Vec3 at, Palette palette, int count) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            Vec3 v = randomDirection(random).scale(0.22);
            emit(level, ModParticles.SHARD.get(), i % 2 == 0 ? palette.bright : palette.main, 1.4F, at.x, at.y, at.z, v.x, v.y + 0.12, v.z);
        }
    }

    /** A streak of light left along a path, as from a dash or a blink. */
    public static void trail(ServerLevel level, Vec3 from, Vec3 to, Palette palette) {
        Vec3 step = to.subtract(from);
        int points = Math.max(3, (int)(step.length() * 3));
        Vec3 drift = step.normalize().scale(-0.05);
        for (int i = 0; i <= points; i++) {
            Vec3 at = from.add(step.scale((double)i / points));
            emit(level, ModParticles.MOTE.get(), i % 3 == 0 ? palette.bright : palette.main, 1.4F, at.x, at.y + 0.9, at.z, drift.x, 0.01, drift.z);
            if (i % 2 == 0) {
                emit(level, ModParticles.SPARK.get(), palette.bright, 1.0F, at.x, at.y + 0.4 + level.random.nextDouble(), at.z, drift.x * 4, 0.02, drift.z * 4);
            }
        }
    }

    /**
     * A pair of wings drawn in light on an entity's back, opened by {@code spread} (0 folded, 1 wide) and beating with
     * {@code flap} (any angle; one beat per full turn). Called every few ticks while something flies.
     */
    public static void wings(ServerLevel level, LivingEntity entity, Palette palette, double spread, double flap) {
        float bodyYaw = entity.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(bodyYaw);
        double fz = Mth.cos(bodyYaw);
        double rx = -fz;
        double rz = fx;
        Vec3 root = entity.position().add(-fx * 0.3, entity.getBbHeight() * 0.72, -fz * 0.3);
        double beat = Math.sin(flap) * 0.35;
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 1; i <= 10; i++) {
                double t = i / 10.0;
                double out = t * (0.6 + 1.5 * spread);
                double up = Math.sin(t * Math.PI * 0.8) * 0.7 * spread + beat * t;
                double back = t * t * 0.5;
                Vec3 at = root.add(rx * side * out - fx * back, up, rz * side * out - fz * back);
                emit(level, ModParticles.MOTE.get(), i >= 8 ? palette.bright : palette.main, (float)(1.4 - t * 0.5), at.x, at.y, at.z, 0, -0.01, 0);
                if (i % 2 == 0) {
                    for (int f = 1; f <= 3; f++) {
                        Vec3 feather = at.add(-fx * 0.1 * f, -0.22 * f * (1.2 - t), -fz * 0.1 * f);
                        emit(level, ModParticles.MOTE.get(), palette.main, 0.8F, feather.x, feather.y, feather.z, 0, -0.015, 0);
                    }
                }
            }
        }
    }

    /**
     * One tick of power gathering into a point: light spirals inward and upward around it, tightening as
     * {@code progress} runs from 0 to 1. The opening of a transformation.
     */
    public static void vortex(ServerLevel level, Vec3 centre, double radius, double height, Palette palette, double progress, int tick) {
        RandomSource random = level.random;
        double r = radius * (1.0 - progress * 0.7);
        for (int i = 0; i < 10; i++) {
            double a = tick * 0.45 + i * Mth.TWO_PI / 10;
            double y = random.nextDouble() * height;
            Vec3 at = centre.add(Math.cos(a) * r, y, Math.sin(a) * r);
            Vec3 inward = centre.add(0.0, height * 0.5, 0.0).subtract(at).normalize().scale(0.08 + progress * 0.12);
            Vec3 swirl = new Vec3(-Math.sin(a), 0.0, Math.cos(a)).scale(0.18);
            emit(level, i % 2 == 0 ? ModParticles.SPARK.get() : ModParticles.MOTE.get(), i % 3 == 0 ? palette.bright : palette.main,
                1.2F, at.x, at.y, at.z, inward.x + swirl.x, inward.y + 0.04, inward.z + swirl.z);
        }
    }

    private static Vec3 randomDirection(RandomSource random) {
        double a = random.nextDouble() * Mth.TWO_PI;
        double y = random.nextDouble() * 2 - 1;
        double r = Math.sqrt(1 - y * y);
        return new Vec3(Math.cos(a) * r, y, Math.sin(a) * r);
    }
}
