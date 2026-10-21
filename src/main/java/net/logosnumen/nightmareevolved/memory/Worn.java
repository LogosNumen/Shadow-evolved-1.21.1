package net.logosnumen.nightmareevolved.memory;

import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * A summoned Memory at work: whose it is, the item (for a worn Memory, the copy its soul keeps), what it is, and how it
 * is being carried. Its enchantments act through this.
 */
public record Worn(ServerPlayer player, SoulData soul, ItemStack stack, MemoryData data, Slot slot) {
    public enum Slot {
        MAIN_HAND,
        OFF_HAND,
        ARMOR,
        WORN,
        /** The Memory that loosed the projectile that just struck. */
        FIRED
    }

    public ServerLevel level() {
        return this.player.serverLevel();
    }

    /** How strongly its enchantments work: by its Rank and Tier, raised by a Dawn Shard nearby. */
    public double power() {
        return SoulMemories.power(this.player, this.data);
    }

    public boolean held() {
        return this.slot == Slot.MAIN_HAND || this.slot == Slot.OFF_HAND;
    }

    /** A blow struck in melee with this Memory in the main hand. */
    public boolean melee(DamageSource source) {
        return this.slot == Slot.MAIN_HAND && source.getDirectEntity() == this.player && !source.is(DamageTypeTags.IS_PROJECTILE);
    }

    /** A blow struck by something this Memory loosed. */
    public boolean shot() {
        return this.slot == Slot.FIRED;
    }

    /** A blow struck with this Memory, in hand or loosed from it. */
    public boolean struck(DamageSource source) {
        return this.melee(source) || this.shot();
    }

    public MemoryDef def() {
        return Memories.byId(this.data.def());
    }

    /** What the Memory remembers between uses: stored heat, a captured reflection, how many foes it has felled. */
    public CompoundTag state() {
        return this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public void setState(CompoundTag tag) {
        this.stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
