package net.logosnumen.nightmareevolved.gametest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.mojang.serialization.Lifecycle;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.client.anim.PoseClips;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The foundations: work scheduled for later ticks runs on time, every body animation the mod names exists, and the
 * Nightmare dimension doesn't make a world "experimental".
 */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class CoreGameTests {
    @GameTest(template = "empty")
    public static void scheduledWorkRunsOnTime(GameTestHelper helper) {
        long[] ranAt = {-1L};
        int[] steps = {0};
        long start = helper.getLevel().getGameTime();
        Vfx.later(3, () -> ranAt[0] = helper.getLevel().getGameTime());
        Vfx.over(5, tick -> steps[0]++);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(ranAt[0] - start == 3, "Delayed work should run three ticks later, ran after " + (ranAt[0] - start));
            helper.assertTrue(steps[0] == 5, "Repeated work should run once a tick for five ticks, ran " + steps[0]);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void everyBodyAnimationExists(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (String clip : List.of("cast", "cast_both", "raise", "raise_one", "slash", "slash_down", "spin", "roar", "breath", "slam", "dash", "guard", "channel",
            "channel_self", "write", "point", "flap", "cloak", "rise", "transform_gather", "transform_burst")) {
            if (PoseClips.get(clip) == null) {
                missing.add(clip);
            }
        }

        helper.assertTrue(missing.isEmpty(), "Missing body animations: " + missing);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nightmareDimensionKeepsWorldsStable(GameTestHelper helper) {
        helper.assertTrue(lifecycleWith(helper, NightmareEvolved.id("nightmare")) == Lifecycle.stable(),
            "The Nightmare dimension shouldn't mark a world experimental");
        helper.assertTrue(lifecycleWith(helper, ResourceLocation.fromNamespaceAndPath("elsewhere", "extra")) != Lifecycle.stable(),
            "Other mods' dimensions should be judged as before");
        helper.succeed();
    }

    /** How the game judges a world with the vanilla dimensions and one more, as it does before loading it. */
    private static Lifecycle lifecycleWith(GameTestHelper helper, ResourceLocation extra) {
        Registry<LevelStem> vanilla = helper.getLevel().getServer().registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        Map<ResourceKey<LevelStem>, LevelStem> stems = new LinkedHashMap<>();
        vanilla.entrySet().forEach(entry -> stems.put(entry.getKey(), entry.getValue()));
        LevelStem overworld = vanilla.getOrThrow(LevelStem.OVERWORLD);
        stems.put(ResourceKey.create(Registries.LEVEL_STEM, extra), new LevelStem(overworld.type(), overworld.generator()));
        return new WorldDimensions(stems).bake(new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze()).lifecycle();
    }
}
