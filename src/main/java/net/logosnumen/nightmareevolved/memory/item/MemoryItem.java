package net.logosnumen.nightmareevolved.memory.item;

import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.memory.Worn;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * What every Memory item has in common, whatever vanilla item it is built on: its name and runes, how a stray copy
 * dissolves, what happens when it breaks, and how using it reaches its enchantments.
 */
public final class MemoryItem {
    private MemoryItem() {
    }

    /** "[Midnight Shard]", coloured by Rank. */
    public static Component name(ItemStack stack, Component fallback) {
        MemoryData data = SoulMemories.data(stack);
        return data == null ? fallback : Memories.name(data).copy().withStyle(Tier.byOrdinal(data.rank()).colour);
    }

    /** The runes the Spell shows for a Memory: kind, Rank and Tier, enchantments, and what it is. */
    public static void tooltip(ItemStack stack, List<Component> lines) {
        MemoryData data = SoulMemories.data(stack);
        MemoryDef def = data == null ? null : Memories.byId(data.def());
        if (data == null || def == null) {
            return;
        }

        lines.add(Component.translatable("memory.nightmare_evolved.runes.kind", def.kind.title()).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("memory.nightmare_evolved.runes.rank", Tier.byOrdinal(data.rank()).title(), roman(data.tier()))
            .withStyle(ChatFormatting.GRAY));
        if (def.shard) {
            lines.add(Component.translatable("memory.nightmare_evolved.runes.shard").withStyle(ChatFormatting.GOLD));
        }

        for (String id : data.enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null) {
                lines.add(Component.literal("[").append(enchant.title()).append("]").withStyle(style -> style.withColor(Spell.COLOUR)));
                lines.add(Component.literal("  ").append(enchant.description()).withStyle(ChatFormatting.GRAY));
            }
        }

        if (def.named) {
            lines.add(def.description().copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        if (def.filled) {
            lines.add(Component.translatable("memory.nightmare_evolved.runes.filled").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (!data.owned()) {
            lines.add(Component.translatable("memory.nightmare_evolved.runes.ownerless").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static MutableComponent roman(int tier) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII"};
        return Component.literal(numerals[Math.max(0, Math.min(6, tier - 1))]);
    }

    /** Once a second, server side: an ownerless Memory binds to whoever takes it up; a stray copy dissolves. */
    public static void tick(ItemStack stack, Entity holder) {
        if (!holder.level().isClientSide && holder.tickCount % 20 == 0) {
            SoulMemories.checkCopy(stack, holder);
        }
    }

    /** The same for one lying on the ground; true if it dissolved. */
    public static boolean entityTick(ItemStack stack, ItemEntity entity) {
        return !entity.level().isClientSide && entity.tickCount % 20 == 0 && SoulMemories.checkLoose(stack, entity);
    }

    /** Wear on a Memory; if it would break, the Memory is destroyed. */
    public static int damage(ItemStack stack, int amount, @Nullable LivingEntity holder) {
        if (amount > 0 && stack.getDamageValue() + amount >= stack.getMaxDamage()) {
            SoulMemories.broken(stack, holder);
        }

        return amount;
    }

    /** The owner's Memory at work in their hand, or null if it isn't theirs. */
    @Nullable
    public static Worn worn(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer server)) {
            return null;
        }

        ItemStack stack = player.getItemInHand(hand);
        MemoryData data = SoulMemories.mine(player, stack);
        if (data == null) {
            if (SoulMemories.data(stack) != null && SoulMemories.data(stack).owned()) {
                SoulMemories.tell(server, "not_yours");
            }

            return null;
        }

        return new Worn(server, Soul.of(server), stack, data, hand == InteractionHand.MAIN_HAND ? Worn.Slot.MAIN_HAND : Worn.Slot.OFF_HAND);
    }

    /** Used in the air: the first enchantment that answers acts. */
    public static boolean use(Player player, InteractionHand hand) {
        Worn w = worn(player, hand);
        if (w == null) {
            return false;
        }

        for (String id : w.data().enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null && enchant.use(w, hand)) {
                return true;
            }
        }

        return false;
    }

    public static InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Worn w = worn(player, context.getHand());
        if (w == null) {
            return player.level().isClientSide && SoulMemories.data(context.getItemInHand()) != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        for (String id : w.data().enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null && enchant.useOn(w, context.getClickedPos(), context.getClickedFace())) {
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    /** Used on a creature: an enchantment's, or (crouching, on another Awakened) handing the Memory over. */
    public static InteractionResult useOnEntity(Player player, LivingEntity target, InteractionHand hand) {
        Worn w = worn(player, hand);
        if (w == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && target instanceof ServerPlayer taker) {
            return SoulMemories.bestow(w.player(), taker, w.data()) ? InteractionResult.CONSUME : InteractionResult.FAIL;
        }

        for (String id : w.data().enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null && enchant.useOnEntity(w, target)) {
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    /** The definition behind an item. */
    @Nullable
    public static MemoryDef def(String id) {
        return Memories.byId(id);
    }
}
