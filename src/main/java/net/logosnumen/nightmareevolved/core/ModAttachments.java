package net.logosnumen.nightmareevolved.core;

import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.CreatureProfile;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data the mod keeps on entities: a player's soul, a creature's Nightmare profile, whom a creature is bound to, and
 * what afflicts it.
 */
public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NightmareEvolved.MOD_ID);
    public static final Supplier<AttachmentType<SoulData>> SOUL = ATTACHMENTS.register(
        "soul", () -> AttachmentType.serializable(SoulData::new).copyOnDeath().build()
    );
    public static final Supplier<AttachmentType<CreatureProfile>> CREATURE = ATTACHMENTS.register(
        "creature", () -> AttachmentType.serializable(CreatureProfile::new).build()
    );
    /** A creature bound to a player: summoned, raised as a puppet, or held by a stolen will. */
    public static final Supplier<AttachmentType<CompoundTag>> BOUND = ATTACHMENTS.register(
        "bound", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).build()
    );
    /** Lasting harm and marks an ability left on a creature: bleeding, decay, festering, weak points. */
    public static final Supplier<AttachmentType<CompoundTag>> AFFLICTION = ATTACHMENTS.register(
        "affliction", () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
