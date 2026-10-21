package net.logosnumen.nightmareevolved.memory.item;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/**
 * A Memory bow. Its arrows strike harder for its Rank and Tier, and carry its enchantments; a bow that makes its own
 * arrows from its wielder's soul (Morgan's Warbow) needs no quiver, and can loose an obliterating shot.
 */
public class MemoryBowItem extends BowItem {
    /** Essence a soul arrow costs. */
    private static final double SOUL_ARROW = 2.0;
    public final String memory;

    public MemoryBowItem(String memory, Item.Properties properties) {
        super(properties);
        this.memory = memory;
    }

    private static boolean soulArrows(Player player, ItemStack bow) {
        MemoryData data = SoulMemories.mine(player, bow);
        return data != null && data.has(Enchant.SOUL_ARROWS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        if (player.getProjectile(bow).isEmpty() && soulArrows(player, bow)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bow);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!(user instanceof ServerPlayer player) || !player.getProjectile(stack).isEmpty() || !soulArrows(player, stack)) {
            super.releaseUsing(stack, level, user, timeLeft);
            return;
        }

        float power = getPowerForTime(this.getUseDuration(stack, user) - timeLeft);
        SoulData soul = Soul.of(player);
        if (power < 0.1F || !soul.spend(SOUL_ARROW)) {
            return;
        }

        ItemStack arrow = new ItemStack(Items.ARROW);
        arrow.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
        this.shoot((ServerLevel)level, player, player.getUsedItemHand(), stack, List.of(arrow), power * 3.0F, 1.0F, power == 1.0F, null);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.9F + power * 0.4F);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectile, ItemStack weapon) {
        MemoryData data = SoulMemories.data(weapon);
        if (data == null || !(arrow.getOwner() instanceof ServerPlayer player) || !player.getUUID().equals(data.owner())) {
            return arrow;
        }

        SoulMemories.markFired(arrow, data, false);
        arrow.setBaseDamage(arrow.getBaseDamage() * (1.0 + 0.15 * data.rank() + 0.03 * (data.tier() - 1)));
        if (data.has(Enchant.SOUL_ARROWS)) {
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }

        SoulData soul = Soul.of(player);
        if (data.has(Enchant.DEATH_DEALER) && player.isShiftKeyDown() && arrow.isCritArrow() && soul.spend(soul.maxEssence() * 0.25)) {
            arrow.setBaseDamage(arrow.getBaseDamage() * 4.0);
            arrow.getPersistentData().putBoolean("nightmare_evolved_obliterate", true);
            Vfx.flash(player.serverLevel(), player.getEyePosition(), Vfx.Palette.STEEL, 2.0F);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
        }

        return arrow;
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
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return MemoryItem.damage(stack, amount, entity);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }
}
