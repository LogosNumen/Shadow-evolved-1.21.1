package net.logosnumen.nightmareevolved.dream.gate;

import java.util.Locale;
import com.mojang.serialization.MapCodec;
import net.logosnumen.nightmareevolved.dream.seed.Seeds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Seed of Nightmare: a vile thing the Spell grows in the Dream Realm, holding a Nightmare of its Category and calling
 * to every Awakened who comes near. Most are blocks of black stone breathing a ghostly darkness; the Dark Seed in the
 * Ivory Tower is seven shackles inscribed with runes. Once it blooms, a Gate opens in the waking world.
 */
public final class SeedBlock extends Block {
    public static final MapCodec<SeedBlock> CODEC = simpleCodec(SeedBlock::new);
    public static final EnumProperty<Form> FORM = EnumProperty.create("form", Form.class);
    /** The Category of the Nightmare inside: two for a Second Nightmare, up to six. */
    public static final IntegerProperty CATEGORY = IntegerProperty.create("category", 2, 6);
    public static final BooleanProperty BLOOMED = BooleanProperty.create("bloomed");

    public enum Form implements StringRepresentable {
        STONE,
        SHACKLES;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public SeedBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORM, Form.STONE).setValue(CATEGORY, 2).setValue(BLOOMED, false));
    }

    @Override
    protected MapCodec<? extends SeedBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORM, CATEGORY, BLOOMED);
    }

    public BlockState seed(Form form, int category) {
        return this.defaultBlockState().setValue(FORM, form).setValue(CATEGORY, Math.max(2, Math.min(6, category)));
    }

    /** Laying a hand on a Seed challenges it. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer server) {
            Seeds.challenge(server, pos, state);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** A ghostly darkness rises off it, thicker the deeper the Nightmare inside, and thicker still once it blooms. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int puffs = state.getValue(CATEGORY) - 1 + (state.getValue(BLOOMED) ? 3 : 0);
        for (int i = 0; i < puffs; i++) {
            if (random.nextInt(3) != 0) {
                continue;
            }

            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.4;
            double y = pos.getY() + random.nextDouble() * 1.3;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.4;
            level.addParticle(ParticleTypes.SQUID_INK, x, y, z, 0.0, 0.03, 0.0);
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.SCULK_SOUL, x, y + 0.4, z, 0.0, 0.02, 0.0);
            }
        }
    }
}
