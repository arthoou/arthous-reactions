package com.arthou.arthousreactions.sound;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ReactionSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ArthousReactions.MOD_ID);

    public static final RegistryObject<SoundEvent> REACTION_POP = SOUND_EVENTS.register("reaction_pop", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("reaction_pop")));

    private ReactionSoundEvents() {
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}

