package net.logosnumen.nightmareevolved.soulsea;

import com.mojang.serialization.MapCodec;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The still water and the black floor of a Soul Sea. Unbreakable, never dropped, and found only in the Soul Sea. The
 * surface is solid: the sea is walked on, not swum in.
 */
public final class SeaBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NightmareEvolved.MOD_ID);
    /** Footsteps on the still surface: a soft, low chime. */
    public static final SoundType STILL_WATER = new DeferredSoundType(0.25F, 0.5F, () -> SoundEvents.AMETHYST_BLOCK_BREAK,
        () -> SoundEvents.AMETHYST_BLOCK_STEP, () -> SoundEvents.AMETHYST_BLOCK_PLACE, () -> SoundEvents.AMETHYST_BLOCK_HIT,
        () -> SoundEvents.AMETHYST_BLOCK_FALL);
    public static final DeferredBlock<Still> SURFACE = BLOCKS.register("soul_sea_surface", () -> new Still(properties(true)));
    public static final DeferredBlock<Still> BED = BLOCKS.register("soul_sea_bed", () -> new Still(properties(false)));

    private SeaBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private static BlockBehaviour.Properties properties(boolean surface) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(-1.0F, 3600000.0F)
            .noLootTable()
            .isValidSpawn((state, level, pos, type) -> false)
            .pushReaction(PushReaction.BLOCK)
            .sound(STILL_WATER);
        return surface ? properties.noOcclusion().lightLevel(state -> 2) : properties;
    }

    /** A block of the sea itself. */
    public static final class Still extends Block {
        public static final MapCodec<Still> CODEC = simpleCodec(Still::new);

        public Still(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }
    }
}
