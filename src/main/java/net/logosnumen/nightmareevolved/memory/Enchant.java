package net.logosnumen.nightmareevolved.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.aspect.Ability;
import net.logosnumen.nightmareevolved.aspect.Afflictions;
import net.logosnumen.nightmareevolved.aspect.Kit;
import net.logosnumen.nightmareevolved.aspect.Targets;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.entity.memory.ThrownMemoryEntity;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * The enchantments woven into Memories. The named ones are the novel's own, on the Memories that carry them in the
 * novel; the rest are the commoner enchantments the Spell gives the Memories it weaves for anonymous prey. Only the
 * weakest work on their own; most draw on their owner's essence, as the novel has it, and say so.
 *
 * <p>Every enchantment is optional in what it does: an enchantment overrides only the moments it changes. They act
 * through {@link Worn}, which says which Memory, whose, and how it is carried.
 */
public enum Enchant {
    // Common weapon enchantments.
    KEEN(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.struck(source) ? amount * (float)(1.12 + 0.04 * w.power()) : amount;
        }
    },
    HUNGERING(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.struck(source)) {
                w.player().heal(amount * 0.12F);
            }

            return amount;
        }
    },
    VENOMOUS(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.struck(source)) {
                Kit.quiet(target, MobEffects.POISON, 80, w.power() > 2.0 ? 1 : 0);
            }

            return amount;
        }
    },
    SEARING(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.struck(source)) {
                target.igniteForSeconds((float)(3.0 + w.power()));
            }

            return amount;
        }
    },
    FROSTBITTEN(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.struck(source)) {
                Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + 60));
            }

            return amount;
        }
    },
    RENDING(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.struck(source)) {
                Afflictions.afflict(target, w.player(), Afflictions.BLEED, 0.5 * w.power(), 4);
            }

            return amount;
        }
    },
    THUNDERING(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.struck(source) && w.player().getRandom().nextFloat() < 0.25F) {
                chain(w, target, amount * 0.4F, 2, 5.0);
            }

            return amount;
        }
    },
    WEIGHTY(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.melee(source)) {
                Kit.shove(target, w.player().position(), 0.6, 0.15);
                Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 20, 1);
            }

            return amount;
        }
    },
    GHOST_EDGE(Pool.WEAPON) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.struck(source) && incorporeal(target) ? amount * 1.5F : amount;
        }
    },

    // Common armour enchantments.
    STURDY(Pool.ARMOR) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            out.add(Attributes.ARMOR, 1.0 + w.power(), AttributeModifier.Operation.ADD_VALUE);
        }
    },
    WARDING(Pool.ARMOR) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return soulAttack(source) ? amount * 0.8F : amount;
        }
    },
    STEADFAST(Pool.ARMOR) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            out.add(Attributes.KNOCKBACK_RESISTANCE, 0.25, AttributeModifier.Operation.ADD_VALUE);
        }
    },
    FLAMEPROOF(Pool.ARMOR) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FIRE) ? amount * 0.65F : amount;
        }
    },
    LIGHTFOOT(Pool.ARMOR) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FALL) ? amount * 0.5F : amount;
        }
    },

    // Common charm enchantments.
    VIGOR(Pool.CHARM) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            out.add(Attributes.MAX_HEALTH, 2.0 * w.power(), AttributeModifier.Operation.ADD_VALUE);
        }
    },
    WELLSPRING(Pool.CHARM) {
        @Override
        public double regen(Worn w) {
            return 1.2 + 0.03 * w.power();
        }
    },
    CLEAR_MIND(Pool.CHARM) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0) {
                cleanse(w.player(), MobEffects.CONFUSION, MobEffects.DARKNESS);
            }
        }
    },
    NIGHT_EYES(Pool.CHARM) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 40 == 0) {
                Kit.quiet(w.player(), MobEffects.NIGHT_VISION, 260, 0);
            }
        }
    },
    FLEET(Pool.CHARM) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            out.add(Attributes.MOVEMENT_SPEED, 0.04 + 0.01 * w.power(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    },
    FORTUNE(Pool.CHARM) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            out.add(Attributes.LUCK, 1.0, AttributeModifier.Operation.ADD_VALUE);
        }
    },

    // Common garment enchantments.
    SHROUDED(Pool.GARMENT) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 10 == 0 && w.player().isShiftKeyDown() && w.player().getDeltaMovement().horizontalDistanceSqr() < 1.0E-4) {
                Kit.quiet(w.player(), MobEffects.INVISIBILITY, 20, 0);
            }
        }
    },
    WEATHERWARD(Pool.GARMENT) {
        @Override
        public void tick(Worn w, int age) {
            w.player().setTicksFrozen(0);
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypes.LIGHTNING_BOLT) ? amount * 0.5F : amount;
        }
    },
    SOFTSTEP(Pool.GARMENT) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FALL) ? Math.max(0.0F, amount - 4.0F) * 0.6F : amount;
        }
    },

    // Enchantments any Memory may carry.
    DURABLE(Pool.ANY) {
        @Override
        public double durability() {
            return 2.5;
        }
    },
    SWIFT_SUMMON(Pool.ANY) {
        @Override
        public boolean instant() {
            return true;
        }
    },
    TETHERED(Pool.ANY) {
        @Override
        public boolean tethered() {
            return true;
        }
    },
    MENDING_WEAVE(Pool.ANY) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 40 == 0 && w.stack().isDamaged()) {
                w.stack().setDamageValue(w.stack().getDamageValue() - 1);
            }
        }
    },

    // The tools the Spell weaves for anonymous prey each do one thing.
    WAYFINDER(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (w.held() && age % 20 == 0) {
                Kit.quiet(w.player(), MobEffects.NIGHT_VISION, 260, 0);
                for (LivingEntity foe : near(w, 12.0 + 2.0 * w.power())) {
                    if (foe instanceof Enemy) {
                        Kit.quiet(foe, MobEffects.GLOWING, 30, 0);
                    }
                }
            }
        }
    },
    SPRING_WATER(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (!ready(w, "Drink", 600)) {
                return false;
            }

            drink(w, 4, 0.4F);
            return true;
        }
    },
    RALLYING_CALL(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (!ready(w, "Call", 900) || !pay(w, 6.0)) {
                return false;
            }

            for (LivingEntity ally : Targets.alliesNear(w.level(), w.player(), w.player().position(), 24.0)) {
                Kit.buff(ally, MobEffects.DAMAGE_BOOST, 12, 0);
                Kit.buff(ally, MobEffects.MOVEMENT_SPEED, 12, 0);
            }

            Kit.sound(w.level(), w.player().position(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0), 3.0F, 0.9F);
            Vfx.ring(w.level(), w.player().position().add(0.0, 0.2, 0.0), 8.0, Vfx.Palette.EARTH, 40);
            return true;
        }
    },

    // The Shard Memories of the Forgotten Shore.
    /** Midnight Shard: near unbreakable, and fiercest when its wielder is close to death and refuses to yield. */
    UNBROKEN(Pool.NAMED) {
        @Override
        public double durability() {
            return 12.0;
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            ServerPlayer p = w.player();
            if (w.melee(source) && p.getHealth() < p.getMaxHealth() * 0.35F) {
                Kit.quiet(p, MobEffects.DAMAGE_RESISTANCE, 40, 0);
                Vfx.embers(w.level(), Vfx.chest(p), Vfx.Palette.STEEL, 3, 0.4);
                return amount * (float)(1.4 + 0.1 * w.power());
            }

            return amount;
        }
    },
    /** Zenith Shard: the falling star's spear strikes hardest from above. */
    ZENITH(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.melee(source) && w.player().fallDistance > 1.0F) {
                Vfx.pillar(w.level(), target.position(), 2.0, Vfx.Palette.LIGHT);
                return amount * (float)(1.4 + 0.05 * Math.min(10.0F, w.player().fallDistance));
            }

            return amount;
        }
    },
    /** Dawn Shard (and the Crown of Dawn): every Memory nearby grows stronger, the weaker ones by nearly a whole Rank. */
    DAWN(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 40 == 0) {
                Vfx.halo(w.level(), w.player().getEyePosition().add(0.0, 0.45, 0.0), 0.35, Vfx.Palette.LIGHT.bright, 40);
            }
        }
    },
    /** Dusk Shard: its weight is its master's to choose; light to carry, immovable to strike or to stop. */
    INDOMITABLE(Pool.NAMED) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            if (w.held()) {
                out.add(Attributes.KNOCKBACK_RESISTANCE, 0.5, AttributeModifier.Operation.ADD_VALUE);
            }
        }

        @Override
        public float blocked(Worn w, DamageSource source, float amount) {
            if (source.getDirectEntity() instanceof LivingEntity attacker && attacker != w.player()) {
                Kit.shove(attacker, w.player().position(), 1.2, 0.3);
                Kit.quiet(attacker, MobEffects.MOVEMENT_SLOWDOWN, 30, 2);
                Kit.sound(w.level(), attacker.position(), SoundEvents.ANVIL_LAND, 0.6F, 0.6F);
            }

            return amount;
        }
    },
    /** Moonlight Shard: appears in its master's hand at once, and finds the throat of whoever isn't watching for it. */
    UNSEEN(Pool.NAMED) {
        @Override
        public boolean instant() {
            return true;
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.melee(source) && !(target instanceof Mob mob && mob.getTarget() == w.player()) ? amount * 1.6F : amount;
        }
    },
    /** Sunlight Shard: the war hammer's beak strikes like noon, burning what hides from the sun. */
    SUNLIGHT(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (!w.melee(source)) {
                return amount;
            }

            Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 30, 3);
            if (target.getType().is(EntityTypeTags.UNDEAD)) {
                target.igniteForSeconds(4.0F);
                Vfx.flash(w.level(), target.getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 1.2F);
                return amount * 1.5F;
            }

            return amount;
        }
    },
    /** Starlight Shard: under an open night sky the white cloak keeps its wearer whole and quick. */
    STARLIGHT(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            ServerPlayer p = w.player();
            if (age % 40 == 0 && w.level().isNight() && w.level().canSeeSky(p.blockPosition())) {
                Kit.quiet(p, MobEffects.REGENERATION, 60, 0);
                Kit.quiet(p, MobEffects.MOVEMENT_SPEED, 60, 0);
                Vfx.mote(w.level(), p.position().add(0.0, 1.6, 0.0), 0xE6EEFF, 0.02);
            }
        }
    },

    // Named Memories of the novel, by Rank.
    /**
     * Dark Wing: its wearer drifts down softly from any height, can hang a little above the ground (crouch in the air)
     * and skims over water.
     */
    GLIDE(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            ServerPlayer p = w.player();
            Vec3 motion = p.getDeltaMovement();
            if (!p.onGround() && motion.y < -0.08 && !p.isFallFlying() && !p.getAbilities().flying) {
                p.setDeltaMovement(motion.x * 1.02, p.isShiftKeyDown() ? 0.0 : Math.max(motion.y, -0.12), motion.z * 1.02);
                p.hurtMarked = true;
                p.fallDistance = 0.0F;
                if (age % 4 == 0) {
                    Vfx.mote(w.level(), p.position().add(0.0, 1.2, 0.0), 0xCFE8F4, -0.02);
                }
            } else if (p.isInWater() && !p.isShiftKeyDown() && motion.y < 0.05) {
                p.setDeltaMovement(motion.x, 0.08, motion.z);
                p.hurtMarked = true;
            }
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FALL) ? 0.0F : amount;
        }
    },
    /** Dream Blade: a measure of protection against attacks on the soul. */
    DREAMERS_WARD(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return soulAttack(source) ? amount * 0.75F : amount;
        }
    },
    /** Endless Spring: water without end, enough to make a desert bloom. Drink it, or pour it out. */
    GIFT_OF_WATER(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (!spendCharge(w, "Water", 8, 600)) {
                return false;
            }

            drink(w, 3, 0.6F);
            return true;
        }

        @Override
        public boolean useOn(Worn w, BlockPos pos, Direction face) {
            BlockPos at = pos.relative(face);
            if (w.level().dimensionType().ultraWarm() || !w.level().getBlockState(at).canBeReplaced(Fluids.WATER)
                || !spendCharge(w, "Water", 8, 600)) {
                return false;
            }

            w.level().setBlock(at, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
            Kit.sound(w.level(), Vec3.atCenterOf(at), SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
            return true;
        }
    },
    /** Puppeteer's Shroud, Shroud of Graceless Dusk: weariness of the body takes longer to set in and passes sooner. */
    BLESSING_OF_FLESH(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0) {
                ServerPlayer p = w.player();
                p.getFoodData().setExhaustion(Math.max(0.0F, p.getFoodData().getExhaustionLevel() - 0.4F));
                cleanse(p, MobEffects.HUNGER, MobEffects.WEAKNESS);
            }
        }
    },
    /** Puppeteer's Shroud, Shroud of Graceless Dusk: weariness of the mind takes longer to set in and passes sooner. */
    BLESSING_OF_SPIRIT(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0) {
                cleanse(w.player(), MobEffects.DIG_SLOWDOWN);
            }
        }

        @Override
        public double regen(Worn w) {
            return 1.1;
        }
    },
    ENHANCED_DURABILITY(Pool.NAMED) {
        @Override
        public double durability() {
            return 3.0;
        }
    },
    /** Puppeteer's Shroud: a little protection against what would get into the mind. */
    DOUBTLESS(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0) {
                cleanse(w.player(), MobEffects.CONFUSION);
            }
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return soulAttack(source) ? amount * 0.9F : amount;
        }
    },
    /** Silver Bell: rung, it is heard across a vast distance. */
    SILVER_SONG(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (!ready(w, "Ring", 60)) {
                return false;
            }

            ServerPlayer p = w.player();
            boolean soft = p.isShiftKeyDown() && w.data().has(SONOROUS);
            double reach = soft ? 24.0 : 1024.0;
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, soft ? 0.6F : 2.0F, 1.6F);
            for (ServerPlayer other : p.serverLevel().players()) {
                if (other != p && other.distanceToSqr(p) < reach * reach) {
                    other.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.6F);
                    other.displayClientMessage(Component.translatable("memory.nightmare_evolved.silver_bell.heard", p.getDisplayName(),
                        compass(other.position(), p.position()), Math.round(other.distanceTo(p))).withStyle(ChatFormatting.GRAY), true);
                }
            }

            Vfx.halo(w.level(), p.getEyePosition(), 1.2, 0xE6EEFF, 20);
            return true;
        }
    },
    /** Silver Bell: its owner decides how loud it rings (crouch to ring it softly). */
    SONOROUS(Pool.NAMED),
    /** Blood Blossom: every Memory, Echo and Shadow of its wearer thirsts for blood, and is the stronger for it. */
    FLOWER_OF_EVIL(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            ItemStack main = w.player().getMainHandItem();
            boolean memory = source.getDirectEntity() == w.player() && SoulMemories.data(main) != null || SoulMemories.firedByMemory(source);
            if (memory) {
                w.player().heal(amount * 0.05F);
                return amount * 1.15F;
            }

            return amount;
        }
    },
    /** Broken Oath: it wears away the souls of everyone who comes near its wearer. */
    OATHBREAKER(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 != 0) {
                return;
            }

            for (LivingEntity other : w.level().getEntitiesOfClass(LivingEntity.class, w.player().getBoundingBox().inflate(5.0),
                e -> e != w.player() && e.isAlive() && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
                Targets.soulHit(w.level(), w.player(), other, (float)(0.5 * w.power()));
                Vfx.mote(w.level(), other.getEyePosition(), 0x3A2A4A, 0.03);
            }
        }
    },
    /** Prowling Thorn: thrown, it flies on an unseen string and comes back to the hand; now and then the string fails. */
    ROSE_OF_BETRAYAL(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            ThrownMemoryEntity.launch(w, hand, 1.8F, ThrownMemoryEntity.Return.HAND);
            return true;
        }

        @Override
        public void thrownHit(Worn w, @Nullable LivingEntity target, Vec3 at) {
            if (target != null) {
                Targets.hit(w.level(), w.player(), target, (float)(4.0 + 2.0 * w.power()));
                Afflictions.afflict(target, w.player(), Afflictions.BLEED, 0.5 * w.power(), 3);
            }
        }
    },
    /** Extraordinary Rock: thrown, it lands far harder than any rock should, then returns to its owner's soul. */
    NOT_REALLY(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            ThrownMemoryEntity.launch(w, hand, 1.5F, ThrownMemoryEntity.Return.SOUL);
            return true;
        }

        @Override
        public void thrownHit(Worn w, @Nullable LivingEntity target, Vec3 at) {
            if (target != null) {
                Targets.hit(w.level(), w.player(), target, (float)(6.0 + 3.0 * w.power()));
                Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 30, 6);
                Kit.shove(target, w.player().position(), 0.8, 0.3);
            }

            Kit.sound(w.level(), at, SoundEvents.STONE_BREAK, 1.2F, 0.6F);
        }
    },
    /** Heavenly Burden: whatever this needle pierces rises into the sky. */
    UNBURDENED(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (w.player().isShiftKeyDown()) {
                Kit.quiet(w.player(), MobEffects.LEVITATION, 40, 1);
                return true;
            }

            ThrownMemoryEntity.launch(w, hand, 2.2F, ThrownMemoryEntity.Return.SOUL);
            return true;
        }

        @Override
        public void thrownHit(Worn w, @Nullable LivingEntity target, Vec3 at) {
            if (target != null) {
                Targets.hit(w.level(), w.player(), target, 2.0F);
                Kit.quiet(target, MobEffects.LEVITATION, (int)(60 + 20 * w.power()), 2);
            }
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.melee(source)) {
                Kit.quiet(target, MobEffects.LEVITATION, (int)(40 + 20 * w.power()), 1);
            }

            return amount;
        }
    },
    /** Evertwine: the golden rope finds what its owner points at; it pulls them to a wall, or binds a creature fast. */
    GOLDEN_TWINE(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            ServerPlayer p = w.player();
            LivingEntity seen = Targets.looked(p, 16.0);
            if (seen != null) {
                Kit.quiet(seen, MobEffects.MOVEMENT_SLOWDOWN, 60, 4);
                Vfx.beam(w.level(), p.getEyePosition(), seen.getEyePosition(), Vfx.Palette.LIGHT, 0.4);
                return true;
            }

            var hit = Targets.lookedBlock(p, 24.0, net.minecraft.world.level.ClipContext.Fluid.NONE);
            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                return false;
            }

            Vec3 pull = hit.getLocation().subtract(p.position()).normalize().scale(1.6);
            p.setDeltaMovement(pull.x, Math.max(0.4, pull.y + 0.3), pull.z);
            p.hurtMarked = true;
            p.fallDistance = 0.0F;
            Vfx.beam(w.level(), p.getEyePosition(), hit.getLocation(), Vfx.Palette.LIGHT, 0.4);
            return true;
        }
    },
    /** Essence Quiver: arrows woven from essence whenever the archer has none. */
    ESSENCE_ARROWS(Pool.NAMED),
    /** Silent Dagger: no sound follows its wielder, and its first cut finds the unwary. */
    HUSH(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.melee(source) && !(target instanceof Mob mob && mob.getTarget() == w.player()) ? amount * 1.3F : amount;
        }
    },
    /** An enchanted bag: far larger inside than out, but it tips its contents out whenever it is dismissed. */
    SPILLING_BAG(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            MemoryStorage.open(w, 18);
            return true;
        }

        @Override
        public void dismissed(Worn w, Vec3 at) {
            MemoryStorage.spill(w, at);
        }
    },
    /** Bag of Withholding: larger inside than out, and it keeps what it holds through every summoning. */
    WITHHOLDING(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            MemoryStorage.open(w, 27);
            return true;
        }
    },

    /** Blood Arrow: arrows made of the archer's own blood, as many as they can bleed. */
    RAIN_OF_BLOOD(Pool.NAMED),
    /** Blood Arrow: a hit drinks the prey's blood to restore the archer; a miss costs the archer more. */
    RESTITUTION(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.shot()) {
                w.player().heal(amount * 0.5F);
                Vfx.trail(w.level(), target.getBoundingBox().getCenter(), Vfx.chest(w.player()), Vfx.Palette.BLOOD);
            }

            return amount;
        }
    },
    /** Blackhorn Bow: strong and sturdy beyond its make. */
    UNYIELDING_HORN(Pool.NAMED) {
        @Override
        public double durability() {
            return 3.0;
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.shot() ? amount + 2.0F : amount;
        }
    },
    /** Full Quiver: not one arrow but a whole quiver of them, which never runs dry. */
    ENDLESS_VOLLEY(Pool.NAMED),
    /** Strike of Thunder: flies as fast as lightning and leaps from its prey to whatever stands near. */
    CAGED_LIGHTNING(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.shot()) {
                chain(w, target, amount * 0.6F, 3, 6.0);
                Kit.sound(w.level(), target.position(), SoundEvents.LIGHTNING_BOLT_IMPACT, 0.8F, 1.4F);
            }

            return amount;
        }
    },
    /** Cruel Sight: a sword or a spear, as its master wishes (crouch and use it to change). */
    SHAPESHIFTER(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (!w.player().isShiftKeyDown()) {
                return false;
            }

            SoulMemories.reshape(w, hand);
            return true;
        }
    },
    /** Cruel Sight: its blade drinks in light, and pours it out again as a lance. */
    LIGHT_EATER(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0 && w.held() && w.level().getMaxLocalRawBrightness(w.player().blockPosition()) >= 13) {
                CompoundTag s = w.state();
                s.putInt("Light", Math.min(20, s.getInt("Light") + 1));
                w.setState(s);
            }
        }

        @Override
        public boolean use(Worn w, InteractionHand hand) {
            CompoundTag s = w.state();
            int light = s.getInt("Light");
            if (w.player().isShiftKeyDown() || light < 5) {
                return false;
            }

            s.putInt("Light", 0);
            w.setState(s);
            ServerPlayer p = w.player();
            Vec3 from = p.getEyePosition();
            Vec3 to = from.add(p.getLookAngle().scale(20.0));
            for (LivingEntity target : Targets.line(w.level(), p, 20.0, 1.0)) {
                Targets.hit(w.level(), p, target, light * (float)(0.5 + 0.2 * w.power()));
                target.igniteForSeconds(3.0F);
            }

            Vfx.beam(w.level(), from, to, Vfx.Palette.LIGHT, 0.3);
            Kit.sound(w.level(), p.position(), SoundEvents.BEACON_ACTIVATE, 0.8F, 1.8F);
            return true;
        }
    },
    /** Cruel Sight: its blade cuts what has no body. */
    GHOST_BLADE(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.melee(source) && incorporeal(target) ? amount * 1.6F : amount;
        }
    },
    /** Cruel Sight: each blow may carry any element its wielder has been hurt by. */
    DARK_MIRROR(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            String element = element(source);
            if (element != null) {
                CompoundTag s = w.state();
                s.putString("Element", element);
                w.setState(s);
            }

            return amount;
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (!w.melee(source)) {
                return amount;
            }

            switch (w.state().getString("Element")) {
                case "fire" -> target.igniteForSeconds(4.0F);
                case "cold" -> target.setTicksFrozen(target.getTicksRequiredToFreeze() + 40);
                case "poison" -> Kit.quiet(target, MobEffects.POISON, 80, 1);
                case "wither" -> Kit.quiet(target, MobEffects.WITHER, 80, 0);
                case "lightning" -> chain(w, target, amount * 0.3F, 2, 4.0);
                case "soul" -> Targets.soulHit(w.level(), w.player(), target, amount * 0.25F);
                default -> {
                    return amount;
                }
            }

            return amount * 1.1F;
        }
    },
    /** Covetous Coffer: it can pass for any object at all (crouch and use it on a block to take that block's look). */
    MENDACIOUS_COFFER(Pool.NAMED) {
        @Override
        public boolean useOn(Worn w, BlockPos pos, Direction face) {
            if (!w.player().isShiftKeyDown()) {
                return false;
            }

            BlockState state = w.level().getBlockState(pos);
            CompoundTag s = w.state();
            s.putString("Guise", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            w.setState(s);
            w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.covetous_coffer.guise", state.getBlock().getName()), true);
            return true;
        }
    },
    /** Covetous Coffer: larger inside than out, and what it holds stays in it through every summoning. */
    CAPACIOUS_CHEST(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            if (w.player().isShiftKeyDown()) {
                return false;
            }

            MemoryStorage.open(w, 27 + 9 * Math.min(3, w.data().rank()));
            return true;
        }
    },
    /** Covetous Coffer: it can walk after its owner on its own (crouch and use it on the ground to set it down). */
    LOCOMOTIVE_CHIFFONIER(Pool.NAMED) {
        @Override
        public boolean useOn(Worn w, BlockPos pos, Direction face) {
            return face == Direction.UP && !w.player().isShiftKeyDown() && MemoryStorage.walk(w, pos.above());
        }
    },
    /** Covetous Coffer: it defends itself from thieves, by eating them. */
    LURID_TRUNK(Pool.NAMED),
    /** Morgan's Warbow: its arrows are made of its wielder's soul, and share its nature. */
    SOUL_ARROWS(Pool.NAMED),
    UNBENDING(Pool.NAMED) {
        @Override
        public double durability() {
            return 4.0;
        }
    },
    /** Morgan's Warbow: what its arrows pierce grows terribly heavy. */
    BURDEN_OF_PEACE(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.shot()) {
                Kit.quiet(target, MobEffects.MOVEMENT_SLOWDOWN, 80, 4);
                Kit.quiet(target, MobEffects.JUMP, 80, 250);
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, -0.6, 0.0));
            }

            return amount;
        }
    },
    /** Morgan's Warbow: at the cost of a great deal of essence, a shot that obliterates (crouch at full draw). */
    DEATH_DEALER(Pool.NAMED),
    /** Memory of Fire: moderate protection from fire. */
    SCORCHING_HEAT(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FIRE) ? amount * 0.5F : amount;
        }
    },
    /** Memory of Ice: moderate protection from cold. */
    BITTER_COLD(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (w.player().getTicksFrozen() > 0) {
                w.player().setTicksFrozen(Math.max(0, w.player().getTicksFrozen() - 2));
            }
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FREEZING) ? amount * 0.5F : amount;
        }
    },
    /** Mirror of Truth: catch a being's reflection, then break the mirror to borrow one of its powers for a while. */
    SEARCHING_REFLECTION(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            CompoundTag s = w.state();
            if (!s.contains("Family")) {
                LivingEntity seen = Targets.looked(w.player(), 24.0);
                if (seen == null) {
                    return false;
                }

                s.putString("Family", Family.of(seen).id());
                s.putString("Being", seen.getType().getDescriptionId());
                w.setState(s);
                w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.mirror_of_truth.caught",
                    Component.translatable(seen.getType().getDescriptionId())), true);
                Vfx.trail(w.level(), seen.getEyePosition(), w.player().getEyePosition(), Vfx.Palette.FROST);
                return true;
            }

            if (!w.player().isShiftKeyDown()) {
                w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.mirror_of_truth.holds",
                    Component.translatable(s.getString("Being"))), true);
                return false;
            }

            borrow(w.player(), s.getString("Family"), (int)(1200 * (1.0 + 0.2 * w.power())));
            Vfx.shards(w.level(), w.player().getEyePosition(), Vfx.Palette.FROST, 24);
            Kit.sound(w.level(), w.player().position(), SoundEvents.GLASS_BREAK, 1.0F, 0.7F);
            SoulMemories.destroy(w.player(), w.data().id(), false);
            return true;
        }
    },
    /** Patient Avenger: especially durable. */
    TENACIOUS(Pool.NAMED) {
        @Override
        public double durability() {
            return 4.0;
        }
    },
    /** Patient Avenger: keeps some of the fire it stops, to let out as a shockwave (crouch and use the shield). */
    BURNING_HEART(Pool.NAMED) {
        @Override
        public float blocked(Worn w, DamageSource source, float amount) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                CompoundTag s = w.state();
                s.putFloat("Heat", Math.min(60.0F, s.getFloat("Heat") + amount));
                w.setState(s);
            }

            return amount;
        }

        @Override
        public boolean use(Worn w, InteractionHand hand) {
            return w.player().isShiftKeyDown() && avenge(w);
        }
    },
    /** Patient Avenger: keeps some of the force of the blows it stops, to let out as a wave of fire. */
    COLD_STEEL(Pool.NAMED) {
        @Override
        public float blocked(Worn w, DamageSource source, float amount) {
            if (!source.is(DamageTypeTags.IS_FIRE)) {
                CompoundTag s = w.state();
                s.putFloat("Force", Math.min(60.0F, s.getFloat("Force") + amount));
                w.setState(s);
            }

            return amount;
        }
    },
    /** Bitter Cusp: the cup brims with a bitter venom. Coat a blade in it; don't drink it (crouch to drink anyway). */
    BITTER_DRAUGHT(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            ServerPlayer p = w.player();
            if (p.isShiftKeyDown()) {
                Kit.quiet(p, MobEffects.POISON, 200, 2);
                Kit.quiet(p, MobEffects.CONFUSION, 200, 0);
                p.displayClientMessage(Component.translatable("memory.nightmare_evolved.bitter_cusp.drunk"), true);
                return true;
            }

            if (!ready(w, "Pour", 400)) {
                return false;
            }

            SoulMemories.coat(p, 8);
            Kit.sound(w.level(), p.position(), SoundEvents.BREWING_STAND_BREW, 0.8F, 0.6F);
            return true;
        }
    },
    /** Nameless Sun: a blade like a fragment of daylight; what it cuts burns, and the dead burn brightest. */
    RADIANCE(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (!w.melee(source)) {
                return amount;
            }

            target.igniteForSeconds(5.0F);
            Vfx.embers(w.level(), target.getBoundingBox().getCenter(), Vfx.Palette.LIGHT, 6, 0.4);
            return target.getType().is(EntityTypeTags.UNDEAD) ? amount * 1.4F : amount * 1.1F;
        }
    },
    /** Mantle of the Underworld: the armour mends itself while it is worn. */
    LIVING_STONE(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0 && w.stack().isDamaged()) {
                w.stack().setDamageValue(w.stack().getDamageValue() - 1);
            }
        }
    },
    /** Mantle of the Underworld: its weight is the wearer's to change; crouch in the air to fall like a stone. */
    FEATHER_OF_TRUTH(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            ServerPlayer p = w.player();
            if (p.isShiftKeyDown() && !p.onGround() && p.getDeltaMovement().y < 0.0) {
                p.setDeltaMovement(p.getDeltaMovement().add(0.0, -0.12, 0.0));
                p.hurtMarked = true;
            }
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return source.is(DamageTypeTags.IS_FALL) ? amount * 0.4F : amount;
        }
    },
    /** Mantle of the Underworld, and the Stone Saint's line: very hard to harm by blows, hard by elements. */
    STALWART(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            if (soulAttack(source)) {
                return amount * 0.9F;
            }

            return source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypes.LIGHTNING_BOLT)
                ? amount * 0.85F : amount * 0.8F;
        }
    },
    /** Mantle of the Underworld: holds a charm and makes its enchantments its own, and stronger. */
    UNDERWORLD_ARMAMENT(Pool.NAMED),
    /** Mantle of the Underworld: the more foes its wearer defeats, the stronger it grows. */
    PRINCE_OF_THE_UNDERWORLD(Pool.NAMED) {
        @Override
        public void killed(Worn w, LivingEntity victim) {
            if (victim instanceof Enemy || Creatures.ofNightmare(victim)) {
                CompoundTag s = w.state();
                s.putInt("Felled", s.getInt("Felled") + 1);
                w.setState(s);
            }
        }

        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            int felled = w.state().getInt("Felled");
            out.add(Attributes.ARMOR, Math.min(6.0, felled * 0.02), AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ATTACK_DAMAGE, Math.min(4.0, felled * 0.01), AttributeModifier.Operation.ADD_VALUE);
        }
    },
    /** Mantle of the Underworld: a soulbound relic. Bound to its owner, it becomes part of them (crouch-use, twice). */
    SOULBOUND_RELIC(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            return w.player().isShiftKeyDown() && SoulMemories.bindRelic(w, SoulAttribute.MARBLE_SHELL.id());
        }
    },
    /** Dire Warning: creatures of lesser Rank than its owner are frightened away. */
    DIRE_WARNING(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 != 0) {
                return;
            }

            int rank = w.soul().rank().ladder;
            for (LivingEntity foe : near(w, 12.0)) {
                if (foe instanceof Mob mob && Soul.ladder(mob) < rank) {
                    if (mob.getTarget() == w.player()) {
                        mob.setTarget(null);
                    }

                    Vec3 away = mob.position().subtract(w.player().position()).normalize().scale(6.0);
                    mob.getNavigation().moveTo(mob.getX() + away.x, mob.getY(), mob.getZ() + away.z, 1.3);
                }
            }
        }
    },
    /** Altar of Denial: powers cost less essence, but only while its owner stands still. */
    DENIAL(Pool.NAMED) {
        @Override
        public double cost(Worn w) {
            return w.player().getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 ? 0.6 : 1.0;
        }
    },
    /** Ananke's Mantle: it endures what would tear another garment, and spares its wearer some of it. */
    ENDURING(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return amount * 0.92F;
        }
    },
    /** Ananke's Mantle: its wearer is hard to find; creatures lose track of them at a distance. */
    CONCEALING(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 != 0) {
                return;
            }

            for (LivingEntity foe : near(w, 32.0)) {
                if (foe instanceof Mob mob && mob.getTarget() == w.player() && mob.distanceToSqr(w.player()) > 10.0 * 10.0
                    && w.player().getLastHurtMob() != mob) {
                    mob.setTarget(null);
                }
            }
        }
    },
    /** Ananke's Mantle: whatever hungers finds its wearer unappetising. */
    TASTELESS(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0) {
                cleanse(w.player(), MobEffects.POISON, MobEffects.HUNGER);
            }
        }
    },
    /** Dying Wish: all who see its wearer are stirred: allies take heart, foes come for them, and their power grows. */
    DEATH_WISH(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 != 0) {
                return;
            }

            for (LivingEntity ally : Targets.alliesNear(w.level(), w.player(), w.player().position(), 24.0)) {
                if (ally != w.player()) {
                    Kit.quiet(ally, MobEffects.DAMAGE_BOOST, 40, 0);
                    Kit.quiet(ally, MobEffects.MOVEMENT_SPEED, 40, 0);
                }
            }

            for (LivingEntity foe : near(w, 24.0)) {
                if (foe instanceof Mob mob && mob.getTarget() == null && mob.hasLineOfSight(w.player())) {
                    mob.setTarget(w.player());
                }
            }
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            long foes = near(w, 24.0).stream().filter(e -> e instanceof Mob mob && mob.getTarget() == w.player()).count();
            return amount * (1.0F + Math.min(0.6F, 0.06F * foes));
        }
    },
    /** Shroud of Graceless Dusk: a sharper mind, quicker to see and slower to be clouded. */
    SHARPENED_MIND(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0) {
                cleanse(w.player(), MobEffects.CONFUSION, MobEffects.BLINDNESS, MobEffects.DARKNESS);
            }
        }

        @Override
        public double cooldown(Worn w) {
            return 0.9;
        }
    },
    /** Shroud of Graceless Dusk: essence returns faster to its wearer. */
    DUSK_WELLSPRING(Pool.NAMED) {
        @Override
        public double regen(Worn w) {
            return 1.3;
        }
    },
    /** Shroud of Graceless Dusk: woven of sublime silk, impervious and splendid. */
    SUBLIME_SILK(Pool.NAMED) {
        @Override
        public double durability() {
            return 4.0;
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            return amount * 0.85F;
        }
    },
    /** Shroud of Graceless Dusk: the last sybil's blessing, a link of the mind to one other being (use it on them). */
    SYBILS_BLESSING(Pool.NAMED) {
        @Override
        public boolean useOnEntity(Worn w, LivingEntity target) {
            if (!(target instanceof ServerPlayer other)) {
                return false;
            }

            CompoundTag s = w.state();
            s.putUUID("Linked", other.getUUID());
            w.setState(s);
            w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.shroud_of_graceless_dusk.linked", other.getDisplayName()), true);
            return true;
        }

        @Override
        public void tick(Worn w, int age) {
            CompoundTag s = w.state();
            if (age % 40 != 0 || !s.hasUUID("Linked")) {
                return;
            }

            ServerPlayer other = w.level().getServer().getPlayerList().getPlayer(s.getUUID("Linked"));
            if (other != null && other.level() == w.level()) {
                w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.shroud_of_graceless_dusk.sense", other.getDisplayName(),
                    compass(w.player().position(), other.position()), (int)w.player().distanceTo(other), Math.round(other.getHealth())), true);
            }
        }
    },
    /** Sin of Solace: it wounds the mind along with the body. */
    SINISTER_WHISPER(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            if (w.melee(source)) {
                Kit.quiet(target, MobEffects.CONFUSION, 80, 0);
                Kit.quiet(target, MobEffects.WEAKNESS, 80, 0);
                SoulMemories.later(() -> Targets.soulHit(w.level(), w.player(), target, amount * 0.25F), 1);
            }

            return amount;
        }
    },
    /** Sin of Solace: carved from sublime jade, sharp and durable beyond reason. */
    PERFECT_JADE(Pool.NAMED) {
        @Override
        public double durability() {
            return 12.0;
        }

        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.melee(source) ? amount + 2.0F : amount;
        }
    },
    /** Sin of Solace: a remnant of a daemon's presence that strikes dread into every heart around it. */
    OMEN_OF_DREAD(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 20 != 0 || !w.held()) {
                return;
            }

            for (LivingEntity foe : near(w, 8.0)) {
                Kit.quiet(foe, MobEffects.WEAKNESS, 40, 0);
                Kit.quiet(foe, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
            }
        }
    },
    /** Sin of Solace: the more its wielder's sanity breaks, the stronger it becomes. */
    HIDEOUS_TRUTH(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.melee(source) ? amount * (1.0F + w.state().getInt("Madness") / 200.0F) : amount;
        }
    },
    /** Sin of Solace: it slowly drives its wielder mad. Its whispers fade while it rests in the soul. */
    CURSED(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 200 != 0) {
                return;
            }

            CompoundTag s = w.state();
            int madness = Math.min(100, s.getInt("Madness") + 1);
            s.putInt("Madness", madness);
            w.setState(s);
            if (madness >= 40 && w.player().getRandom().nextInt(100) < madness) {
                w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.sin_of_solace.whisper." + w.player().getRandom().nextInt(6))
                    .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
                if (madness >= 70) {
                    Kit.quiet(w.player(), MobEffects.CONFUSION, 100, 0);
                }
            }
        }
    },
    /** Stifled Scream: its master's body grows stronger for as long as they keep silent. */
    SILENT_MIGHT(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 100 == 0) {
                long silent = w.level().getGameTime() - w.state().getLong("Spoke");
                int level = silent > 24000 ? 2 : silent > 6000 ? 1 : silent > 1200 ? 0 : -1;
                if (level >= 0) {
                    Kit.quiet(w.player(), MobEffects.DAMAGE_BOOST, 120, level);
                }
            }
        }
    },
    /** Stifled Scream: after a great silence, a single word of its master's is a command few can ignore. */
    WORD_OF_POWER(Pool.NAMED),
    /** Undying Chain: chains that reach for whoever hurts their wearer. */
    CHAINS_OF_LONGING(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            if (source.getEntity() instanceof LivingEntity attacker && attacker != w.player() && attacker.distanceToSqr(w.player()) < 16.0 * 16.0) {
                Kit.shove(attacker, w.player().position(), -0.8, 0.1);
                Kit.quiet(attacker, MobEffects.MOVEMENT_SLOWDOWN, 40, 2);
                Vfx.beam(w.level(), Vfx.chest(w.player()), attacker.getBoundingBox().getCenter(), Vfx.Palette.STEEL, 0.5);
            }

            return amount;
        }
    },
    /** Undying Chain: an unbending will; the mind can't be clouded. */
    BRILLIANT_WILL(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 10 == 0) {
                cleanse(w.player(), MobEffects.CONFUSION, MobEffects.BLINDNESS, MobEffects.DARKNESS, MobEffects.WEAKNESS);
            }
        }
    },
    /** Undying Chain: its wearer can not die. The chain bears the cost; if it ever breaks, so does the promise. */
    UNDYING(Pool.NAMED) {
        @Override
        public boolean spares(Worn w, DamageSource source) {
            SoulData soul = w.soul();
            double toll = soul.maxEssence() * 0.3;
            if (!soul.spend(Math.min(soul.essence(), toll))) {
                return false;
            }

            if (soul.essence() < 1.0) {
                w.stack().hurtAndBreak(Math.max(1, w.stack().getMaxDamage() / 4), w.player(), net.minecraft.world.entity.EquipmentSlot.CHEST);
            }

            Vfx.burst(w.level(), Vfx.chest(w.player()), Vfx.Palette.STEEL, 30, 0.3);
            Kit.sound(w.level(), w.player().position(), SoundEvents.CHAIN_BREAK, 1.2F, 0.5F);
            return true;
        }
    },
    /** Dark Shaper: the black-stone mallet reshapes what it strikes: a block into its stair, slab or wall, and back. */
    SCULPTORS_WILL(Pool.NAMED) {
        @Override
        public boolean useOn(Worn w, BlockPos pos, Direction face) {
            BlockState state = w.level().getBlockState(pos);
            Block next = SoulMemories.reshapeBlock(state.getBlock());
            if (next == null || !w.player().mayBuild() || !pay(w, 1.0)) {
                return false;
            }

            w.level().setBlock(pos, next.defaultBlockState(), Block.UPDATE_ALL);
            Kit.sound(w.level(), Vec3.atCenterOf(pos), SoundEvents.STONE_PLACE, 1.0F, 0.7F);
            Vfx.burst(w.level(), Vec3.atCenterOf(pos), Vfx.Palette.SHADOW, 6, 0.1);
            return true;
        }
    },
    /** Crown of Twilight: essence floods back at the borders of day and night. */
    LEGACY_OF_TWILIGHT(Pool.NAMED) {
        @Override
        public double regen(Worn w) {
            long time = w.level().getDayTime() % 24000L;
            return time > 11500L && time < 13500L || time > 22500L || time < 500L ? 3.0 : 1.0;
        }
    },
    /** Crown of Twilight: no mind can be touched by any other, but wrath comes easily and will not be denied. */
    KINGS_RESENTMENT(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 10 == 0) {
                cleanse(w.player(), MobEffects.CONFUSION, MobEffects.BLINDNESS, MobEffects.DARKNESS);
            }
        }

        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            if (soulAttack(source)) {
                return 0.0F;
            }

            if (source.getEntity() instanceof LivingEntity) {
                Kit.quiet(w.player(), MobEffects.DAMAGE_BOOST, 100, 1);
            }

            return amount * 1.1F;
        }
    },
    /** Crown of Twilight: the water obeys its wearer's Domain; in it, they are its master. */
    ROYAL_PROMISE(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            ServerPlayer p = w.player();
            if (age % 20 == 0 && p.isInWaterOrRain()) {
                Kit.quiet(p, MobEffects.CONDUIT_POWER, 60, 0);
                Kit.quiet(p, MobEffects.DOLPHINS_GRACE, 60, 0);
                Kit.quiet(p, MobEffects.DAMAGE_BOOST, 60, 0);
            }
        }
    },
    /** Beast Locket: inside it lies a sunlit meadow, where living beings can be kept (use on a creature; crouch to free). */
    POCKET_MEADOW(Pool.NAMED) {
        @Override
        public boolean useOnEntity(Worn w, LivingEntity target) {
            return target instanceof Mob mob && SoulMemories.keepInLocket(w, mob);
        }

        @Override
        public boolean use(Worn w, InteractionHand hand) {
            return w.player().isShiftKeyDown() && SoulMemories.freeFromLocket(w);
        }
    },
    /** Condemnation: a sacred blade, dreadful to stand near, that cuts through what armour is worn against it. */
    JUDGEMENT(Pool.NAMED) {
        @Override
        public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
            return w.melee(source) ? amount * 1.3F + target.getArmorValue() * 0.4F : amount;
        }

        @Override
        public void tick(Worn w, int age) {
            if (age % 20 == 0 && w.held()) {
                for (LivingEntity foe : near(w, 6.0)) {
                    Kit.quiet(foe, MobEffects.WEAKNESS, 40, 1);
                }
            }
        }
    },
    /** Evening Star: power in every shape it takes: strength, endurance, will and soul. */
    BOUNDLESS_POWER(Pool.NAMED) {
        @Override
        public void modifiers(Worn w, Ability.Modifiers out) {
            out.add(Attributes.MAX_HEALTH, 8.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE);
            out.add(Attributes.ATTACK_DAMAGE, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            out.add(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }

        @Override
        public double regen(Worn w) {
            return 1.3;
        }
    },
    /** Weaver's Mask: hides who wears it. */
    MANTLE_OF_LIES(Pool.NAMED),
    /** Weaver's Mask: a glimpse of the threads of Fate; sometimes a blow is seen before it is struck. */
    WHERE_IS_MY_EYE(Pool.NAMED) {
        @Override
        public float taken(Worn w, DamageSource source, float amount) {
            if (source.getEntity() instanceof LivingEntity && w.player().getRandom().nextFloat() < 0.15F) {
                Vfx.glyphs(w.level(), w.player().getEyePosition(), 0.6, Vfx.Palette.RUNE, 4);
                return 0.0F;
            }

            return amount;
        }
    },
    /** Weaver's Mask: turns its wearer's Flaw against itself; while the mask is worn, the Flaw has no hold on them. */
    SIMPLE_TRICK(Pool.NAMED),
    /** Shadow Lantern: it devours light and holds shadows without end, gathering them in the dark to set them loose. */
    GATES_OF_SHADOW(Pool.NAMED) {
        @Override
        public void tick(Worn w, int age) {
            if (age % 100 == 0 && w.held() && w.level().getMaxLocalRawBrightness(w.player().blockPosition()) < 6) {
                CompoundTag s = w.state();
                s.putInt("Shadows", Math.min(7 + w.data().rank(), s.getInt("Shadows") + 1));
                w.setState(s);
            }
        }

        @Override
        public boolean use(Worn w, InteractionHand hand) {
            CompoundTag s = w.state();
            int shadows = s.getInt("Shadows");
            if (shadows <= 0) {
                for (LivingEntity foe : near(w, 10.0)) {
                    Kit.quiet(foe, MobEffects.DARKNESS, 120, 0);
                    Kit.quiet(foe, MobEffects.BLINDNESS, 60, 0);
                }

                Vfx.dome(w.level(), w.player().position(), 10.0, Vfx.Palette.SHADOW, 60);
                return true;
            }

            s.putInt("Shadows", 0);
            w.setState(s);
            SoulMemories.releaseShadows(w, shadows);
            return true;
        }
    },
    /** Drop of Ichor: a drop of Weaver's divine lineage, which becomes part of whoever consumes it. */
    WEAVERS_LINEAGE(Pool.NAMED) {
        @Override
        public boolean use(Worn w, InteractionHand hand) {
            return SoulMemories.consumeLineage(w, List.of(SoulAttribute.BLOOD_WEAVE, SoulAttribute.BONE_WEAVE, SoulAttribute.FLESH_WEAVE,
                SoulAttribute.MIND_WEAVE, SoulAttribute.SOUL_WEAVE));
        }
    };

    /** Which Memories the Spell may roll an enchantment on; named ones belong only to their own Memories. */
    public enum Pool {
        WEAPON,
        ARMOR,
        CHARM,
        GARMENT,
        ANY,
        NAMED
    }

    public final Pool pool;

    Enchant(Pool pool) {
        this.pool = pool;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("enchant.nightmare_evolved." + this.id());
    }

    public Component description() {
        return Component.translatable("enchant.nightmare_evolved." + this.id() + ".desc");
    }

    @Nullable
    public static Enchant byId(String id) {
        for (Enchant enchant : values()) {
            if (enchant.id().equals(id)) {
                return enchant;
            }
        }

        return null;
    }

    /** The enchantments the Spell may give a Memory of a kind. */
    public static List<Enchant> pool(MemoryDef.Kind kind) {
        Pool own = switch (kind) {
            case WEAPON -> Pool.WEAPON;
            case ARMOR -> Pool.ARMOR;
            case CHARM -> Pool.CHARM;
            case GARMENT -> Pool.GARMENT;
            default -> Pool.ANY;
        };
        List<Enchant> out = new ArrayList<>();
        for (Enchant enchant : values()) {
            if (enchant.pool == own || enchant.pool == Pool.ANY) {
                out.add(enchant);
            }
        }

        return out;
    }

    // What an enchantment can do. Each is optional.

    /** Every tick while the Memory is summoned and carried. */
    public void tick(Worn w, int age) {
    }

    /** Lasting changes to its owner's body while it is summoned and carried. */
    public void modifiers(Worn w, Ability.Modifiers out) {
    }

    /** A blow its owner lands, with it or while carrying it; returns the damage the blow should do. */
    public float dealt(Worn w, LivingEntity target, DamageSource source, float amount) {
        return amount;
    }

    /** A blow its owner takes while carrying it; returns the damage the blow should do. */
    public float taken(Worn w, DamageSource source, float amount) {
        return amount;
    }

    /** A blow it stopped, held up as a shield; returns the damage stopped. */
    public float blocked(Worn w, DamageSource source, float amount) {
        return amount;
    }

    public void killed(Worn w, LivingEntity victim) {
    }

    /** Whether it keeps its owner alive through a blow that would have killed them. */
    public boolean spares(Worn w, DamageSource source) {
        return false;
    }

    /** How fast its owner's essence returns, as a share. */
    public double regen(Worn w) {
        return 1.0;
    }

    /** What its owner's powers cost, as a share. */
    public double cost(Worn w) {
        return 1.0;
    }

    /** How long its owner's powers rest, as a share. */
    public double cooldown(Worn w) {
        return 1.0;
    }

    /** Used in the air; returns whether anything happened. */
    public boolean use(Worn w, InteractionHand hand) {
        return false;
    }

    /** Used on a block; returns whether anything happened. */
    public boolean useOn(Worn w, BlockPos pos, Direction face) {
        return false;
    }

    /** Used on a creature; returns whether anything happened. */
    public boolean useOnEntity(Worn w, LivingEntity target) {
        return false;
    }

    /** Where it lands when thrown. */
    public void thrownHit(Worn w, @Nullable LivingEntity target, Vec3 at) {
    }

    /** As it is dismissed, at the place it was. */
    public void dismissed(Worn w, Vec3 at) {
    }

    /** How much longer it lasts than an unenchanted Memory. */
    public double durability() {
        return 1.0;
    }

    /** Whether it is summoned at once, without the moment it takes to gather. */
    public boolean instant() {
        return false;
    }

    /** Whether the essence spent on it comes back however far away it is when dismissed. */
    public boolean tethered() {
        return false;
    }

    // Shared helpers.

    static List<LivingEntity> near(Worn w, double radius) {
        return Targets.near(w.level(), w.player(), w.player().position(), radius);
    }

    /** An attack on the soul or the mind, rather than the body. */
    static boolean soulAttack(DamageSource source) {
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.SONIC_BOOM);
    }

    /** Things with little or no body: ghosts and spirits, and the undead that are mostly bone or will. */
    static boolean incorporeal(LivingEntity target) {
        EntityType<?> type = target.getType();
        return Family.of(target) == Family.SPIRIT || type == EntityType.ALLAY || type == EntityType.WITHER || type == EntityType.WITHER_SKELETON
            || type == EntityType.SKELETON || type == EntityType.STRAY;
    }

    @Nullable
    static String element(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return "fire";
        } else if (source.is(DamageTypeTags.IS_FREEZING)) {
            return "cold";
        } else if (source.is(DamageTypes.LIGHTNING_BOLT)) {
            return "lightning";
        } else if (source.is(DamageTypes.WITHER)) {
            return "wither";
        } else if (soulAttack(source)) {
            return "soul";
        }

        return source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(MobEffects.POISON) ? "poison" : null;
    }

    @SafeVarargs
    static void cleanse(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>... effects) {
        for (var effect : effects) {
            entity.removeEffect(effect);
        }
    }

    /** Arcs from a target to up to {@code count} foes near it, each taking {@code damage}. */
    static void chain(Worn w, LivingEntity from, float damage, int count, double reach) {
        LivingEntity last = from;
        List<LivingEntity> struck = new ArrayList<>(List.of(from));
        for (int i = 0; i < count; i++) {
            LivingEntity hop = null;
            double best = reach * reach;
            for (LivingEntity e : w.level().getEntitiesOfClass(LivingEntity.class, last.getBoundingBox().inflate(reach), e -> Targets.foe(e, w.player()))) {
                double d = e.distanceToSqr(last);
                if (!struck.contains(e) && d < best) {
                    best = d;
                    hop = e;
                }
            }

            if (hop == null) {
                return;
            }

            Vfx.beam(w.level(), last.getBoundingBox().getCenter(), hop.getBoundingBox().getCenter(), Vfx.Palette.RUNE, 0.35);
            hop.invulnerableTime = 0;
            Targets.hit(w.level(), w.player(), hop, damage);
            struck.add(hop);
            last = hop;
        }
    }

    /** Whether a use with its own rest is ready, starting the rest if so. */
    static boolean ready(Worn w, String key, int ticks) {
        CompoundTag s = w.state();
        long now = w.level().getGameTime();
        if (now < s.getLong(key)) {
            w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.resting", (s.getLong(key) - now + 19) / 20), true);
            return false;
        }

        s.putLong(key, now + ticks);
        w.setState(s);
        return true;
    }

    /** Spends one of a Memory's charges, which come back one every {@code refill} ticks up to {@code max}. */
    static boolean spendCharge(Worn w, String key, int max, int refill) {
        CompoundTag s = w.state();
        long now = w.level().getGameTime();
        int charges = s.contains(key) ? s.getInt(key) : max;
        long since = now - s.getLong(key + "At");
        if (since > 0 && s.contains(key + "At")) {
            charges = (int)Math.min(max, charges + since / refill);
        }

        if (charges <= 0) {
            w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.dry"), true);
            return false;
        }

        s.putInt(key, charges - 1);
        s.putLong(key + "At", now);
        w.setState(s);
        return true;
    }

    static boolean pay(Worn w, double essence) {
        if (w.soul().spend(essence)) {
            return true;
        }

        w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.no_essence"), true);
        return false;
    }

    static void drink(Worn w, int food, float saturation) {
        ServerPlayer p = w.player();
        p.getFoodData().eat(food, saturation);
        p.clearFire();
        Kit.sound(w.level(), p.position(), SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
        Vfx.burst(w.level(), p.getEyePosition(), Vfx.Palette.FROST, 8, 0.1);
    }

    /** Which way something lies, as a compass point. */
    static Component compass(Vec3 from, Vec3 to) {
        double angle = Math.toDegrees(Math.atan2(to.z - from.z, to.x - from.x));
        String[] points = {"east", "south_east", "south", "south_west", "west", "north_west", "north", "north_east"};
        int index = Math.floorMod(Math.round(angle / 45.0), 8);
        return Component.translatable("memory.nightmare_evolved.compass." + points[index]);
    }

    /** Shatters the Mirror of Truth's reflection into its holder: one power of that kind of being, for a while. */
    static void borrow(ServerPlayer p, String family, int ticks) {
        switch (family) {
            case "undead" -> {
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 0));
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, ticks, 0));
            }
            case "arthropod" -> p.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 2));
            case "beast" -> {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ticks, 1));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ticks, 0));
            }
            case "aquatic" -> {
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, ticks, 0));
                p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, ticks, 0));
            }
            case "fire" -> p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ticks, 0));
            case "ender", "spirit" -> {
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, ticks, 0));
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks / 2, 0));
            }
            case "construct" -> p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 1));
            case "human" -> p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, ticks, 1));
            default -> p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks / 4, 0));
        }

        p.displayClientMessage(Component.translatable("memory.nightmare_evolved.mirror_of_truth.borrowed." + family), true);
    }

    /** Patient Avenger lets go of what it kept: stored fire as a shockwave, stored force as a wave of flame. */
    static boolean avenge(Worn w) {
        CompoundTag s = w.state();
        float heat = s.getFloat("Heat");
        float force = s.getFloat("Force");
        if (heat + force < 4.0F) {
            w.player().displayClientMessage(Component.translatable("memory.nightmare_evolved.patient_avenger.empty"), true);
            return false;
        }

        ServerPlayer p = w.player();
        for (LivingEntity foe : near(w, 6.0)) {
            if (heat > 0.0F) {
                Targets.hit(w.level(), p, foe, heat * 0.6F);
                Kit.shove(foe, p.position(), 1.2, 0.4);
            }

            if (force > 0.0F) {
                Targets.hit(w.level(), p, foe, force * 0.4F);
                foe.igniteForSeconds(4.0F);
            }
        }

        if (heat > 0.0F) {
            Vfx.shockwave(w.level(), p.position(), 6.0, Vfx.Palette.STEEL);
        }

        if (force > 0.0F) {
            Vfx.ring(w.level(), p.position().add(0.0, 0.2, 0.0), 6.0, Vfx.Palette.FIRE, 48);
            Vfx.embers(w.level(), p.position(), Vfx.Palette.FIRE, 30, 3.0);
        }

        s.putFloat("Heat", 0.0F);
        s.putFloat("Force", 0.0F);
        w.setState(s);
        Kit.sound(w.level(), p.position(), SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 1.3F);
        return true;
    }
}
