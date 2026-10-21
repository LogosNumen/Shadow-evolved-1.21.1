package net.logosnumen.nightmareevolved.entity.memory;

import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.memory.Worn;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A Memory thrown from the hand. Where it lands, its enchantments act; then it either flies back to the hand on its
 * unseen string (Prowling Thorn, whose string sometimes fails) or dissolves back into its owner's soul.
 */
public class ThrownMemoryEntity extends ThrowableItemProjectile {
    public enum Return {
        HAND,
        SOUL
    }

    private Return back = Return.SOUL;
    private boolean landed;

    public ThrownMemoryEntity(EntityType<? extends ThrownMemoryEntity> type, Level level) {
        super(type, level);
    }

    private ThrownMemoryEntity(LivingEntity thrower, Level level) {
        super(MemoryEntities.THROWN_MEMORY.get(), thrower, level);
    }

    /** Throws the Memory in the given hand. */
    public static void launch(Worn w, InteractionHand hand, float speed, Return back) {
        ServerPlayer player = w.player();
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return;
        }

        ThrownMemoryEntity thrown = new ThrownMemoryEntity(player, player.level());
        thrown.setItem(held.copy());
        thrown.back = back;
        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 0.5F);
        player.level().addFreshEntity(thrown);
        player.setItemInHand(hand, ItemStack.EMPTY);
        player.swing(hand, true);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STICK;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel && (this.tickCount % 20 == 0 && SoulMemories.checkLoose(this.getItem(), this) || this.tickCount > 100)) {
            if (!this.isRemoved()) {
                this.land(null, this.position());
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (result.getEntity() instanceof LivingEntity target && target != this.getOwner()) {
            this.land(target, result.getLocation());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.land(null, result.getLocation());
    }

    /** Lands: the enchantments act, then the Memory goes home. */
    private void land(@Nullable LivingEntity target, Vec3 at) {
        if (this.landed || !(this.level() instanceof ServerLevel level)) {
            return;
        }

        this.landed = true;
        ItemStack stack = this.getItem();
        MemoryData data = SoulMemories.data(stack);
        if (!(this.getOwner() instanceof ServerPlayer owner) || data == null || !owner.getUUID().equals(data.owner())) {
            level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, stack.copy()));
            this.discard();
            return;
        }

        Worn w = new Worn(owner, Soul.of(owner), stack, data, Worn.Slot.FIRED);
        for (String id : data.enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null) {
                enchant.thrownHit(w, target, at);
            }
        }

        if (this.back == Return.HAND && owner.level() == level && owner.distanceToSqr(this) < 48.0 * 48.0) {
            if (owner.getRandom().nextFloat() < 0.05F) {
                // The string is strong, but fickle.
                level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, stack.copy()));
                SoulMemories.tell(owner, "prowling_thorn.snapped");
            } else {
                Vfx.trail(level, at, Vfx.chest(owner), Vfx.Palette.ACID);
                if (owner.getMainHandItem().isEmpty()) {
                    owner.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                } else if (!owner.getInventory().add(stack.copy())) {
                    owner.drop(stack.copy(), false);
                }
            }

            this.discard();
            return;
        }

        SoulMemories.dismiss(owner, data.id(), -1, true);
        Vfx.burst(level, at, Vfx.Palette.SOUL, 8, 0.05);
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Back", this.back.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.back = "HAND".equals(tag.getString("Back")) ? Return.HAND : Return.SOUL;
    }
}
