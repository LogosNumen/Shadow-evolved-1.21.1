package net.logosnumen.nightmareevolved.flaw;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soul.SoulHooks;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Every Flaw the novel gives an Awakened, each with the Aspect it belongs to. Where the novel names a Flaw, it keeps
 * that name; where it only says whose Flaw it is, the mod gives it a plain one. Each does here what it does there, as
 * far as the game allows: Sunny cannot leave a question unanswered, Nephis burns when she uses her power, Cassie is
 * blind, Rain cannot kill.
 */
public enum Flaw implements SoulHooks {
    /** Sunny: cannot lie, and must answer any question put to him. */
    CLEAR_CONSCIENCE {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            long owed = soul.flawState().getLong("Owed");
            if (owed > 0 && age % 100 == 0) {
                int strain = soul.flawState().getInt("Strain") + 1;
                soul.flawState().putInt("Strain", strain);
                FlawKit.pain(player, Math.min(8.0F, strain));
                Kit.quiet(player, MobEffects.CONFUSION, 120, 0);
                FlawKit.say(player, "clear_conscience");
            }
        }
    },
    /** Nephis: must suffer to use her power; every use burns her as if alive in a fire. */
    PRISTINE_SOUL {
        @Override
        public void used(ServerPlayer player, SoulData soul, Ability ability) {
            FlawKit.pain(player, (float)Math.max(0.5, ability.cost * (ability.held() ? 0.25 : 0.08)));
            Vfx.embers(player.serverLevel(), player.getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 6, 0.5);
        }
    },
    /** Rain: cannot kill. Her blows stop a breath short, and what she fells only collapses. */
    CROWN_OF_THORNS {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            float spare = target.getHealth() + target.getAbsorptionAmount() - 1.0F;
            if (amount >= spare) {
                Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
                Kit.quiet(target, MobEffects.WEAKNESS, 100, 2);
                return Math.max(0.0F, spare);
            }

            return amount;
        }
    },
    /** Cassie: blind since her First Nightmare. Her Aspect becomes her eyes as it grows. */
    BLINDNESS {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 == 0) {
                Kit.quiet(player, MobEffects.BLINDNESS, 60, 0);
            }
        }
    },
    /** Kai: knows every lie told near him, and the ugliness behind it. Deceivers nearby sicken him. */
    EAR_FOR_LIES {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 40 != 0) {
                return;
            }

            List<LivingEntity> liars = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0),
                e -> e != player && (e.isInvisible() || "illusion".equals(Bound.kind(e)) || "mimic".equals(Bound.kind(e))));
            if (!liars.isEmpty()) {
                liars.forEach(e -> Kit.quiet(e, MobEffects.GLOWING, 60, 0));
                Kit.quiet(player, MobEffects.CONFUSION, 100, 0);
                Kit.quiet(player, MobEffects.WEAKNESS, 60, 0);
            }
        }
    },
    /** Effie: a body that needs far more than others; she eats like a pack of wolves. */
    RAVENOUS_BODY {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 == 0) {
                player.causeFoodExhaustion(0.15F);
                if (player.getFoodData().getFoodLevel() < 6) {
                    Kit.quiet(player, MobEffects.WEAKNESS, 40, 0);
                }
            }
        }
    },
    /** Jet: her core is broken and her soul is dead. It makes no essence and leaks what it holds; only killing feeds it. */
    DEATHLY_CORE {
        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return 0.0;
        }

        @Override
        public double shards(ServerPlayer player, SoulData soul) {
            return 0.0;
        }

        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 != 0) {
                return;
            }

            soul.setEssence(soul.essence() - soul.maxEssence() * 0.004);
            if (soul.essence() < 0.5) {
                Kit.quiet(player, MobEffects.MOVEMENT_SLOWDOWN, 40, 1);
                Kit.quiet(player, MobEffects.WEAKNESS, 40, 1);
                Kit.quiet(player, MobEffects.HUNGER, 40, 0);
                if (age % 200 == 0) {
                    FlawKit.say(player, "deathly_core");
                }
            }
        }
    },
    /** Mordret: his self broke into seven pieces. Now and then another piece holds one of his powers. */
    SEVENFOLD_SELF {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            long now = player.level().getGameTime();
            if (now < soul.flawState().getLong("ShiftAt")) {
                return;
            }

            List<String> held = Soul.unlockedAbilityIds(player);
            soul.flawState().putString("Held", held.isEmpty() ? "" : held.get(player.getRandom().nextInt(held.size())));
            soul.flawState().putLong("ShiftAt", now + 6000);
            soul.changed();
        }

        @Nullable
        @Override
        public Component forbids(ServerPlayer player, SoulData soul, Ability ability) {
            return ability.id.equals(soul.flawState().getString("Held")) ? Component.translatable("flaw.nightmare_evolved.notice.sevenfold_self") : null;
        }
    },
    /** Morgan: her touch makes things fragile. What she holds wears out far faster. */
    BRITTLE_TOUCH {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            ItemStack held = player.getMainHandItem();
            if (held.isDamageableItem() && source.getDirectEntity() == player) {
                held.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
            }

            return amount;
        }
    },
    /** Anvil: must lose whatever he cherishes. Things he has named wear away while he carries them. */
    FORSAKEN_TREASURES {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 1200 != 0) {
                return;
            }

            for (ItemStack stack : player.getInventory().items) {
                if (stack.has(DataComponents.CUSTOM_NAME) && stack.isDamageableItem()) {
                    stack.hurtAndBreak(Math.max(1, stack.getMaxDamage() / 100), player.serverLevel(), player, item -> FlawKit.say(player, "forsaken_treasures"));
                }
            }
        }
    },
    /** Ki Song: her love for her daughters. When one who serves her falls, her grief costs her. */
    MOTHERS_LOVE,
    /** Caster: every use of his power shortens his life. Each takes a little of his health, coming back only slowly. */
    BORROWED_TIME {
        @Override
        public void used(ServerPlayer player, SoulData soul, Ability ability) {
            if (!ability.held() || player.tickCount % 100 == 0) {
                float lost = Math.min(10.0F, soul.flawState().getFloat("Lost") + 0.5F);
                soul.flawState().putFloat("Lost", lost);
                soul.flawState().putLong("RestoreAt", player.level().getGameTime() + 12000);
                Soul.applyStats(player);
            }
        }

        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            float lost = soul.flawState().getFloat("Lost");
            if (lost > 0.0F && age % 200 == 0 && player.level().getGameTime() >= soul.flawState().getLong("RestoreAt")) {
                soul.flawState().putFloat("Lost", Math.max(0.0F, lost - 0.5F));
                soul.flawState().putLong("RestoreAt", player.level().getGameTime() + 12000);
                Soul.applyStats(player);
            }
        }

        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            out.add(Attributes.MAX_HEALTH, -soul.flawState().getFloat("Lost"), AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** Dorn: his body never stops growing, and one day will be too heavy to bear. */
    ENDLESS_GROWTH {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 1200 == 0) {
                float growth = Math.min(0.5F, soul.flawState().getFloat("Growth") + 0.002F);
                soul.flawState().putFloat("Growth", growth);
                if ((int)(growth * 100) != (int)((growth - 0.002F) * 100)) {
                    Soul.applyStats(player);
                }
            }
        }

        @Override
        public void modifiers(ServerPlayer player, SoulData soul, Ability.Modifiers out) {
            float growth = soul.flawState().getFloat("Growth");
            out.add(Attributes.SCALE, growth, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            out.add(Attributes.MAX_HEALTH, growth * 8.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.MOVEMENT_SPEED, -growth * 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            out.add(Attributes.FALL_DAMAGE_MULTIPLIER, growth, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    },
    /** Jest: cannot use anything enchanted, so he carries no Memories at all, only a plain cane and his hands. */
    BARE_HANDS {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 != 0) {
                return;
            }

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.isEnchanted()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }

                    FlawKit.say(player, "bare_hands");
                }
            }
        }
    },
    /** Quentin: can never break a promise. A creature he has sworn peace to, he cannot harm. */
    BINDING_WORD {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            if (FlawEvents.sworn(soul, target)) {
                FlawKit.say(player, "binding_word");
                return 0.0F;
            }

            return amount;
        }
    },
    /** Belle: poor at holding back. Struck, he throws himself at whatever struck him. */
    RASH_HEART {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            long now = player.level().getGameTime();
            if (source.getEntity() instanceof LivingEntity attacker && attacker != player && now >= soul.flawState().getLong("Next")) {
                soul.flawState().putLong("Next", now + 60);
                Vec3 to = attacker.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
                if (to.lengthSqr() > 1.0E-4) {
                    Vec3 dir = to.normalize();
                    player.setDeltaMovement(dir.x * 0.7, 0.2, dir.z * 0.7);
                    player.hurtMarked = true;
                }

                player.getCooldowns().addCooldown(Items.SHIELD, 40);
                Kit.quiet(player, MobEffects.DAMAGE_BOOST, 40, 0);
            }

            return amount;
        }
    },
    /** Helie: her Aspect answers only while her emotions are in check. Hurt or near death, it falls silent. */
    STILL_WATERS {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            if (amount > 0.0F) {
                soul.flawState().putLong("Calm", player.level().getGameTime() + 60);
            }

            return amount;
        }

        @Nullable
        @Override
        public Component forbids(ServerPlayer player, SoulData soul, Ability ability) {
            boolean shaken = player.level().getGameTime() < soul.flawState().getLong("Calm") || player.getHealth() < player.getMaxHealth() * 0.3F;
            return shaken ? Component.translatable("flaw.nightmare_evolved.notice.still_waters") : null;
        }
    },
    /** Kim: naturally unassuming. Her name goes unseen, and creatures notice her late. */
    OVERLOOKED {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 100 == 0) {
                FlawEvents.hideName(player, true);
            }
        }
    },
    /** Lucky Sleeper: everyone forgets his name, however often he tells them. */
    FORGOTTEN_NAME,
    /** Luster: the novel says only that it is plain to see. Here, his essence ebbs when no one is near to share it with. */
    UNSPOKEN {
        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            boolean company = !player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24.0),
                e -> e != player && (e instanceof Player || Bound.servesOrAllied(e, player))).isEmpty();
            return company ? 1.0 : 0.5;
        }
    },
    /** Samara: her soul refills its essence slowly. */
    SLOW_WELL {
        @Override
        public double regen(ServerPlayer player, SoulData soul) {
            return 0.35;
        }
    },
    /** Tristan: hot-blooded. Each blow he takes stokes his temper, until he charges in heedless of guard. */
    HOT_BLOOD {
        @Override
        public float taken(ServerPlayer player, SoulData soul, DamageSource source, float amount) {
            long now = player.level().getGameTime();
            if (now < soul.flawState().getLong("Exposed")) {
                return amount * 1.3F;
            }

            if (source.getEntity() instanceof LivingEntity && amount > 0.0F) {
                int temper = soul.flawState().getInt("Temper") + 1;
                if (temper >= 5) {
                    temper = 0;
                    soul.flawState().putLong("Exposed", now + 80);
                    Kit.quiet(player, MobEffects.DAMAGE_BOOST, 80, 1);
                    Kit.quiet(player, MobEffects.MOVEMENT_SPEED, 80, 0);
                    FlawKit.say(player, "hot_blood");
                }

                soul.flawState().putInt("Temper", temper);
            }

            return amount;
        }
    },
    /** Revel: cannot step into sunlight. Under the open daytime sky she burns. */
    LIGHTLESS {
        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 == 0 && FlawKit.inSunlight(player)) {
                player.igniteForSeconds(3.0F);
                Kit.quiet(player, MobEffects.WEAKNESS, 40, 1);
            }
        }
    },
    /** Seishan: a craving for blood. She grows weak without it, and the taste of it drives her on. */
    BLOODTHIRST {
        @Override
        public float dealt(ServerPlayer player, SoulData soul, LivingEntity target, DamageSource source, float amount) {
            soul.flawState().putLong("LastBlood", player.level().getGameTime());
            return amount;
        }

        @Override
        public void killed(ServerPlayer player, SoulData soul, LivingEntity victim) {
            Kit.quiet(player, MobEffects.DAMAGE_BOOST, 100, 0);
            player.getFoodData().eat(2, 0.3F);
        }

        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 100 == 0 && player.level().getGameTime() - soul.flawState().getLong("LastBlood") > 6000) {
                Kit.quiet(player, MobEffects.WEAKNESS, 120, 0);
                Kit.quiet(player, MobEffects.HUNGER, 120, 0);
            }
        }
    },
    /** Naeve: his Aspect belongs to the water. On dry land it costs twice as much and he moves heavily. */
    LANDBOUND {
        @Override
        public double cost(ServerPlayer player, SoulData soul, Ability ability) {
            return player.isInWaterRainOrBubble() ? 1.0 : 2.0;
        }

        @Override
        public void tick(ServerPlayer player, SoulData soul, int age) {
            if (age % 20 == 0 && !player.isInWaterRainOrBubble()) {
                Kit.quiet(player, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
            }
        }
    };

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("flaw.nightmare_evolved." + this.id());
    }

    public Component description() {
        return Component.translatable("flaw.nightmare_evolved." + this.id() + ".desc");
    }

    /** Whether the novel itself gives this Flaw its name. */
    public boolean namedInNovel() {
        return this == CLEAR_CONSCIENCE || this == PRISTINE_SOUL || this == CROWN_OF_THORNS;
    }

    @Nullable
    public static Flaw byId(String id) {
        for (Flaw flaw : values()) {
            if (flaw.id().equals(id)) {
                return flaw;
            }
        }

        return null;
    }
}
