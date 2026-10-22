package net.logosnumen.nightmareevolved.entity.creature;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Move;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * A Nightmare Creature of the Dream Realm, of one {@link Kind}. It walks, climbs, flies, swims or stays rooted as its
 * kind does, and fights with its kind's moves: it picks one that suits the range, holds still through its wind-up while
 * clients play it, and lands its blows on the ticks the move names. Kinds with more to them than that extend it.
 */
public class DreamCreature extends Monster {
    /** Persistent data naming the creature for the Memories it may leave. */
    public static final String LORE = "nightmare_evolved_lore";
    private static final EntityDataAccessor<Integer> DATA_ACTION = SynchedEntityData.defineId(DreamCreature.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(DreamCreature.class, EntityDataSerializers.INT);
    protected final Kind kind;
    private final int[] cooldowns;
    @Nullable
    private Move acting;
    private int actingIndex = -1;
    private int actingTicks;
    private int actionSerial;
    private int restTicks;
    private int clientActionStart;
    private final Set<UUID> struck = new HashSet<>();
    /** How long a swimmer has lain out of the water. */
    private int beached;
    /** The lair this creature keeps, if it is one of the named; empty for the rest. */
    private String lair = "";

    /** Kinds with bodies of their own extend this in the same package. */
    DreamCreature(EntityType<? extends DreamCreature> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.cooldowns = new int[kind.moves.size()];
        this.xpReward = 5 + kind.rank.ordinal() * 6 + kind.creatureClass.ordinal() * 4;
        switch (kind.motion) {
            case FLY, HOVER -> {
                this.moveControl = new FlyingMoveControl(this, 20, kind.motion == Kind.Motion.HOVER);
                this.setNoGravity(true);
            }
            case SWIM -> {
                this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.06F, 0.1F, true);
                this.lookControl = new SmoothSwimmingLookControl(this, 10);
                this.setPathfindingMalus(PathType.WATER, 0.0F);
            }
            default -> {
            }
        }

        this.navigation = this.navigationFor(level);
        if (!level.isClientSide) {
            this.addGoals();
        }
    }

    /** What a kind's creatures are made of, before their rank adds anything. */
    public static AttributeSupplier.Builder attributes(Kind kind) {
        AttributeSupplier.Builder builder = Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, kind.health)
            .add(Attributes.ATTACK_DAMAGE, kind.damage)
            .add(Attributes.ARMOR, kind.armor)
            .add(Attributes.MOVEMENT_SPEED, kind.speed)
            .add(Attributes.FOLLOW_RANGE, kind.follow)
            .add(Attributes.KNOCKBACK_RESISTANCE, kind.motion == Kind.Motion.ROOT ? 1.0 : Math.min(1.0, kind.creatureClass.ordinal() * 0.15 + kind.height * 0.04))
            .add(Attributes.STEP_HEIGHT, Math.max(0.6, Math.min(2.5, kind.height * 0.35)));
        if (kind.motion == Kind.Motion.FLY || kind.motion == Kind.Motion.HOVER) {
            builder.add(Attributes.FLYING_SPEED, kind.speed * 1.6);
        }

        return builder;
    }

    public Kind kind() {
        return this.kind;
    }

    /** Which of its kind's looks this one wears. */
    public int look() {
        return this.entityData.get(DATA_LOOK);
    }

    public boolean keepsLair() {
        return !this.lair.isEmpty();
    }

    public void keepLair(String lair) {
        this.lair = lair;
        this.setPersistenceRequired();
    }

    // ---- Moving -----------------------------------------------------------------------------------------------------

    private PathNavigation navigationFor(Level level) {
        return switch (this.kind.motion) {
            case CLIMB -> new WallClimberNavigation(this, level);
            case FLY, HOVER -> {
                FlyingPathNavigation flying = new FlyingPathNavigation(this, level);
                flying.setCanOpenDoors(false);
                flying.setCanFloat(true);
                flying.setCanPassDoors(true);
                yield flying;
            }
            case SWIM -> new WaterBoundPathNavigation(this, level);
            default -> this.createNavigation(level);
        };
    }

    @Override
    protected void registerGoals() {
        // Goals depend on the kind, which isn't known yet while the body is first built; see addGoals.
    }

    protected void addGoals() {
        Kind.Motion motion = this.kind.motion;
        if (motion == Kind.Motion.WALK || motion == Kind.Motion.CLIMB) {
            this.goalSelector.addGoal(0, new FloatGoal(this));
        }

        this.goalSelector.addGoal(1, new MoveGoal());
        if (motion != Kind.Motion.ROOT) {
            this.goalSelector.addGoal(2, new ChaseGoal());
        }

        switch (motion) {
            case WALK, CLIMB -> this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
            case FLY, HOVER -> this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
            case SWIM -> this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0, 20));
            default -> {
            }
        }

        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, motion != Kind.Motion.ROOT));
    }

    @Override
    public boolean isImmobile() {
        return this.kind.motion == Kind.Motion.ROOT || super.isImmobile();
    }

    @Override
    public boolean isPushable() {
        return this.kind.motion != Kind.Motion.ROOT && super.isPushable();
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return this.kind.motion != Kind.Motion.SWIM && super.isPushedByFluid(type);
    }

    @Override
    public boolean onClimbable() {
        return this.kind.motion == Kind.Motion.CLIMB && this.horizontalCollision || super.onClimbable();
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        if (this.kind.motion == Kind.Motion.FLY || this.kind.motion == Kind.Motion.HOVER || this.kind.has(Kind.Trait.LIGHT_FALL)) {
            return false;
        }

        return super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (this.kind.motion != Kind.Motion.FLY && this.kind.motion != Kind.Motion.HOVER) {
            super.checkFallDamage(y, onGround, state, pos);
        }
    }

    /** Swimmers move freely in the water, as fish do. */
    @Override
    public void travel(Vec3 input) {
        if (this.kind.motion == Kind.Motion.SWIM && this.isInWater() && this.isControlledByLocalInstance()) {
            this.moveRelative(this.getSpeed(), input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            return;
        }

        super.travel(input);
    }

    // ---- Spawning and saving ----------------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTION, 0);
        builder.define(DATA_LOOK, 0);
    }

    /**
     * Readies a creature being spawned (from the spawn event): picks its look, makes it a Nightmare Creature of its
     * kind's rank and class, names it for the Memories it may leave, and keeps those raised on purpose.
     */
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof DreamCreature creature) || event.isSpawnCancelled()) {
            return;
        }

        Kind kind = creature.kind;
        creature.entityData.set(DATA_LOOK, creature.random.nextInt(Math.max(1, kind.looks)));
        if (!Creatures.ofNightmare(creature)) {
            Creatures.profile(creature).set(kind.rank, kind.creatureClass, false, -1L);
        }

        if (kind.lore) {
            creature.getPersistentData().putString(LORE, kind.id);
        }

        MobSpawnType reason = event.getSpawnType();
        if (reason == MobSpawnType.SPAWN_EGG || reason == MobSpawnType.COMMAND || reason == MobSpawnType.SPAWNER) {
            creature.setPersistenceRequired();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Look", this.look());
        if (!this.lair.isEmpty()) {
            tag.putString("Lair", this.lair);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_LOOK, tag.getInt("Look"));
        this.lair = tag.getString("Lair");
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return this.lair.isEmpty() && super.removeWhenFarAway(distance);
    }

    // ---- Sounds -----------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.kind.ambient.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.kind.hurt.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.kind.death.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.kind.step.get(), Math.min(1.0F, 0.15F * this.kind.height), 1.0F);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * Mth.clamp(1.6F / Math.max(0.5F, this.kind.height), 0.5F, 1.4F);
    }

    // ---- Being hurt -------------------------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.kind.has(Kind.Trait.FIRE_WEAK) && source.is(DamageTypeTags.IS_FIRE)) {
            amount *= 2.0F;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && this.kind.has(Kind.Trait.PACK) && source.getEntity() instanceof LivingEntity attacker && this.level() instanceof ServerLevel) {
            for (DreamCreature kin : this.level().getEntitiesOfClass(DreamCreature.class, this.getBoundingBox().inflate(this.kind.follow * 0.6),
                c -> c != this && this.kind.answers(c.kind) && c.getTarget() == null)) {
                kin.setTarget(attacker);
            }
        }

        return hurt;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return (this.kind.motion == Kind.Motion.SWIM && source.is(DamageTypes.DROWN)) || super.isInvulnerableTo(source);
    }

    // ---- Moves ------------------------------------------------------------------------------------------------------

    /** The move running on the client, as clients were last told, or null. */
    @Nullable
    public Move clientAction() {
        int index = (this.entityData.get(DATA_ACTION) & 0xFF) - 1;
        return index >= 0 && index < this.kind.moves.size() ? this.kind.moves.get(index) : null;
    }

    /** How far through its move the creature is on the client, 0 to 1, or -1 when it is not in one. */
    public float actionProgress(float partial) {
        Move move = this.clientAction();
        if (move == null) {
            return -1.0F;
        }

        return Mth.clamp((this.tickCount + partial - this.clientActionStart) / move.duration(), 0.0F, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ACTION.equals(key)) {
            this.clientActionStart = this.tickCount;
        }
    }

    public boolean acting() {
        return this.acting != null;
    }

    @Nullable
    protected Move currentMove() {
        return this.acting;
    }

    /** How often a move is chosen now (kinds can change their minds as a fight goes). */
    protected int weight(Move move) {
        return move.weight();
    }

    private int chooseMove(LivingEntity target) {
        double distance = this.distanceToTarget(target);
        List<Move> moves = this.kind.moves;
        int total = 0;
        for (int i = 0; i < moves.size(); i++) {
            if (this.usable(moves.get(i), i, distance)) {
                total += this.weight(moves.get(i));
            }
        }

        if (total <= 0) {
            return -1;
        }

        int roll = this.random.nextInt(total);
        for (int i = 0; i < moves.size(); i++) {
            if (this.usable(moves.get(i), i, distance)) {
                roll -= this.weight(moves.get(i));
                if (roll < 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    /** Distance from the edge of this body to the edge of the target's, in blocks. */
    protected double distanceToTarget(LivingEntity target) {
        double centre = Math.sqrt(Mth.square(target.getX() - this.getX()) + Mth.square(target.getZ() - this.getZ()));
        return Math.max(0.0, centre - this.getBbWidth() * 0.5 - target.getBbWidth() * 0.5);
    }

    private boolean usable(Move move, int index, double distance) {
        return this.weight(move) > 0 && this.cooldowns[index] <= 0 && distance >= move.minRange() && distance <= move.maxRange();
    }

    /** Starts a move by its index: tells clients, stops walking, and sounds its warning. */
    protected void beginMove(int index) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        this.acting = this.kind.moves.get(index);
        this.actingIndex = index;
        this.actingTicks = 0;
        this.struck.clear();
        this.actionSerial = this.actionSerial + 1 & 0xFFFFFF;
        this.entityData.set(DATA_ACTION, this.actionSerial << 8 | index + 1);
        if (this.acting.style() != Move.Style.CHARGE) {
            this.getNavigation().stop();
        }

        this.windup(level, this.acting, this.getTarget());
    }

    /** Starts the named move at once, whatever its range or cooldown, if nothing else is running. */
    public void forceMove(String id) {
        if (this.acting != null) {
            return;
        }

        for (int i = 0; i < this.kind.moves.size(); i++) {
            if (this.kind.moves.get(i).id().equals(id)) {
                this.beginMove(i);
                return;
            }
        }
    }

    private void endMove() {
        if (this.acting != null && this.actingIndex >= 0) {
            this.cooldowns[this.actingIndex] = this.acting.cooldown();
        }

        this.acting = null;
        this.actingIndex = -1;
        this.restTicks = 6 + this.random.nextInt(8);
        this.entityData.set(DATA_ACTION, this.actionSerial << 8);
    }

    /** As a move begins. */
    protected void windup(ServerLevel level, Move move, @Nullable LivingEntity target) {
        this.playSound(this.kind.ambient.get(), Math.min(2.0F, 0.6F + 0.2F * this.kind.height), this.getVoicePitch() * 0.85F);
    }

    /** Every tick of a move. */
    protected void moveTick(ServerLevel level, Move move, int tick, @Nullable LivingEntity target) {
        switch (move.style()) {
            case POUNCE -> {
                int leap = Math.max(1, move.firstImpact() - 7);
                if (tick == leap && target != null) {
                    Vec3 to = target.position().subtract(this.position());
                    double flat = Math.min(move.maxRange() + 1.0, Math.sqrt(to.x * to.x + to.z * to.z));
                    Vec3 dir = new Vec3(to.x, 0.0, to.z).normalize();
                    this.setDeltaMovement(dir.x * flat * 0.22, this.isNoGravity() ? to.y * 0.18 : 0.42 + flat * 0.03, dir.z * flat * 0.22);
                    this.hasImpulse = true;
                }
            }
            case CHARGE -> {
                if (tick >= 4 && tick <= move.firstImpact()) {
                    Vec3 dir = Vec3.directionFromRotation(0.0F, this.getYRot());
                    double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.6;
                    this.setDeltaMovement(dir.x * speed, this.getDeltaMovement().y, dir.z * speed);
                    for (LivingEntity hit : this.victims(this.getBoundingBox().inflate(0.4))) {
                        if (this.struck.add(hit.getUUID())) {
                            this.strike(hit, this.blow(move.power()), 1.4F);
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    /** One of a move's blows landing. Kinds with blows of their own override this and fall back on it. */
    protected void impact(ServerLevel level, Move move, int which, @Nullable LivingEntity target) {
        float damage = this.blow(move.power());
        switch (move.style()) {
            case STRIKE -> {
                for (LivingEntity hit : this.inCone(move.reach(), 70.0)) {
                    this.strike(hit, damage, 0.6F);
                }

                Vfx.slash(level, this, move.reach() + this.getBbWidth() * 0.5, this.kind.palette, 120.0);
            }
            case BITE -> {
                if (target != null && this.distanceToTarget(target) <= move.reach() + 0.5) {
                    this.strike(target, damage, 0.4F);
                }
            }
            case SWEEP, POUNCE -> {
                for (LivingEntity hit : this.around(move.reach())) {
                    this.strike(hit, damage, 0.9F);
                }

                Vfx.ring(level, this.position().add(0.0, 0.3, 0.0), move.reach() + this.getBbWidth() * 0.5, this.kind.palette, 18);
            }
            case SLAM -> {
                for (LivingEntity hit : this.around(move.reach())) {
                    this.strike(hit, damage, 1.2F);
                    hit.setDeltaMovement(hit.getDeltaMovement().add(0.0, 0.45, 0.0));
                }

                Vfx.shockwave(level, this.position(), move.reach() + this.getBbWidth() * 0.5, this.kind.palette);
            }
            case GRAB -> {
                if (target != null && this.distanceToTarget(target) <= move.reach() + 0.5) {
                    Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.8);
                    target.setDeltaMovement(pull.x, 0.25, pull.z);
                    target.hurtMarked = true;
                    this.strike(target, damage, 0.0F);
                }
            }
            case BREATH -> {
                for (LivingEntity hit : this.inCone(move.reach(), 30.0)) {
                    this.strike(hit, damage, 0.3F);
                }

                Vec3 eye = this.getEyePosition();
                Vfx.cone(level, eye, this.getLookAngle(), move.reach() + this.getBbWidth() * 0.5, 30.0, this.kind.palette, 40, false);
            }
            case SHOT -> {
                if (target != null) {
                    CreatureBolt.shoot(this, target, damage, this.kind.palette, this.shotSpeed(move));
                }
            }
            case CALL -> this.call(level);
            default -> this.special(level, move, which, target);
        }
    }

    /** A move of the creature's own kind. */
    protected void special(ServerLevel level, Move move, int which, @Nullable LivingEntity target) {
    }

    protected double shotSpeed(Move move) {
        return 1.4;
    }

    /** Calls kin to the fight: those of its kind (and any it commands) nearby take up its target. */
    protected void call(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        for (DreamCreature kin : level.getEntitiesOfClass(DreamCreature.class, this.getBoundingBox().inflate(this.kind.follow), c -> c != this && this.kind.answers(c.kind))) {
            kin.setTarget(target);
        }
    }

    /** The attack damage times a factor. */
    protected float blow(float factor) {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor;
    }

    /** Lands a blow on something, with a shove of the given strength away from this body. */
    protected void strike(LivingEntity hit, float damage, float shove) {
        if (hit.hurt(this.damageSources().mobAttack(this), damage)) {
            if (this.kind.has(Kind.Trait.DRAIN)) {
                this.heal(damage * 0.5F);
            }

            if (shove > 0.0F) {
                Vec3 away = hit.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize().scale(shove * 0.5);
                hit.push(away.x, 0.1 * shove, away.z);
                hit.hurtMarked = true;
            }

            this.afflict(hit);
        }
    }

    /** What a landed blow leaves behind beyond the wound (venom and the like). */
    protected void afflict(LivingEntity hit) {
        for (Supplier<MobEffectInstance> affliction : this.kind.afflictions) {
            hit.addEffect(affliction.get(), this);
        }
    }

    /** A bolt of this creature's has struck something. */
    void afflictFromShot(LivingEntity hit) {
        this.afflict(hit);
    }

    /** Whether a blow of this creature's may fall on something. */
    protected boolean canStrike(LivingEntity other) {
        if (other == this || !other.isAlive() || other instanceof DreamCreature kin && kin.kind == this.kind) {
            return false;
        }

        if (other instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }

        return other == this.getTarget() || other instanceof Mob mob && mob.getTarget() == this;
    }

    protected List<LivingEntity> victims(AABB box) {
        return this.level().getEntitiesOfClass(LivingEntity.class, box, this::canStrike);
    }

    /** What stands within reach of the body all around. */
    protected List<LivingEntity> around(double reach) {
        double r = reach + this.getBbWidth() * 0.5;
        return this.victims(this.getBoundingBox().inflate(reach, Math.max(1.0, reach * 0.5), reach)).stream()
            .filter(e -> e.distanceToSqr(this) <= Mth.square(r + e.getBbWidth() * 0.5)).toList();
    }

    /** What stands within reach in front, inside a cone of the given half-angle. */
    protected List<LivingEntity> inCone(double reach, double halfAngle) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
        double r = reach + this.getBbWidth() * 0.5;
        double cos = Math.cos(Math.toRadians(halfAngle));
        return this.victims(this.getBoundingBox().inflate(reach, Math.max(1.0, reach * 0.5), reach)).stream().filter(e -> {
            Vec3 to = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            double d = to.length();
            return d <= r + e.getBbWidth() * 0.5 && (d < 0.5 || to.normalize().dot(facing) >= cos);
        }).toList();
    }

    /** Turns toward a point, at most so many degrees a tick. */
    protected void turnToward(double x, double z, float maxStep) {
        float yaw = (float)(Mth.atan2(z - this.getZ(), x - this.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, maxStep));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel)this.level();
        for (int i = 0; i < this.cooldowns.length; i++) {
            if (this.cooldowns[i] > 0) {
                this.cooldowns[i]--;
            }
        }

        if (this.restTicks > 0) {
            this.restTicks--;
        }

        // A swimmer left without water (the Dark Sea draining at dawn) sinks away into what is left of it.
        if (this.kind.motion == Kind.Motion.SWIM && !this.isInWater() && !this.keepsLair()) {
            if (++this.beached > 60) {
                Vfx.smoke(level, this.position().add(0.0, this.getBbHeight() * 0.5, 0.0), this.kind.palette.deep, 20, this.getBbWidth() * 0.5);
                this.discard();
                return;
            }
        } else {
            this.beached = 0;
        }

        if (this.acting != null) {
            Move move = this.acting;
            this.actingTicks++;
            LivingEntity target = this.getTarget();
            if (move.style() != Move.Style.CHARGE && move.style() != Move.Style.POUNCE) {
                this.getNavigation().stop();
            }

            if (target != null && this.actingTicks < move.firstImpact() - 3 && move.style() != Move.Style.CHARGE) {
                this.turnToward(target.getX(), target.getZ(), 18.0F);
            }

            this.moveTick(level, move, this.actingTicks, target);
            int[] impacts = move.impacts();
            for (int i = 0; i < impacts.length; i++) {
                if (impacts[i] == this.actingTicks) {
                    this.impact(level, move, i, target);
                }
            }

            if (this.actingTicks >= move.duration() && this.acting == move) {
                this.endMove();
            }
        }
    }

    /** Picks a move that suits the range whenever it is free to, and holds the body still while one runs. */
    private final class MoveGoal extends Goal {
        MoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (DreamCreature.this.acting != null) {
                return true;
            }

            LivingEntity target = DreamCreature.this.getTarget();
            if (target == null || !target.isAlive() || DreamCreature.this.restTicks > 0 || !DreamCreature.this.hasLineOfSight(target)) {
                return false;
            }

            int index = DreamCreature.this.chooseMove(target);
            if (index < 0) {
                return false;
            }

            DreamCreature.this.beginMove(index);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return DreamCreature.this.acting != null;
        }
    }

    /** Closes on its target: by path on foot or in water, straight through the air for fliers. */
    private final class ChaseGoal extends Goal {
        private int repath;

        ChaseGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        private double closest() {
            double near = Double.MAX_VALUE;
            for (Move move : DreamCreature.this.kind.moves) {
                if (move.style() != Move.Style.CALL) {
                    near = Math.min(near, move.maxRange());
                }
            }

            return near == Double.MAX_VALUE ? 1.5 : Math.max(0.8, near * 0.8);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = DreamCreature.this.getTarget();
            return DreamCreature.this.acting == null && target != null && target.isAlive()
                && DreamCreature.this.distanceToTarget(target) > this.closest();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = DreamCreature.this.getTarget();
            if (target == null) {
                return;
            }

            DreamCreature.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            Kind.Motion motion = DreamCreature.this.kind.motion;
            if (motion == Kind.Motion.FLY || motion == Kind.Motion.HOVER) {
                double lift = motion == Kind.Motion.FLY ? 2.5 : 0.8;
                DreamCreature.this.getMoveControl().setWantedPosition(target.getX(), target.getY() + lift, target.getZ(), 1.2);
                return;
            }

            if (--this.repath <= 0) {
                this.repath = 8 + DreamCreature.this.random.nextInt(6);
                if (!DreamCreature.this.getNavigation().moveTo(target, 1.15)) {
                    DreamCreature.this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.15);
                }
            }
        }

        @Override
        public void stop() {
            DreamCreature.this.getNavigation().stop();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Every blow goes through a move; touching is not an attack.
        return false;
    }
}
