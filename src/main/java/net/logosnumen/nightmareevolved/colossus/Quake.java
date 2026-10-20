package net.logosnumen.nightmareevolved.colossus;

import java.util.List;
import com.mojang.math.Transformation;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * What a colossus's blows do to the ground and everything on it: slams that ripple outward, sweeping arcs, strikes
 * along a line, and spikes of stone, bone or chain that burst up out of the earth and sink back. Colossi never hit each
 * other, and nothing here breaks blocks.
 */
public final class Quake {
    /** Marks the spikes, which are display entities, so any left behind by a server stopping mid-quake are cleared. */
    private static final String FX_TAG = "nightmare_evolved_fx";

    private Quake() {
    }

    /** Living things a colossus's blow can catch: not itself, not another colossus, not spectators or creative players. */
    public static List<LivingEntity> victims(ServerLevel level, LivingEntity source, AABB area) {
        return level.getEntitiesOfClass(
            LivingEntity.class, area, e -> e != source && e.isAlive() && !(e instanceof ColossusEntity) && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e)
        );
    }

    /** Hurts a victim and throws it, as much as its knockback resistance lets. */
    public static void hit(ServerLevel level, LivingEntity source, LivingEntity victim, float damage, Vec3 push) {
        if (victim.hurt(level.damageSources().mobAttack(source), damage)) {
            double give = 1.0 - Mth.clamp(victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            victim.push(push.x * give, push.y * give, push.z * give);
            victim.hurtMarked = true;
        }
    }

    /** The top of the ground near a point: the first standing place found looking down from a little above it. */
    public static double groundAt(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y) + 3, Mth.floor(z));
        for (int i = 0; i < 14; i++) {
            BlockPos under = pos.below();
            boolean solidBelow = !level.getBlockState(under).getCollisionShape(level, under).isEmpty();
            if (solidBelow && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return pos.getY();
            }

            pos.move(Direction.DOWN);
        }

        return y;
    }

    private static BlockState groundBlock(ServerLevel level, Vec3 at) {
        return level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
    }

    /** Broken ground thrown up in a ring: dust of whatever the ground is made of. */
    public static void debris(ServerLevel level, Vec3 at, double radius, int count) {
        BlockState ground = groundBlock(level, at);
        if (ground.isAir()) {
            return;
        }

        RandomSource random = level.random;
        BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Mth.TWO_PI;
            double r = radius * Math.sqrt(random.nextDouble());
            level.sendParticles(dust, at.x + Math.cos(a) * r, at.y + 0.2, at.z + Math.sin(a) * r, 2, 0.2, 0.3, 0.2, 0.25);
        }
    }

    /** A blow into the ground: everything near is hurt (less at the edge) and thrown outward and up. */
    public static void slam(ServerLevel level, LivingEntity source, Vec3 centre, double radius, float damage, double lift, Vfx.Palette palette) {
        Vec3 at = new Vec3(centre.x, groundAt(level, centre.x, centre.y, centre.z), centre.z);
        for (LivingEntity victim : victims(level, source, new AABB(at, at).inflate(radius, 3.0, radius))) {
            Vec3 away = new Vec3(victim.getX() - at.x, 0.0, victim.getZ() - at.z);
            double d = away.length();
            if (d > radius + victim.getBbWidth() * 0.5) {
                continue;
            }

            float falloff = (float)(1.0 - 0.5 * Math.min(1.0, d / radius));
            Vec3 dir = d < 0.01 ? new Vec3(1.0, 0.0, 0.0) : away.scale(1.0 / d);
            hit(level, source, victim, damage * falloff, new Vec3(dir.x * 1.2 * falloff, lift * falloff, dir.z * 1.2 * falloff));
        }

        Vfx.shockwave(level, at, radius, palette);
        debris(level, at, radius * 0.8, (int)(radius * 8));
        Vfx.smoke(level, at.add(0.0, 0.5, 0.0), palette.deep, 10, radius * 0.5);
        level.playSound(null, BlockPos.containing(at), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.5F);
        level.playSound(null, BlockPos.containing(at), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2F, 0.4F);
    }

    /**
     * A sweep across the front: everything within reach and inside the arc (degrees) is hurt and thrown sideways and
     * back, and a trail of sparks marks the blade's path at the given height.
     */
    public static void arc(ServerLevel level, LivingEntity source, double reach, double arcDegrees, float damage, double push, double lift, double height,
        Vfx.Palette palette) {
        Vec3 origin = source.position();
        float yaw = source.getYRot();
        Vec3 facing = Vec3.directionFromRotation(0.0F, yaw);
        for (LivingEntity victim : victims(level, source, source.getBoundingBox().inflate(reach, 2.0, reach))) {
            Vec3 to = new Vec3(victim.getX() - origin.x, 0.0, victim.getZ() - origin.z);
            double d = to.length();
            if (d > reach + victim.getBbWidth() * 0.5 || d < 0.01) {
                continue;
            }

            double angle = Math.toDegrees(Math.acos(Mth.clamp(to.scale(1.0 / d).dot(facing), -1.0, 1.0)));
            if (angle <= arcDegrees * 0.5) {
                Vec3 dir = to.scale(1.0 / d);
                hit(level, source, victim, damage, new Vec3(dir.x * push, lift, dir.z * push));
            }
        }

        for (int i = 0; i <= 24; i++) {
            float a = (float)(yaw - arcDegrees * 0.5 + arcDegrees * i / 24.0);
            Vec3 dir = Vec3.directionFromRotation(0.0F, a);
            Vfx.spark(level, origin.add(dir.scale(reach * 0.85)).add(0.0, height, 0.0), i % 3 == 0 ? palette.bright : palette.main, dir.scale(0.3));
        }

        debris(level, origin.add(facing.scale(reach * 0.6)), reach * 0.5, 16);
        level.playSound(null, source.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.4F);
    }

    /** A blow along a line on the ground, from a point out along a direction, a given width across. */
    public static void line(ServerLevel level, LivingEntity source, Vec3 from, Vec3 direction, double length, double width, float damage, double lift,
        Vfx.Palette palette) {
        Vec3 dir = new Vec3(direction.x, 0.0, direction.z).normalize();
        Vec3 to = from.add(dir.scale(length));
        for (LivingEntity victim : victims(level, source, new AABB(from, to).inflate(width, 3.0, width))) {
            Vec3 rel = new Vec3(victim.getX() - from.x, 0.0, victim.getZ() - from.z);
            double along = rel.dot(dir);
            double across = rel.subtract(dir.scale(along)).length();
            if (along >= -1.0 && along <= length && across <= width * 0.5 + victim.getBbWidth() * 0.5) {
                hit(level, source, victim, damage, new Vec3(dir.x * 0.6, lift, dir.z * 0.6));
            }
        }

        for (double t = 0.0; t <= length; t += 1.0) {
            Vec3 p = from.add(dir.scale(t));
            Vec3 at = new Vec3(p.x, groundAt(level, p.x, p.y, p.z), p.z);
            debris(level, at, width * 0.4, 3);
            Vfx.spark(level, at.add(0.0, 0.3, 0.0), palette.bright, new Vec3(0.0, 0.35, 0.0));
        }
    }

    /** Spikes burst up one after another along a line from a point, each hurting and tossing whatever stands on it. */
    public static void spikeLine(ServerLevel level, LivingEntity source, Vec3 from, Vec3 direction, int count, double spacing, BlockState state, float damage,
        Vfx.Palette palette) {
        Vec3 step = new Vec3(direction.x, 0.0, direction.z).normalize().scale(spacing);
        Vfx.over(count, i -> {
            Vec3 p = from.add(step.scale(i + 1));
            erupt(level, source, new Vec3(p.x, groundAt(level, p.x, p.y, p.z), p.z), state, damage, palette);
        });
    }

    /** A ring of spikes bursting up around a point all at once. */
    public static void spikeRing(ServerLevel level, LivingEntity source, Vec3 centre, double radius, int count, BlockState state, float damage, Vfx.Palette palette) {
        for (int i = 0; i < count; i++) {
            double a = i * Mth.TWO_PI / count + level.random.nextDouble() * 0.3;
            Vec3 p = centre.add(Math.cos(a) * radius, 0.0, Math.sin(a) * radius);
            erupt(level, source, new Vec3(p.x, groundAt(level, p.x, p.y, p.z), p.z), state, damage, palette);
        }
    }

    private static void erupt(ServerLevel level, LivingEntity source, Vec3 at, BlockState state, float damage, Vfx.Palette palette) {
        RandomSource random = level.random;
        spike(level, at, state, 0.8F + random.nextFloat() * 0.5F, 2.2F + random.nextFloat() * 1.6F, 14);
        for (LivingEntity victim : victims(level, source, new AABB(at, at).inflate(1.3, 2.5, 1.3))) {
            hit(level, source, victim, damage, new Vec3(0.0, 0.9, 0.0));
        }

        debris(level, at, 1.0, 4);
        Vfx.spark(level, at.add(0.0, 0.5, 0.0), palette.bright, new Vec3(0.0, 0.5, 0.0));
        BlockPos pos = BlockPos.containing(at);
        level.playSound(null, pos, state.getSoundType(level, pos, null).getBreakSound(), SoundSource.HOSTILE, 1.5F, 0.6F);
    }

    /**
     * One spike: a block drawn stretched tall, tilted a little at random, that rises out of the ground over a few
     * ticks, holds, and sinks back. It is a display entity, so it costs no blocks and leaves the ground untouched.
     */
    private static void spike(ServerLevel level, Vec3 at, BlockState state, float width, float height, int hold) {
        Display.BlockDisplay display = EntityType.BLOCK_DISPLAY.create(level);
        if (display == null) {
            return;
        }

        RandomSource random = level.random;
        Quaternionf tilt = new Quaternionf().rotateX((random.nextFloat() - 0.5F) * 0.5F).rotateZ((random.nextFloat() - 0.5F) * 0.5F);
        display.moveTo(at.x, at.y, at.z, random.nextFloat() * 360.0F, 0.0F);
        display.addTag(FX_TAG);
        shape(display, state, tilt, width, height, -height - 0.2F, 0);
        level.addFreshEntity(display);
        Vfx.later(1, () -> shape(display, state, tilt, width, height, 0.0F, 3));
        Vfx.later(4 + hold, () -> shape(display, state, tilt, width, height, -height - 0.2F, 8));
        Vfx.later(13 + hold, display::discard);
    }

    private static void shape(Display.BlockDisplay display, BlockState state, Quaternionf tilt, float width, float height, float drop, int duration) {
        if (display.isRemoved()) {
            return;
        }

        CompoundTag tag = display.saveWithoutId(new CompoundTag());
        tag.put("block_state", NbtUtils.writeBlockState(state));
        Transformation transformation = new Transformation(
            new Vector3f(-width * 0.5F, drop, -width * 0.5F), new Quaternionf(tilt), new Vector3f(width, height, width), new Quaternionf()
        );
        Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, transformation).result().ifPresent(t -> tag.put("transformation", t));
        tag.putInt("interpolation_duration", duration);
        tag.putInt("start_interpolation", 0);
        display.load(tag);
    }

    /** Spikes saved by a server that stopped mid-quake are dropped when their chunk loads again. */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() && event.getEntity() instanceof Display && event.getEntity().getTags().contains(FX_TAG)) {
            event.setCanceled(true);
        }
    }
}
