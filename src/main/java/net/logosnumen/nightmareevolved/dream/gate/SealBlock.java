package net.logosnumen.nightmareevolved.dream.gate;

import com.mojang.serialization.MapCodec;
import net.logosnumen.nightmareevolved.dream.RealmState;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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

    /** The Oath Key turns in the lock: the seven stars shine, and the Dark Sea is drawn down into the Spire's well. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
        BlockHitResult hit) {
        if (!stack.is(MemoryItems.OATH_KEY.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (state.getValue(SEALED)) {
            return ItemInteractionResult.CONSUME;
        }

        if (level instanceof ServerLevel server) {
            server.setBlock(pos, state.setValue(SEALED, true), Block.UPDATE_ALL);
            RealmState.get(server.getServer()).setDarkSeaSealed(true);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            Vfx.pillar(server, Vec3.atCenterOf(pos), 30.0, Vfx.Palette.LIGHT);
            server.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 2.0F, 0.6F);
            for (ServerPlayer other : server.players()) {
                Spell.say(other, "spell.nightmare_evolved.dark_sea_sealed");
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
