package net.logosnumen.nightmareevolved.item;

import java.util.List;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A soul shard: what is left of a slain Nightmare Creature's soul. Using it absorbs it into your soul cores (sneak to
 * take just one); the higher the creature's rank, the more each shard fills them.
 */
public final class SoulShardItem extends Item {
    private static final Vfx.Palette[] PALETTES = {
        Vfx.Palette.STEEL, Vfx.Palette.FROST, Vfx.Palette.SOUL, Vfx.Palette.ACID, Vfx.Palette.LIGHT, Vfx.Palette.BLOOD, Vfx.Palette.SHADOW
    };
    /** Shard colours by rank, for tinting the item. */
    public static final int[] COLOURS = {0xD8D8E0, 0x8FD0FF, 0x7CF0C8, 0xB0FF6A, 0xFFD24A, 0xFF6A4A, 0xB070FF};

    public SoulShardItem(Properties properties) {
        super(properties);
    }

    /** The tint for a shard of this stack's rank, opaque. */
    public static int colour(ItemStack stack) {
        return 0xFF000000 | COLOURS[rank(stack).ordinal() % COLOURS.length];
    }

    private static CreatureRank rank(ItemStack stack) {
        return CreatureRank.values()[Math.max(0, Math.min(CreatureRank.values().length - 1, ModItems.shardRank(stack)))];
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.nightmare_evolved.soul_shard.ranked", rank(stack).title()).withStyle(Creatures.colour(rank(stack)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server && level instanceof ServerLevel serverLevel) {
            CreatureRank rank = rank(stack);
            int taken = Soul.absorb(server, rank.ordinal(), player.isShiftKeyDown() ? 1 : stack.getCount());
            if (taken > 0) {
                stack.shrink(taken);
                Vfx.burst(serverLevel, Vfx.chest(player), PALETTES[rank.ordinal()], 12 + taken * 2, 0.25);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.nightmare_evolved.soul_shard.desc").withStyle(ChatFormatting.GRAY));
    }
}
