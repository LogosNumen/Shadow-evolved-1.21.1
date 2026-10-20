package net.logosnumen.nightmareevolved.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import java.util.stream.Stream;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The game calls any world with a dimension beyond the vanilla three "experimental" and warns before loading it. The
 * Nightmare dimension is part of this mod, as settled as the mod itself, so it doesn't count toward that.
 */
@Mixin(WorldDimensions.class)
public abstract class WorldDimensionsMixin {
    private static boolean nightmare_evolved$ours(ResourceKey<LevelStem> key) {
        return key.location().getNamespace().equals(NightmareEvolved.MOD_ID);
    }

    @ModifyReturnValue(method = "checkStability", at = @At("RETURN"))
    private static Lifecycle nightmare_evolved$stableNightmare(Lifecycle stability, @Local(argsOnly = true) ResourceKey<LevelStem> key) {
        return nightmare_evolved$ours(key) ? Lifecycle.stable() : stability;
    }

    /** Leaves this mod's dimensions out of the count that decides whether the world has only the vanilla three. */
    @ModifyExpressionValue(method = "bake", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int nightmare_evolved$countWithoutNightmare(int size, @Local(argsOnly = true) Registry<LevelStem> stemRegistry) {
        long ours = Stream.concat(stemRegistry.registryKeySet().stream(), ((WorldDimensions)(Object)this).dimensions().keySet().stream())
            .distinct()
            .filter(key -> nightmare_evolved$ours(key))
            .count();
        return size - (int)ours;
    }
}
