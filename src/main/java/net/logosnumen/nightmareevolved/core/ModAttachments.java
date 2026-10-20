package net.logosnumen.nightmareevolved.core;

import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.CreatureProfile;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Data the mod keeps on entities: a player's soul, and a creature's Nightmare profile. */
public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NightmareEvolved.MOD_ID);
    public static final Supplier<AttachmentType<SoulData>> SOUL = ATTACHMENTS.register(
        "soul", () -> AttachmentType.serializable(SoulData::new).copyOnDeath().build()
    );
    public static final Supplier<AttachmentType<CreatureProfile>> CREATURE = ATTACHMENTS.register(
        "creature", () -> AttachmentType.serializable(CreatureProfile::new).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
