package net.logosnumen.nightmareevolved.colossus;

import java.util.List;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.entity.colossus.CairnColossusEntity;
import net.logosnumen.nightmareevolved.entity.colossus.ChainboundColossusEntity;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusBoulderEntity;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.logosnumen.nightmareevolved.entity.colossus.GravewyrmEntity;
import net.logosnumen.nightmareevolved.entity.colossus.OssuaryBehemothEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Everything the colossi need registered, in one place: their entity types, the boulder they throw, spawn eggs, their
 * stats and where they may spawn.
 *
 * <p>For now they come from spawn eggs and {@code /ne colossus}. The spawn rule is ready for the wild: when a world of
 * the mod's own gives them a place, only one draw in four spawns, never within 2,000 blocks of the world's origin, never
 * within 160 blocks of another colossus, and only where there is room for the whole body.
 */
public final class Colossi {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NightmareEvolved.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NightmareEvolved.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CairnColossusEntity>> CAIRN = ENTITY_TYPES.register(
        "cairn_colossus", () -> EntityType.Builder.of(CairnColossusEntity::new, MobCategory.MONSTER).sized(3.0F, 8.6F).clientTrackingRange(10).build("cairn_colossus")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<OssuaryBehemothEntity>> OSSUARY = ENTITY_TYPES.register(
        "ossuary_behemoth",
        () -> EntityType.Builder.of(OssuaryBehemothEntity::new, MobCategory.MONSTER).sized(4.2F, 5.2F).clientTrackingRange(10).build("ossuary_behemoth")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<GravewyrmEntity>> GRAVEWYRM = ENTITY_TYPES.register(
        "gravewyrm", () -> EntityType.Builder.of(GravewyrmEntity::new, MobCategory.MONSTER).sized(2.6F, 2.6F).clientTrackingRange(12).build("gravewyrm")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ChainboundColossusEntity>> CHAINBOUND = ENTITY_TYPES.register(
        "chainbound_colossus",
        () -> EntityType.Builder.of(ChainboundColossusEntity::new, MobCategory.MONSTER).sized(2.6F, 8.2F).clientTrackingRange(10).build("chainbound_colossus")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ColossusBoulderEntity>> BOULDER = ENTITY_TYPES.register(
        "colossus_boulder",
        () -> EntityType.Builder.<ColossusBoulderEntity>of(ColossusBoulderEntity::new, MobCategory.MISC)
            .sized(1.2F, 1.2F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("colossus_boulder")
    );

    public static final DeferredItem<DeferredSpawnEggItem> CAIRN_EGG = ITEMS.register(
        "cairn_colossus_spawn_egg", () -> new DeferredSpawnEggItem(CAIRN, 0x5C5A55, 0xFFA845, new Item.Properties())
    );
    public static final DeferredItem<DeferredSpawnEggItem> OSSUARY_EGG = ITEMS.register(
        "ossuary_behemoth_spawn_egg", () -> new DeferredSpawnEggItem(OSSUARY, 0xD8D2C0, 0x5AEBD2, new Item.Properties())
    );
    public static final DeferredItem<DeferredSpawnEggItem> GRAVEWYRM_EGG = ITEMS.register(
        "gravewyrm_spawn_egg", () -> new DeferredSpawnEggItem(GRAVEWYRM, 0x4A3A3A, 0x9CFF5A, new Item.Properties())
    );
    public static final DeferredItem<DeferredSpawnEggItem> CHAINBOUND_EGG = ITEMS.register(
        "chainbound_colossus_spawn_egg", () -> new DeferredSpawnEggItem(CHAINBOUND, 0x3A3D44, 0xC8CCD8, new Item.Properties())
    );

    private Colossi() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(Colossi::onAttributes);
        modBus.addListener(Colossi::onSpawnPlacements);
        NeoForge.EVENT_BUS.addListener(Quake::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(ColossusEntity::onFinalizeSpawn);
    }

    public static EntityType<? extends ColossusEntity> typeOf(ColossusKind kind) {
        return switch (kind) {
            case CAIRN -> CAIRN.get();
            case OSSUARY -> OSSUARY.get();
            case GRAVEWYRM -> GRAVEWYRM.get();
            case CHAINBOUND -> CHAINBOUND.get();
        };
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(CAIRN.get(), ColossusEntity.colossusAttributes(360.0, 22.0, 14.0, 0.18).build());
        event.put(OSSUARY.get(), ColossusEntity.colossusAttributes(300.0, 18.0, 8.0, 0.24).build());
        event.put(GRAVEWYRM.get(), ColossusEntity.colossusAttributes(280.0, 18.0, 10.0, 0.26).build());
        event.put(CHAINBOUND.get(), ColossusEntity.colossusAttributes(420.0, 26.0, 18.0, 0.2).build());
    }

    private static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (EntityType<? extends ColossusEntity> type : List.of(CAIRN.get(), OSSUARY.get(), GRAVEWYRM.get(), CHAINBOUND.get())) {
            placement(event, type);
        }
    }

    private static <T extends ColossusEntity> void placement(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Colossi::checkSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** Natural spawns are rare, far out, alone and roomy; spawners, eggs and commands only need solid ground. */
    public static boolean checkSpawn(EntityType<? extends ColossusEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION) {
            if (random.nextInt(4) != 0 || (long)pos.getX() * pos.getX() + (long)pos.getZ() * pos.getZ() < 2000L * 2000L) {
                return false;
            }

            if (!level.noCollision(type.getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(pos)))) {
                return false;
            }

            if (!level.getEntitiesOfClass(ColossusEntity.class, new AABB(pos).inflate(160.0)).isEmpty()) {
                return false;
            }
        }

        return Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }
}
