package net.logosnumen.nightmareevolved.dream;

import java.util.List;
import java.util.function.Predicate;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.dream.gate.GatewayBlock;
import net.logosnumen.nightmareevolved.dream.gate.SealBlock;
import net.logosnumen.nightmareevolved.dream.gate.SeedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * What the Dream Realm is made of where nothing in the waking world will do: the Forgotten Shore's black earth and
 * blade-like crimson coral, ash, the Desert's white sand, Godgrave's vermilion grass and red moss, the flawless ivory of
 * the Ivory Tower and the lightless ebony of its twin, Ravenheart's jade, the charred trunks of the Burned Forest, the
 * black-barked tree on the Ashen Barrow, and stone carved with shining runes.
 */
public final class DreamBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NightmareEvolved.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NightmareEvolved.MOD_ID);

    public static final DeferredBlock<Block> BLACK_EARTH = simple("black_earth",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.6F).sound(SoundType.ROOTED_DIRT));
    public static final DeferredBlock<Block> CRIMSON_CORAL = simple("crimson_coral",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.8F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.CORAL_BLOCK));
    public static final DeferredBlock<Plant> CRIMSON_CORAL_BLADES = plant("crimson_coral_blades", MapColor.COLOR_RED, SoundType.CORAL_BLOCK,
        state -> state.is(CRIMSON_CORAL.get()) || state.is(BLACK_EARTH.get()), true);
    public static final DeferredBlock<ColoredFallingBlock> ASH = BLOCKS.register("ash",
        () -> new ColoredFallingBlock(new ColorRGBA(0xFF8A8580), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.5F).sound(SoundType.SAND)));
    public static final DeferredBlock<AshLayer> ASH_LAYER = BLOCKS.register("ash_layer",
        () -> new AshLayer(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).replaceable().strength(0.1F)
            .sound(SoundType.SAND).isViewBlocking((state, level, pos) -> state.getValue(AshLayer.LAYERS) >= 8).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<ColoredFallingBlock> PALE_SAND = BLOCKS.register("pale_sand",
        () -> new ColoredFallingBlock(new ColorRGBA(0xFFF2EEE6), BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.5F).sound(SoundType.SAND)));
    public static final DeferredBlock<Block> VERMILION_TURF = simple("vermilion_turf",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.2F).sound(SoundType.NYLIUM));
    public static final DeferredBlock<Plant> VERMILION_GRASS = plant("vermilion_grass", MapColor.COLOR_ORANGE, SoundType.GRASS,
        state -> state.is(VERMILION_TURF.get()) || state.is(Blocks.BONE_BLOCK) || state.is(DreamBlocks.RED_MOSS.get()), false);
    public static final DeferredBlock<Block> RED_MOSS = simple("red_moss",
        BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_NYLIUM).strength(0.1F).sound(SoundType.MOSS));
    public static final DeferredBlock<CarpetBlock> RED_MOSS_CARPET = BLOCKS.register("red_moss_carpet",
        () -> new CarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_NYLIUM).strength(0.1F).sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> IVORY = simple("ivory",
        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(3.0F, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.CALCITE));
    public static final DeferredBlock<Block> EBONY = simple("ebony",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0F, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE_TILES));
    public static final DeferredBlock<Block> WHITE_JADE = simple("white_jade",
        BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(2.5F, 9.0F).requiresCorrectToolForDrops().sound(SoundType.CALCITE));
    public static final DeferredBlock<Block> BLACK_JADE = simple("black_jade",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.5F, 9.0F).requiresCorrectToolForDrops().sound(SoundType.GLASS));
    public static final DeferredBlock<RotatedPillarBlock> CHARRED_LOG = BLOCKS.register("charred_log",
        () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0F).sound(SoundType.BASALT)));
    public static final DeferredBlock<RotatedPillarBlock> BLACK_BARK_LOG = BLOCKS.register("black_bark_log",
        () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.5F).sound(SoundType.WOOD)));
    public static final DeferredBlock<LeavesBlock> BLOOD_LEAVES = BLOCKS.register("blood_leaves",
        () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)
            .pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, level, pos) -> false)));
    public static final DeferredBlock<Block> RUNE_STONE = simple("rune_stone",
        BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0F, 9.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE_BRICKS)
            .lightLevel(state -> 6));

    /** The heart of a Gateway. */
    public static final DeferredBlock<GatewayBlock> GATEWAY = BLOCKS.register("gateway", () -> new GatewayBlock(BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0F, 3600000.0F).noLootTable().sound(SoundType.DEEPSLATE_TILES).noOcclusion()
        .lightLevel(state -> switch (state.getValue(GatewayBlock.FORM)) {
            case RING -> 9;
            case WHITE_ALTAR, BLACK_ALTAR -> 6;
            default -> 12;
        }).isValidSpawn((state, level, pos, type) -> false).pushReaction(PushReaction.BLOCK)));
    /** A Seed of Nightmare. */
    public static final DeferredBlock<SeedBlock> SEED = BLOCKS.register("seed_of_nightmare", () -> new SeedBlock(BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable().sound(SoundType.DEEPSLATE).noOcclusion()
        .lightLevel(state -> state.getValue(SeedBlock.BLOOMED) ? 4 : 0).isValidSpawn((state, level, pos, type) -> false).pushReaction(PushReaction.BLOCK)));
    /** The seven-starred lock on the Crimson Spire's gate, where the Oath Key seals the Dark Sea away. */
    public static final DeferredBlock<SealBlock> SEAL = BLOCKS.register("seven_star_lock", () -> new SealBlock(BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_GRAY).strength(-1.0F, 3600000.0F).noLootTable().sound(SoundType.DEEPSLATE_BRICKS)
        .lightLevel(state -> state.getValue(SealBlock.SEALED) ? 10 : 3).pushReaction(PushReaction.BLOCK)));

    /** Every block above, in the order the creative tab lists them. */
    public static final List<DeferredBlock<? extends Block>> ALL = List.of(
        BLACK_EARTH, CRIMSON_CORAL, CRIMSON_CORAL_BLADES, ASH, ASH_LAYER, PALE_SAND, VERMILION_TURF, VERMILION_GRASS, RED_MOSS, RED_MOSS_CARPET, IVORY,
        EBONY, WHITE_JADE, BLACK_JADE, CHARRED_LOG, BLACK_BARK_LOG, BLOOD_LEAVES, RUNE_STONE
    );

    static {
        for (DeferredBlock<? extends Block> block : ALL) {
            item(block);
        }

        item(GATEWAY);
        item(SEED);
        item(SEAL);
    }

    private DreamBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    private static DeferredBlock<Block> simple(String id, BlockBehaviour.Properties properties) {
        return BLOCKS.register(id, () -> new Block(properties));
    }

    private static DeferredBlock<Plant> plant(String id, MapColor colour, SoundType sound, Predicate<BlockState> soil, boolean cuts) {
        return BLOCKS.register(id, () -> new Plant(BlockBehaviour.Properties.of().mapColor(colour).noCollission().instabreak().sound(sound)
            .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY).replaceable(), soil, cuts));
    }

    private static <B extends Block> DeferredItem<BlockItem> item(DeferredBlock<B> block) {
        return ITEMS.registerSimpleBlockItem(block);
    }

    /** The block items, for the creative tab. */
    public static List<Item> items() {
        return ALL.stream().map(block -> block.get().asItem()).toList();
    }

    /**
     * A small growing thing that only takes root on its own ground. The Shore's coral blades cut whatever pushes through
     * them.
     */
    public static final class Plant extends BushBlock {
        private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
        private final Predicate<BlockState> soil;
        private final boolean cuts;
        private final MapCodec<Plant> codec;

        public Plant(BlockBehaviour.Properties properties, Predicate<BlockState> soil, boolean cuts) {
            super(properties);
            this.soil = soil;
            this.cuts = cuts;
            this.codec = simpleCodec(p -> new Plant(p, soil, cuts));
        }

        @Override
        protected MapCodec<Plant> codec() {
            return this.codec;
        }

        @Override
        protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
            return this.soil.test(state);
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            Vec3 offset = state.getOffset(level, pos);
            return SHAPE.move(offset.x, offset.y, offset.z);
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            if (!this.cuts || !(entity instanceof LivingEntity) || level.isClientSide) {
                return;
            }

            entity.makeStuckInBlock(state, new Vec3(0.85, 0.8, 0.85));
            if (Math.abs(entity.getX() - entity.xOld) >= 0.003 || Math.abs(entity.getZ() - entity.zOld) >= 0.003) {
                entity.hurt(DreamDamage.source(level, DreamDamage.CRIMSON_CORAL), 1.0F);
            }
        }
    }

    /** Ash lying in layers, like snow that never melts. */
    public static final class AshLayer extends Block {
        public static final MapCodec<AshLayer> CODEC = simpleCodec(AshLayer::new);
        public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
        private static final VoxelShape[] SHAPES = new VoxelShape[9];

        static {
            SHAPES[0] = Shapes.empty();
            for (int i = 1; i <= 8; i++) {
                SHAPES[i] = Block.box(0.0, 0.0, 0.0, 16.0, i * 2.0, 16.0);
            }
        }

        public AshLayer(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
        }

        @Override
        protected MapCodec<AshLayer> codec() {
            return CODEC;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(LAYERS);
        }

        @Override
        protected boolean isPathfindable(BlockState state, PathComputationType type) {
            return type == PathComputationType.LAND && state.getValue(LAYERS) < 5;
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPES[state.getValue(LAYERS)];
        }

        @Override
        protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPES[state.getValue(LAYERS) - 1];
        }

        @Override
        protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
            return SHAPES[state.getValue(LAYERS)];
        }

        @Override
        protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPES[state.getValue(LAYERS)];
        }

        @Override
        protected boolean useShapeForLightOcclusion(BlockState state) {
            return true;
        }

        @Override
        protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            BlockState below = level.getBlockState(pos.below());
            return Block.isFaceFull(below.getCollisionShape(level, pos.below()), Direction.UP) || below.is(this) && below.getValue(LAYERS) == 8;
        }

        @Override
        protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
            return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, pos, facingPos);
        }

        @Override
        protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
            int layers = state.getValue(LAYERS);
            if (!context.getItemInHand().is(this.asItem()) || layers >= 8) {
                return layers == 1;
            }

            return !context.replacingClickedOnBlock() || context.getClickedFace() == Direction.UP;
        }

        @Nullable
        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            BlockState here = context.getLevel().getBlockState(context.getClickedPos());
            return here.is(this) ? here.setValue(LAYERS, Math.min(8, here.getValue(LAYERS) + 1)) : super.getStateForPlacement(context);
        }

        /** Ash of a depth from 1 to 8 layers. */
        public BlockState depth(int layers) {
            return this.defaultBlockState().setValue(LAYERS, Math.max(1, Math.min(8, layers)));
        }
    }
}
