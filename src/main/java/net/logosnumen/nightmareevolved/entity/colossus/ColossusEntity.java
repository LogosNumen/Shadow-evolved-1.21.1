package net.logosnumen.nightmareevolved.entity.colossus;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.colossus.ColossusAttack;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.colossus.ColossusVariant;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * A colossus: a creature many times a player's height, slow to turn and terrible when it strikes. Each kind has a body
 * plan and a set of attacks; each one born varies within its kind by a seed (colouring, size, parts), shared with
 * clients so they draw it the same. It reads its attacks out loud with a long wind-up, follows its target until just
 * before the blow lands, and then commits, so a player who watches it can get out of the way. Below half health it
 * rages: faster, quicker to strike again, and brighter. It ignores walls, falls and drowning, walks up hills and
 * through leaves, and takes a long time to fall when it dies. Like any great Nightmare Creature it carries a name bar
 * with its rank and class while it is in view.
 */
public abstract class ColossusEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_SEED = SynchedEntityData.defineId(ColossusEntity.class, EntityDataSerializers.INT);
    /** The running attack as (serial << 8 | index + 1), 0 for none; the serial makes a repeat of the same attack a change. */
    private static final EntityDataAccessor<Integer> DATA_ACTION = SynchedEntityData.defineId(ColossusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(ColossusEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int DEATH_TICKS = 50;
    private static final ResourceLocation RAGE_SPEED = ResourceLocation.fromNamespaceAndPath(NightmareEvolved.MOD_ID, "colossus_rage");

    private final ColossusKind kind;
    private final int[] cooldowns;
    @Nullable
    private ColossusAttack acting;
    private int actingIndex = -1;
    private int actingTicks;
    private int actionSerial;
    private int restTicks = 40;
    private int clientActionStart;
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10);

    protected ColossusEntity(EntityType<? extends ColossusEntity> type, Level level, ColossusKind kind) {
        super(type, level);
        this.kind = kind;
        this.cooldowns = new int[this.attacks().size()];
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder colossusAttributes(double health, double damage, double armor, double speed) {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, health)
            .add(Attributes.ATTACK_DAMAGE, damage)
            .add(Attributes.ARMOR, armor)
            .add(Attributes.ARMOR_TOUGHNESS, 6.0)
            .add(Attributes.MOVEMENT_SPEED, speed)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 56.0)
            .add(Attributes.STEP_HEIGHT, 2.6)
            .add(Attributes.JUMP_STRENGTH, 0.75);
    }

    /** This kind's moves, the same list every time (index order is what clients are told). */
    public abstract List<ColossusAttack> attacks();

    /** A blow of an attack lands; {@code which} is its place in the attack's impact ticks. */
    protected abstract void impact(ServerLevel level, ColossusAttack attack, int which, @Nullable LivingEntity target);

    /** The effect colours for this colossus's colouring. */
    public abstract Vfx.Palette palette();

    /** An attack begins: its warning. */
    protected void windup(ServerLevel level, ColossusAttack attack, @Nullable LivingEntity target) {
    }

    /** Every tick of a running attack, for attacks that move the body (charges, dives). */
    protected void actionTick(ServerLevel level, ColossusAttack attack, int tick, @Nullable LivingEntity target) {
    }

    /** The move used when the colossus first falls into a rage, if it has one. */
    @Nullable
    protected String rageAttack() {
        return null;
    }

    /** How often an attack is chosen; a variant can take a move away (0) or favour it. */
    protected int weight(ColossusAttack attack) {
        return attack.weight();
    }

    /** The sound of each heavy footfall. */
    protected abstract SoundEvent stepSound();

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEED, 0);
        builder.define(DATA_ACTION, 0);
        builder.define(DATA_ENRAGED, false);
    }

    public ColossusKind kind() {
        return this.kind;
    }

    public ColossusVariant variant() {
        return new ColossusVariant(this.kind, this.entityData.get(DATA_SEED));
    }

    public boolean enraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    /** Fixes this colossus's variant, and sizes and strengthens it to match. */
    public void setSeed(int seed) {
        this.entityData.set(DATA_SEED, seed == 0 ? 1 : seed);
        ColossusVariant variant = this.variant();
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(variant.size());
        }

        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && variant.elder()) {
            health.setBaseValue(health.getBaseValue() * 1.5);
        }

        this.setHealth(this.getMaxHealth());
    }

    /**
     * Readies a colossus being spawned (from the spawn event): picks its variant unless one is set, and keeps those
     * raised on purpose.
     */
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof ColossusEntity colossus) || event.isSpawnCancelled()) {
            return;
        }

        if (colossus.entityData.get(DATA_SEED) == 0) {
            colossus.setSeed(colossus.random.nextInt());
        }

        MobSpawnType reason = event.getSpawnType();
        if (reason == MobSpawnType.SPAWN_EGG || reason == MobSpawnType.COMMAND || reason == MobSpawnType.SPAWNER) {
            colossus.setPersistenceRequired();
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ColossusSeed", this.entityData.get(DATA_SEED));
        tag.putBoolean("Enraged", this.enraged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Size and health are saved with the attributes; only the seed itself needs restoring.
        this.entityData.set(DATA_SEED, tag.getInt("ColossusSeed"));
        this.entityData.set(DATA_ENRAGED, tag.getBoolean("Enraged"));
    }

    /** Named for its variant: "Mossback Cairn Colossus". */
    @Override
    protected Component getTypeName() {
        return this.entityData.get(DATA_SEED) == 0 ? super.getTypeName() : this.variant().name();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AttackGoal());
        this.goalSelector.addGoal(2, new ChaseGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6, 0.004F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, ColossusEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    // ---- Attacks ----------------------------------------------------------------------------------------------------

    /** The attack running on the client, as clients were last told, or null. */
    @Nullable
    public ColossusAttack clientAction() {
        int index = (this.entityData.get(DATA_ACTION) & 0xFF) - 1;
        return index >= 0 && index < this.attacks().size() ? this.attacks().get(index) : null;
    }

    /** How far through its attack the colossus is on the client, 0 to 1, or -1 when it is not attacking. */
    public float actionProgress(float partial) {
        ColossusAttack attack = this.clientAction();
        if (attack == null) {
            return -1.0F;
        }

        return Mth.clamp((this.tickCount + partial - this.clientActionStart) / attack.duration(), 0.0F, 1.0F);
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

    private double reachScale() {
        return this.getScale();
    }

    /** The attack damage, times a factor: what a given blow hits for. */
    protected float blow(float factor) {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor;
    }

    /** A point ahead of the colossus at a distance in blocks (scaled by its size), at its feet. */
    protected Vec3 ahead(double distance) {
        return this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(distance * this.getScale()));
    }

    protected Vec3 facing() {
        return Vec3.directionFromRotation(0.0F, this.getYRot());
    }

    protected void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, this.blockPosition(), sound, SoundSource.HOSTILE, volume, pitch);
    }

    private int chooseAttack(LivingEntity target) {
        double distance = Math.sqrt(Mth.square(target.getX() - this.getX()) + Mth.square(target.getZ() - this.getZ()));
        List<ColossusAttack> attacks = this.attacks();
        int total = 0;
        for (int i = 0; i < attacks.size(); i++) {
            if (this.usable(attacks.get(i), i, distance)) {
                total += this.weight(attacks.get(i));
            }
        }

        if (total <= 0) {
            return -1;
        }

        int roll = this.random.nextInt(total);
        for (int i = 0; i < attacks.size(); i++) {
            if (this.usable(attacks.get(i), i, distance)) {
                roll -= this.weight(attacks.get(i));
                if (roll < 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean usable(ColossusAttack attack, int index, double distance) {
        double scale = this.reachScale();
        return this.weight(attack) > 0 && this.cooldowns[index] <= 0 && distance >= attack.minRange() * scale && distance <= attack.maxRange() * scale;
    }

    /** Starts an attack by its index: tells clients, stops walking, and sounds its warning. */
    protected void beginAttack(int index) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        this.acting = this.attacks().get(index);
        this.actingIndex = index;
        this.actingTicks = 0;
        this.actionSerial = this.actionSerial + 1 & 0xFFFFFF;
        this.entityData.set(DATA_ACTION, this.actionSerial << 8 | index + 1);
        this.getNavigation().stop();
        this.windup(level, this.acting, this.getTarget());
    }

    /** Starts the named attack at once, whatever its range or cooldown, if nothing else is running. */
    protected void forceAttack(String id) {
        if (this.acting != null) {
            return;
        }

        List<ColossusAttack> attacks = this.attacks();
        for (int i = 0; i < attacks.size(); i++) {
            if (attacks.get(i).id().equals(id)) {
                this.beginAttack(i);
                return;
            }
        }
    }

    private void endAttack() {
        if (this.acting != null && this.actingIndex >= 0) {
            this.cooldowns[this.actingIndex] = (int)(this.acting.cooldown() * (this.enraged() ? 0.65 : 1.0));
        }

        this.acting = null;
        this.actingIndex = -1;
        this.restTicks = this.enraged() ? 8 : 16;
        this.entityData.set(DATA_ACTION, this.actionSerial << 8);
    }

    /** Turns toward a point, at most a few degrees a tick: a colossus can't spin on its heel. */
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

        if (!this.enraged() && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.enrage(level);
        }

        if (this.acting != null) {
            ColossusAttack attack = this.acting;
            this.actingTicks++;
            LivingEntity target = this.getTarget();
            this.getNavigation().stop();
            if (target != null && this.actingTicks < attack.firstImpact() - 4) {
                this.turnToward(target.getX(), target.getZ(), 6.0F);
            }

            this.actionTick(level, attack, this.actingTicks, target);
            int[] impacts = attack.impacts();
            for (int i = 0; i < impacts.length; i++) {
                if (impacts[i] == this.actingTicks) {
                    this.impact(level, attack, i, target);
                }
            }

            if (this.actingTicks >= attack.duration() && this.acting == attack) {
                this.endAttack();
            }
        }

        if (this.tickCount % 5 == 0 && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) {
            this.trample(level);
        }

        this.bossBar.setProgress(Mth.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
    }

    private void enrage(ServerLevel level) {
        this.entityData.set(DATA_ENRAGED, true);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(RAGE_SPEED)) {
            speed.addPermanentModifier(new AttributeModifier(RAGE_SPEED, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        Vfx.shockwave(level, this.position(), 12.0 * this.getScale(), this.palette());
        String rage = this.rageAttack();
        if (rage != null) {
            this.endAttack();
            this.forceAttack(rage);
        }
    }

    /** Leaves, grass and the like crushed underfoot as it walks (when mobs may grief). */
    private void trample(ServerLevel level) {
        if (!EventHooks.canEntityGrief(level, this)) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(0.3, 0.0, 0.3);
        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockState state = level.getBlockState(pos);
            boolean soft = state.is(BlockTags.LEAVES) || state.canBeReplaced() && !state.isAir() && state.getFluidState().isEmpty();
            if (soft && broken < 12) {
                level.destroyBlock(pos, false, this);
                broken++;
            }
        }
    }

    /** Picks an attack that suits the range whenever it is free to, and holds the body still while one runs. */
    private final class AttackGoal extends Goal {
        AttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (ColossusEntity.this.acting != null) {
                return true;
            }

            LivingEntity target = ColossusEntity.this.getTarget();
            if (target == null || !target.isAlive() || ColossusEntity.this.restTicks > 0) {
                return false;
            }

            int index = ColossusEntity.this.chooseAttack(target);
            if (index < 0) {
                return false;
            }

            ColossusEntity.this.beginAttack(index);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return ColossusEntity.this.acting != null;
        }
    }

    /** Walks straight at its target: paths are no use to something this size, so it steps over what it can. */
    private final class ChaseGoal extends Goal {
        ChaseGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ColossusEntity.this.getTarget();
            return ColossusEntity.this.acting == null && target != null && target.isAlive()
                && ColossusEntity.this.distanceToSqr(target) > Mth.square(ColossusEntity.this.getBbWidth() * 0.5 + 2.5);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = ColossusEntity.this.getTarget();
            if (target == null) {
                return;
            }

            ColossusEntity.this.getLookControl().setLookAt(target, 20.0F, 20.0F);
            ColossusEntity.this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
            if (ColossusEntity.this.horizontalCollision && ColossusEntity.this.onGround()) {
                ColossusEntity.this.getJumpControl().jump();
            }
        }

        @Override
        public void stop() {
            ColossusEntity.this.getNavigation().stop();
        }
    }

    // ---- Body -------------------------------------------------------------------------------------------------------

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FALL)
            || super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.stepSound(), 2.2F, 0.45F + this.random.nextFloat() * 0.1F);
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public float getVoicePitch() {
        return 0.55F + (this.random.nextFloat() - 0.5F) * 0.1F;
    }

    /** Big enough that its limbs, tail or body never pop out of view at the edge of the screen. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(this.cullingReach() * this.getScale());
    }

    protected double cullingReach() {
        return 5.0;
    }

    /** It takes a while to fall: the body sinks and collapses before it is gone, in a last burst of its light. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            if (this.deathTime == 1) {
                this.getNavigation().stop();
            }

            if (this.deathTime % 8 == 0) {
                Vfx.smoke(level, this.position().add(0.0, this.getBbHeight() * 0.4, 0.0), this.palette().deep, 6, this.getBbWidth());
            }

            if (this.deathTime >= DEATH_TICKS) {
                Vec3 middle = this.position().add(0.0, this.getBbHeight() * 0.3, 0.0);
                Vfx.flash(level, middle, this.palette(), 4.0F);
                Vfx.burst(level, middle, this.palette(), 60, 1.0);
                Vfx.shockwave(level, this.position(), 10.0 * this.getScale(), this.palette());
                level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.4F);
                level.broadcastEntityEvent(this, (byte)60);
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    /** "Cursed Titan - Mossback Cairn Colossus", in view of everyone who can see it. */
    private Component barTitle() {
        return Component.translatable("colossus.nightmare_evolved.bar", this.kind.rank.title(), this.kind.creatureClass().title(), this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossBar.setName(this.barTitle());
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossBar.removeAllPlayers();
    }

    /** Whether a damage source can still hurt something that has made itself untouchable (burrowing). */
    protected static boolean pierces(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }
}
