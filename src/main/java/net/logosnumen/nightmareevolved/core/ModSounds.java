package net.logosnumen.nightmareevolved.core;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The mod's own sounds: the Soul Sea's low drone, and sinking into it and rising from it. */
public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, NightmareEvolved.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SEA_AMBIENT = sound("soul_sea.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SEA_ENTER = sound("soul_sea.enter");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SEA_EXIT = sound("soul_sea.exit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SEA_MEDITATE = sound("soul_sea.meditate");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(NightmareEvolved.id(name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
