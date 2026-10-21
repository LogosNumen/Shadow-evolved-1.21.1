package net.logosnumen.nightmareevolved.dream.seed;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.colossus.Colossi;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.dream.DreamDamage;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.nightmare.Trial;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * A Nightmare Gate: the rift that opens in the waking world when a Seed of Nightmare blooms. First it is only a tall
 * depression in the world where light bends wrongly; then it tears open, a rift of vile darkness, and Nightmare
 * Creatures pour through, the first waves mostly Dormant, later ones stronger, until the Guardian of the Gate comes,
 * at least one rank above the Gate's Category. Slaying the Guardian closes the Gate; so does conquering its Seed in the
 * Dream Realm. To touch the Gate from this side is death. An Awakened who sleeps near it is drawn by its Call to the
 * Seed.
 */
public final class NightmareGateEntity extends Entity {
    /** How long a Gate takes to open once it begins to form. */
    public static final int FORMING = 1200;
    private static final int WAVE = 300;
    private static final String TAG = "nightmare_evolved_gate";
    private static final EntityDataAccessor<Integer> CATEGORY = SynchedEntityData.defineId(NightmareGateEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(NightmareGateEntity.class, EntityDataSerializers.INT);
    private String seed = "";
    private int age;
    /** On clients: the tick the age last came from the server, so it can be counted on between updates. */
    private int syncedAt;
    private int waves;
    @Nullable
    private UUID guardian;
    private boolean guardianCame;

    public NightmareGateEntity(EntityType<? extends NightmareGateEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static NightmareGateEntity open(ServerLevel level, BlockPos at, String seed, int category) {
        NightmareGateEntity gate = SeedEntities.GATE.get().create(level);
        if (gate == null) {
            throw new IllegalStateException("No Nightmare Gate type");
        }

        gate.seed = seed;
        gate.entityData.set(CATEGORY, category);
        gate.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(gate);
        return gate;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CATEGORY, 2);
        builder.define(AGE, 0);
    }

    public int category() {
        return this.entityData.get(CATEGORY);
    }

    public int age() {
        return this.age;
    }

    /** The age as a client sees it: the last age the server sent, counted on since. */
    public int clientAge() {
        return this.entityData.get(AGE) + this.tickCount - this.syncedAt;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (AGE.equals(key)) {
            this.syncedAt = this.tickCount;
        }
    }

    public String seed() {
        return this.seed;
    }

    public boolean isOpen() {
        return this.age() >= FORMING;
    }

    /** How tall and wide the rift stands, by its Category. */
    public float height() {
        return 7.0F + this.category() * 2.5F;
    }

    public float width() {
        return 2.5F + this.category() * 0.8F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        int age = ++this.age;
        if (age % 40 == 0 || age == FORMING) {
            this.entityData.set(AGE, age);
        }
        if (age == FORMING) {
            level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0F, 0.5F);
            Vfx.shockwave(level, this.position(), 12.0, Vfx.Palette.SHADOW);
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                Seeds.tell(player, "gate_opened", this.category());
            }
        }

        if (age % 10 == 0) {
            this.burn(level);
        }

        if (!this.isOpen() || age % 20 != 0) {
            return;
        }

        if (this.guardianCame) {
            Entity guardian = this.guardian == null ? null : level.getEntity(this.guardian);
            if (!(guardian instanceof LivingEntity living) || !living.isAlive()) {
                // The Guardian is slain: the Gate closes.
                Seeds.gateClosed(level.getServer(), this.seed, false);
                this.close(level);
            }

            return;
        }

        if ((age - FORMING) % WAVE == 0) {
            if (this.waves < 3 + this.category()) {
                this.wave(level);
            } else {
                this.summonGuardian(level);
            }
        }
    }

    /** Whatever touches the rift from this side is torn by it. */
    private void burn(ServerLevel level) {
        float w = this.width() / 2.0F;
        AABB rift = new AABB(this.getX() - w, this.getY(), this.getZ() - w, this.getX() + w, this.getY() + this.height(), this.getZ() + w);
        DamageSource source = DreamDamage.source(level, DreamDamage.GATE);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, rift, e -> e instanceof Player && e.isAlive() && !e.isSpectator())) {
            living.hurt(source, this.isOpen() ? 8.0F : 2.0F);
        }
    }

    /** A wave through the rift: weaker kinds at first, stronger as the Gate grows. */
    private void wave(ServerLevel level) {
        int cap = 6 + this.category() * 2;
        if (this.nearbyCreatures(level) >= cap) {
            return;
        }

        this.waves++;
        Trial trial = trial(this.category());
        RandomSource random = level.random;
        int count = 3 + this.category() + this.waves;
        int top = Math.min(CreatureRank.values().length - 1, Math.max(0, this.category() - 1));
        for (int i = 0; i < count; i++) {
            boolean early = this.waves <= 2;
            int rank = early ? Math.max(0, top - (random.nextFloat() < 0.75F ? 1 : 0)) : top - (random.nextFloat() < 0.3F ? 1 : 0);
            CreatureClass[] classes = CreatureClass.values();
            CreatureClass creatureClass = classes[Math.min(2, random.nextInt(1 + Math.min(this.waves, 2)))];
            this.spawn(level, trial.pool.get(random.nextInt(trial.pool.size())), CreatureRank.values()[Math.max(0, rank)], creatureClass, false);
        }

        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 2.5F, 0.7F);
    }

    /** The Guardian of the Gate: at least a rank above its Category, of a fearsome class. */
    private void summonGuardian(ServerLevel level) {
        Trial trial = trial(this.category());
        CreatureRank rank = CreatureRank.values()[Math.min(CreatureRank.values().length - 1, this.category())];
        CreatureClass creatureClass = switch (this.category()) {
            case 2 -> CreatureClass.DEVIL;
            case 3 -> CreatureClass.TYRANT;
            case 4 -> CreatureClass.TERROR;
            default -> CreatureClass.TITAN;
        };
        Mob guardian;
        if (trial.colossus != null) {
            ColossusEntity colossus = Colossi.typeOf(trial.colossus).create(level);
            if (colossus != null) {
                colossus.setSeed(level.random.nextInt());
            }

            guardian = colossus;
        } else {
            Entity entity = (trial.guardian != null ? trial.guardian : trial.pool.get(0)).create(level);
            guardian = entity instanceof Mob mob ? mob : null;
        }

        if (guardian == null) {
            return;
        }

        this.place(level, guardian, rank, creatureClass, true);
        this.guardian = guardian.getUUID();
        this.guardianCame = true;
        level.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 4.0F, 0.5F);
        for (ServerPlayer player : level.players()) {
            if (player.distanceTo(this) < 160.0) {
                Seeds.tell(player, "gate_guardian", Creatures.title(rank, creatureClass));
            }
        }
    }

    private void spawn(ServerLevel level, EntityType<?> type, CreatureRank rank, CreatureClass creatureClass, boolean guardian) {
        Entity entity = type.create(level);
        if (entity instanceof Mob mob) {
            this.place(level, mob, rank, creatureClass, guardian);
        } else if (entity != null) {
            entity.discard();
        }
    }

    private void place(ServerLevel level, Mob mob, CreatureRank rank, CreatureClass creatureClass, boolean guardian) {
        double a = level.random.nextDouble() * Math.PI * 2.0;
        double r = 2.0 + level.random.nextDouble() * 3.0;
        double x = this.getX() + Math.cos(a) * r;
        double z = this.getZ() + Math.sin(a) * r;
        double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(x), (int)Math.floor(z));
        mob.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.EVENT, null);
        Creatures.raise(mob, rank, creatureClass, guardian, -1L);
        mob.getPersistentData().putUUID(TAG, this.getUUID());
        level.addFreshEntity(mob);
        Vfx.smoke(level, mob.position().add(0.0, 1.0, 0.0), Vfx.Palette.SHADOW.deep, 10, 0.8);
    }

    private int nearbyCreatures(ServerLevel level) {
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(96.0),
            mob -> mob.isAlive() && this.sent(mob));
        return mobs.size();
    }

    static Trial trial(int category) {
        Trial[] all = Trial.values();
        return all[Math.max(Trial.SECOND_NIGHTMARE.ordinal(), Math.min(all.length - 1, category))];
    }

    /** The Gate closes: it folds shut and is gone. */
    public void close(ServerLevel level) {
        Vfx.flash(level, this.position().add(0.0, this.height() / 2.0, 0.0), Vfx.Palette.LIGHT, 4.0F);
        level.playSound(null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.5F);
        this.discard();
    }

    /** Whether a creature came through this Gate. */
    public boolean sent(Entity entity) {
        return entity.getPersistentData().hasUUID(TAG) && entity.getPersistentData().getUUID(TAG).equals(this.getUUID());
    }

    /** Whether a place lies within reach of this Gate's Call. */
    public boolean calls(Vec3 at) {
        return this.isOpen() && this.position().distanceTo(at) < 64.0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 384.0 * 384.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.seed = tag.getString("Seed");
        this.entityData.set(CATEGORY, Math.max(2, tag.getInt("Category")));
        this.age = tag.getInt("Age");
        this.entityData.set(AGE, this.age);
        this.waves = tag.getInt("Waves");
        this.guardian = tag.hasUUID("Guardian") ? tag.getUUID("Guardian") : null;
        this.guardianCame = tag.getBoolean("GuardianCame");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Seed", this.seed);
        tag.putInt("Category", this.category());
        tag.putInt("Age", this.age());
        tag.putInt("Waves", this.waves);
        if (this.guardian != null) {
            tag.putUUID("Guardian", this.guardian);
        }

        tag.putBoolean("GuardianCame", this.guardianCame);
    }
}
