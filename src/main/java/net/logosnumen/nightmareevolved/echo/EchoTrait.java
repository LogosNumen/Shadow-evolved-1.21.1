package net.logosnumen.nightmareevolved.echo;

import java.util.Locale;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The Attributes the Spell finds in an Echo, as its runes show them: [Strong] and [Armored] on a Carapace Scavenger,
 * [Battle Master] and [Stalwart] on the Stone Saint, and so on; and the three that belong only to Echoes and Shadows.
 */
public enum EchoTrait {
    STRONG,
    ARMORED,
    SWIFT,
    BATTLE_MASTER,
    STALWART,
    RELENTLESS,
    VENOMOUS,
    KEEN_SENSES,
    SHADOW_SWORN,
    DARK_DESTRIER,
    SOUL_WEAPON,
    SOUL_BEAST;

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("echo_trait.nightmare_evolved." + this.id());
    }

    public Component description() {
        return Component.translatable("echo_trait.nightmare_evolved." + this.id() + ".desc");
    }

    @Nullable
    public static EchoTrait byId(String id) {
        for (EchoTrait trait : values()) {
            if (trait.id().equals(id)) {
                return trait;
            }
        }

        return null;
    }

    /** What the trait does to the Echo's body as it is summoned. */
    public void apply(Mob mob, ServerPlayer owner) {
        switch (this) {
            case STRONG -> add(mob, Attributes.ATTACK_DAMAGE, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case ARMORED -> add(mob, Attributes.ARMOR, 6.0, AttributeModifier.Operation.ADD_VALUE);
            case SWIFT -> add(mob, Attributes.MOVEMENT_SPEED, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            case BATTLE_MASTER -> {
                add(mob, Attributes.ATTACK_DAMAGE, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                add(mob, Attributes.ATTACK_KNOCKBACK, 0.5, AttributeModifier.Operation.ADD_VALUE);
            }
            case KEEN_SENSES -> add(mob, Attributes.FOLLOW_RANGE, 16.0, AttributeModifier.Operation.ADD_VALUE);
            case SOUL_WEAPON -> add(mob, Attributes.ATTACK_DAMAGE, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case SOUL_BEAST -> {
                double grown = 0.1 * Math.max(0, net.logosnumen.nightmareevolved.soul.Soul.of(owner).rank().ladder);
                add(mob, Attributes.MAX_HEALTH, grown, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                add(mob, Attributes.ATTACK_DAMAGE, grown, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
            default -> {
            }
        }
    }

    /** A blow the Echo lands; returns what it should do. */
    public float dealt(Mob echo, LivingEntity target, DamageSource source, float amount) {
        return switch (this) {
            case VENOMOUS -> {
                Kit.quiet(target, MobEffects.POISON, 80, 0);
                yield amount;
            }
            case SHADOW_SWORN, DARK_DESTRIER -> dark(echo) ? amount * 1.3F : amount;
            default -> amount;
        };
    }

    /** A blow the Echo takes; returns what it should do. */
    public float taken(Mob echo, DamageSource source, float amount) {
        return switch (this) {
            case STALWART -> amount * 0.75F;
            case DARK_DESTRIER -> dark(echo) ? amount * 0.85F : amount;
            default -> amount;
        };
    }

    /** Once a second while the Echo is out. */
    public void tick(Mob echo) {
        switch (this) {
            case RELENTLESS -> {
                if (echo.getTarget() == null && echo.getHealth() < echo.getMaxHealth()) {
                    echo.heal(1.0F);
                }
            }
            case STALWART -> {
                echo.removeEffect(MobEffects.CONFUSION);
                echo.removeEffect(MobEffects.WEAKNESS);
            }
            case DARK_DESTRIER -> {
                if (dark(echo)) {
                    Kit.quiet(echo, MobEffects.MOVEMENT_SPEED, 30, 1);
                }
            }
            default -> {
            }
        }
    }

    private static boolean dark(Mob echo) {
        return echo.level() instanceof ServerLevel level && Kit.shadowed(level, BlockPos.containing(echo.getEyePosition()));
    }

    private void add(Mob mob, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(NightmareEvolved.id("echo." + this.id()), amount, operation));
        }
    }
}
