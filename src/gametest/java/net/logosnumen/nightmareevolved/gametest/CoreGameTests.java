package net.logosnumen.nightmareevolved.gametest;

import java.util.ArrayList;
import java.util.List;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.client.anim.PoseClips;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The foundations: work scheduled for later ticks runs on time, and every body animation the mod names exists. */
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
}
