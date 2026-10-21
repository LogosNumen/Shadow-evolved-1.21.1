package net.logosnumen.nightmareevolved.memory.item;

import java.util.List;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * An arrow Memory: carried in the inventory, any bow looses it, and it is never used up. Blood Arrow's are made of the
 * archer's blood; the Essence Quiver's of essence; Strike of Thunder flies straight and fast as lightning.
 */
public class MemoryArrowItem extends ArrowItem {
    public final String memory;

    public MemoryArrowItem(String memory, Item.Properties properties) {
        super(properties);
        this.memory = memory;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        AbstractArrow arrow = super.createArrow(level, ammo, shooter, weapon);
        MemoryData data = SoulMemories.data(ammo);
        if (data == null || !(shooter instanceof ServerPlayer player) || !player.getUUID().equals(data.owner())) {
            return arrow;
        }

        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        SoulMemories.markFired(arrow, data, true);
        arrow.setBaseDamage(arrow.getBaseDamage() + 0.5 * data.rank() + 0.2 * (data.tier() - 1));
        if (data.has(Enchant.RAIN_OF_BLOOD)) {
            if (player.getHealth() > 2.0F) {
                player.setHealth(player.getHealth() - 1.0F);
                arrow.setBaseDamage(arrow.getBaseDamage() + 2.0);
            } else {
                arrow.setBaseDamage(arrow.getBaseDamage() * 0.5);
            }
        }

        if (data.has(Enchant.ESSENCE_ARROWS) && !Soul.of(player).spend(1.0)) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 0.5);
        }

        if (data.has(Enchant.CAGED_LIGHTNING)) {
            arrow.setNoGravity(true);
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.5);
        }

        return arrow;
    }

    @Override
    public boolean isInfinite(ItemStack ammo, ItemStack bow, LivingEntity shooter) {
        MemoryData data = SoulMemories.data(ammo);
        return data != null && shooter.getUUID().equals(data.owner());
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
}
