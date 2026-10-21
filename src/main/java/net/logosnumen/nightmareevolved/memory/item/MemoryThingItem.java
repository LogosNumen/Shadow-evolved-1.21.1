package net.logosnumen.nightmareevolved.memory.item;

import java.util.List;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/**
 * A Memory that is neither weapon nor armour: a tool used from the hand (a bottle, a bell, a mirror, a lantern), a
 * charm or garment (worn without a slot once summoned; as an item it only appears loose or in the creative tab), or a
 * lineage Memory, consumed.
 */
public class MemoryThingItem extends Item {
    public final String memory;

    public MemoryThingItem(String memory, Item.Properties properties) {
        super(properties);
        this.memory = memory;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return SoulMemories.data(stack) != null ? InteractionResultHolder.success(stack) : InteractionResultHolder.pass(stack);
        }

        return MemoryItem.use(player, hand) ? InteractionResultHolder.consume(player.getItemInHand(hand)) : InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return MemoryItem.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return MemoryItem.useOnEntity(player, target, hand);
    }

    @Override
    public Component getName(ItemStack stack) {
        return MemoryItem.name(stack, super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        MemoryItem.tooltip(stack, lines);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        MemoryItem.tick(stack, holder);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return MemoryItem.entityTick(stack, entity);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }
}
