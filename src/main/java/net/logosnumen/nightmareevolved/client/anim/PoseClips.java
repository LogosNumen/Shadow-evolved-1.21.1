package net.logosnumen.nightmareevolved.client.anim;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The body animations abilities play: casts and thrusts, cuts and spins, roars and slams, guards and channels, and
 * the Transcendent Form's gathering and burst. Arm angles follow the model: a negative x lifts an arm forward and up,
 * and a positive z swings the right arm (negative the left) out to the side.
 */
public final class PoseClips {
    private static final Map<String, PoseClip> CLIPS = new HashMap<>();

    static {
        add(PoseClip.clip("cast", 12)
            .key(0.25F).body(0, 15, 0).rightArm(-60, 0, 15).hand(0.0F, 0.05F, 0.1F, 0, 0, 0)
            .key(0.5F).body(0, -15, 0).rightArm(-95, 5, 0).leftArm(25, 0, -8).hand(0.0F, 0.12F, -0.35F, -20, 0, 0)
            .key(0.75F).hold());
        add(PoseClip.clip("cast_both", 14)
            .key(0.3F).body(-5, 0, 0).arms(-85, 12, 0).hand(0.0F, 0.1F, -0.3F, -15, 0, 0)
            .key(0.75F).hold());
        add(PoseClip.clip("raise", 20)
            .key(0.3F).head(-25, 0, 0).body(-6, 0, 0).arms(-165, 0, 12).hand(0.0F, 0.45F, -0.1F, -50, 0, 0)
            .key(0.75F).head(-30, 0, 0).body(-8, 0, 0).arms(-170, 0, 18).hand(0.0F, 0.5F, -0.1F, -55, 0, 0)
            .shake(0.25F, 0.35F));
        add(PoseClip.clip("raise_one", 14)
            .key(0.3F).head(-15, 0, 0).rightArm(-165, 0, 8).hand(0.0F, 0.4F, -0.1F, -45, 0, 0)
            .key(0.7F).hold());
        add(PoseClip.clip("slash", 10)
            .key(0.2F).body(0, 25, 0).rightArm(-150, 0, 35).hand(0.15F, 0.3F, 0.05F, 0, 0, -40)
            .key(0.45F).body(0, -30, 0).rightArm(-45, 0, -30).leftArm(15, 0, -10).hand(-0.3F, -0.15F, -0.25F, 0, 0, 40)
            .key(0.7F).body(0, -25, 0).rightArm(-25, 0, -40).leftArm(15, 0, -10).hand(-0.35F, -0.2F, -0.2F, 0, 0, 45));
        add(PoseClip.clip("slash_down", 12)
            .key(0.25F).head(-10, 0, 0).body(-8, 0, 0).rightArm(-175, 0, 5).hand(0.0F, 0.45F, 0.0F, -60, 0, 0)
            .key(0.5F).head(10, 0, 0).body(20, 0, 0).rightArm(-40, 0, 0).hand(0.0F, -0.25F, -0.3F, 50, 0, 0)
            .key(0.75F).hold());
        add(PoseClip.clip("spin", 14)
            .key(0.1F).arms(-10, 0, 80).root(0, 0, 0).hand(0.2F, 0.0F, -0.1F, 0, 0, -30)
            .key(0.8F).arms(-10, 0, 85).root(360, 0, 0).hand(-0.3F, 0.0F, -0.2F, 0, 0, 50));
        add(PoseClip.clip("roar", 20)
            .key(0.2F).head(-30, 0, 0).body(-15, 0, 0).arms(30, 0, 35).hand(0.0F, -0.2F, 0.1F, 30, 0, 0)
            .key(0.75F).head(-35, 0, 0).body(-18, 0, 0).arms(35, 0, 40).hand(0.0F, -0.2F, 0.1F, 30, 0, 0)
            .shake(0.6F, 0.25F));
        add(PoseClip.clip("breath", 24)
            .key(0.15F).head(15, 0, 0).body(15, 0, 0).arms(35, 0, 20).hand(0.0F, -0.25F, 0.15F, 20, 0, 0)
            .key(0.85F).hold()
            .shake(0.2F, 0.2F));
        add(PoseClip.clip("slam", 14)
            .key(0.3F).head(-10, 0, 0).body(-10, 0, 0).arms(-165, 0, 10).hand(0.0F, 0.45F, 0.0F, -60, 0, 0)
            .key(0.5F).head(15, 0, 0).body(35, 0, 0).arms(-50, 0, 8).rightLeg(-25, 0, 0).leftLeg(15, 0, 0).hand(0.0F, -0.35F, -0.2F, 60, 0, 0)
            .key(0.8F).hold()
            .shake(0.7F, 0.5F));
        add(PoseClip.clip("dash", 10)
            .key(0.2F).head(-20, 0, 0).body(30, 0, 0).arms(45, 0, 15).rightLeg(-35, 0, 0).leftLeg(30, 0, 0).hand(0.0F, -0.1F, 0.25F, 10, 0, 0)
            .key(0.7F).hold());
        add(PoseClip.clip("guard", 16)
            .key(0.25F).body(5, 0, 0).rightArm(-90, -35, 0).leftArm(-90, 35, 0).hand(-0.15F, 0.05F, -0.15F, 0, -25, 0)
            .key(0.8F).hold());
        add(PoseClip.clip("channel", 20)
            .key(0.2F).arms(-80, 10, 0).hand(0.0F, 0.05F, -0.2F, -10, 0, 0)
            .key(0.5F).arms(-88, 16, 0).hand(0.0F, 0.08F, -0.25F, -12, 0, 0)
            .key(0.8F).arms(-80, 10, 0).hand(0.0F, 0.05F, -0.2F, -10, 0, 0));
        add(PoseClip.clip("channel_self", 16)
            .key(0.25F).head(10, 0, 0).body(5, 0, 0).arms(-55, -45, 0).hand(-0.15F, -0.05F, 0.0F, 0, -30, 0)
            .key(0.75F).hold());
        add(PoseClip.clip("write", 18)
            .key(0.15F).rightArm(-80, -25, 0).hand(-0.15F, 0.1F, -0.25F, -10, 0, 0)
            .key(0.4F).rightArm(-85, 25, 0).hand(0.15F, 0.12F, -0.25F, -10, 0, 0)
            .key(0.6F).rightArm(-75, -10, 0).hand(-0.05F, 0.05F, -0.25F, -10, 0, 0)
            .key(0.8F).rightArm(-85, 20, 0).hand(0.12F, 0.1F, -0.25F, -10, 0, 0));
        add(PoseClip.clip("point", 12)
            .key(0.25F).head(5, 0, 0).body(0, -10, 0).rightArm(-90, 0, 0).hand(0.0F, 0.1F, -0.35F, -10, 0, 0)
            .key(0.8F).hold());
        add(PoseClip.clip("flap", 14)
            .key(0.25F).arms(-10, 0, 110).hand(0.1F, 0.3F, 0.0F, 0, 0, -30)
            .key(0.5F).body(15, 0, 0).arms(10, 0, 25).hand(0.0F, -0.2F, 0.0F, 0, 0, 10)
            .shake(0.3F, 0.5F));
        add(PoseClip.clip("cloak", 12)
            .key(0.3F).head(15, 0, 0).body(10, 0, 0).rightArm(-110, -50, 0).hand(-0.2F, 0.15F, -0.1F, -20, -30, 0)
            .key(0.75F).hold());
        // A legion soldier climbing out of the ground, then raising its weapon to its master.
        add(PoseClip.clip("rise", 26)
            .key(0.0F).head(-25, 0, 0).arms(-170, 0, 12)
            .key(0.5F).hold()
            .key(0.75F).head(-5, 0, 0).rightArm(-130, 0, 0).leftArm(10, 0, -5));
        // The Transcendent Form: the soul gathers (the pose holds until the burst takes over), then breaks free.
        add(PoseClip.clip("transform_gather", 30)
            .key(0.4F).head(-20, 0, 0).body(-5, 0, 0).arms(-40, 0, 60).rightLeg(5, 0, 5).leftLeg(5, 0, -5).hand(0.0F, 0.1F, 0.1F, -20, 0, 0)
            .key(1.0F).head(-40, 0, 0).body(-12, 0, 0).arms(-75, 0, 105).rightLeg(5, 0, 3).leftLeg(5, 0, -3).hand(0.0F, 0.35F, 0.1F, -40, 0, 0));
        add(PoseClip.clip("transform_burst", 16)
            .key(0.0F).head(-40, 0, 0).body(-12, 0, 0).arms(-75, 0, 105).hand(0.0F, 0.35F, 0.1F, -40, 0, 0)
            .key(0.15F).head(-5, 0, 0).body(-20, 0, 0).arms(-15, 0, 125).rightLeg(0, 0, 12).leftLeg(0, 0, -12).hand(0.1F, 0.2F, 0.2F, 0, 0, 0)
            .key(0.6F).hold()
            .shake(1.0F, 0.1F));
    }

    private PoseClips() {
    }

    private static void add(PoseClip.Builder builder) {
        PoseClip clip = builder.build();
        CLIPS.put(clip.id(), clip);
    }

    @Nullable
    public static PoseClip get(String id) {
        return CLIPS.get(id);
    }
}
