package net.logosnumen.nightmareevolved.dream.gate;

import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.dream.DreamRealm;
import net.logosnumen.nightmareevolved.dream.RealmMap;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.dream.seed.SeedEntities;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * The two altars of the Chained Isles and what they give. The Night Temple's black altar takes a little blood, cut from
 * the palm with a blade, and gives the ivory knife. The Sanctuary of Noctis's white altar, on which an obsidian knife lies
 * that no one has ever lifted, lets it be taken by whoever lays more than a thousand of the Isles' strange gold coins on
 * it under the moon. Either knife can cut the chains of the Dark Seed.
 */
public final class Altars {
    public static final int COINS = 1000;

    private Altars() {
    }

    /** An offering at an altar; true if the altar took notice of it. */
    public static boolean offer(ServerPlayer player, BlockPos pos, GatewayBlock.Form form, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        CompoundTag given = Soul.of(player).dream();
        if (form == GatewayBlock.Form.BLACK_ALTAR) {
            if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem)) {
                return false;
            }

            if (given.getBoolean("IvoryKnife")) {
                Spell.say(player, "spell.nightmare_evolved.altar_black_done");
                return true;
            }

            player.hurt(level.damageSources().magic(), 4.0F);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 8, 0.3, 0.1, 0.3, 0.0);
            level.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.5F, 0.5F);
            give(player, new ItemStack(SeedEntities.IVORY_KNIFE.get()));
            given.putBoolean("IvoryKnife", true);
            Spell.say(player, "spell.nightmare_evolved.altar_black");
            return true;
        }

        if (!stack.is(SeedEntities.ANCIENT_COIN.get())) {
            return false;
        }

        if (given.getBoolean("ObsidianKnife")) {
            Spell.say(player, "spell.nightmare_evolved.altar_white_done");
            return true;
        }

        boolean moonlit = level.isNight() && level.canSeeSky(pos.above());
        int coins = player.getInventory().countItem(SeedEntities.ANCIENT_COIN.get());
        if (!moonlit || coins < COINS) {
            Spell.say(player, "spell.nightmare_evolved.altar_white_wants", COINS);
            return true;
        }

        int left = COINS;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && left > 0; slot++) {
            ItemStack held = player.getInventory().getItem(slot);
            if (held.is(SeedEntities.ANCIENT_COIN.get())) {
                int take = Math.min(left, held.getCount());
                held.shrink(take);
                left -= take;
            }
        }

        Vfx.pillar(level, Vec3.atCenterOf(pos), 8.0, Vfx.Palette.LIGHT);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5F, 0.6F);
        give(player, new ItemStack(SeedEntities.OBSIDIAN_KNIFE.get()));
        given.putBoolean("ObsidianKnife", true);
        Spell.say(player, "spell.nightmare_evolved.altar_white");
        return true;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** What Nightmare Creatures of the Chained Isles leave behind: now and then a few of the Isles' strange coins. */
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !DreamRealm.is(level) || !Creatures.ofNightmare(victim)
            || RealmMap.part(victim.getBlockX(), victim.getBlockY(), victim.getBlockZ()) != Region.CHAINED_ISLES) {
            return;
        }

        int count = 1 + level.random.nextInt(3) + Creatures.rankOf(victim).ordinal();
        event.getDrops().add(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), new ItemStack(SeedEntities.ANCIENT_COIN.get(), count)));
    }
}
