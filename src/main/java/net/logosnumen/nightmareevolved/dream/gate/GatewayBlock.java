package net.logosnumen.nightmareevolved.dream.gate;

import java.util.Locale;
import javax.annotation.Nullable;
import com.mojang.serialization.MapCodec;
import net.logosnumen.nightmareevolved.dream.DreamTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The heart of a Gateway, the way out of the Dream Realm. Each looks as it does in the novel: the stone in the middle of
 * the Crimson Spire's iron ring, the white marble altar of the Sanctuary of Noctis, the Night Temple's black altar, the
 * pond under Vanishing Lake's crystal dome, or a shining stone in a castle's or a palace's heart.
 */
public final class GatewayBlock extends Block {
    public static final MapCodec<GatewayBlock> CODEC = simpleCodec(GatewayBlock::new);
    public static final EnumProperty<Form> FORM = EnumProperty.create("form", Form.class);
    private static final VoxelShape ALTAR = Shapes.or(Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0), Block.box(3.0, 4.0, 3.0, 13.0, 11.0, 13.0),
        Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0));

    /** How a Gateway looks, and how it is used. */
    public enum Form implements StringRepresentable {
        /** Stand inside the ring. */
        RING,
        /** Touch it. */
        WHITE_ALTAR,
        /** Come near it. */
        BLACK_ALTAR,
        /** Step into the water over it. */
        POOL,
        /** Touch it. */
        HEART;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        @Nullable
        public static Form byName(String name) {
            for (Form form : values()) {
                if (form.getSerializedName().equals(name)) {
                    return form;
                }
            }

            return null;
        }
    }

    public GatewayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORM, Form.RING));
    }

    @Override
    protected MapCodec<? extends GatewayBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORM);
    }

    public BlockState form(Form form) {
        return this.defaultBlockState().setValue(FORM, form);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Form form = state.getValue(FORM);
        return form == Form.WHITE_ALTAR || form == Form.BLACK_ALTAR ? ALTAR : Shapes.block();
    }

    /** Standing in the Crimson Spire's ring, or in the pond over Vanishing Lake's, begins the passage. */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        Form form = state.getValue(FORM);
        if (!level.isClientSide && (form == Form.RING || form == Form.POOL) && entity instanceof ServerPlayer player) {
            DreamTravel.touch(player, pos);
        }

        super.stepOn(level, pos, state, entity);
    }

    /**
     * The altars of the Chained Isles answer offerings: blood shed on the Night Temple's black altar is repaid with the
     * ivory knife, and a thousand strange coins laid on the Sanctuary's white altar under the moon with the obsidian one.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
        BlockHitResult hit) {
        Form form = state.getValue(FORM);
        if ((form == Form.BLACK_ALTAR || form == Form.WHITE_ALTAR) && player instanceof ServerPlayer server && Altars.offer(server, pos, form, stack)) {
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Touching an altar or a Gateway's heart begins the passage. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer server) {
            Gateways.Gateway gateway = player.isShiftKeyDown() ? Gateways.at(server.serverLevel(), pos) : null;
            if (gateway != null && gateway.citadel() != null) {
                Citadels.bind(server, gateway);
            } else {
                DreamTravel.touch(server, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }

        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.6;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.6;
        double y = pos.getY() + 1.05 + random.nextDouble() * 0.4;
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02 + random.nextDouble() * 0.03, 0.0);
    }
}
