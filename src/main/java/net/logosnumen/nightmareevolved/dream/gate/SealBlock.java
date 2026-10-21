package net.logosnumen.nightmareevolved.dream.gate;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The lock engraved into the Crimson Spire's ancient gate: a sigil of seven shining stars. The seven Shard Memories,
 * joined into the Oath Key and put into it, seal the Dark Sea away into the Spire's well.
 */
public final class SealBlock extends Block {
    public static final MapCodec<SealBlock> CODEC = simpleCodec(SealBlock::new);
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");

    public SealBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SEALED, false));
    }

    @Override
    protected MapCodec<? extends SealBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEALED);
    }
}
