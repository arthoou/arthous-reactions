package com.arthou.arthousreactions.sound;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReactionSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ArthousReactions.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> REACTION_POP = SOUND_EVENTS.register("reaction_pop", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("reaction_pop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEER_POP = SOUND_EVENTS.register("cheer_pop", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("cheer_pop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGRY_ROBLOX = SOUND_EVENTS.register("angry_roblox", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("angry_roblox")));
    public static final DeferredHolder<SoundEvent, SoundEvent> POUT_STOMP = SOUND_EVENTS.register("pout_stomp", () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id("pout_stomp")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_HI = pizza("pizza_hi");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_SAD = pizza("pizza_sad");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_LAUGH = pizza("pizza_laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_FEAR = pizza("pizza_fear");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_WOAH = pizza("pizza_woah");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_EW = pizza("pizza_ew");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_CLAP = pizza("pizza_clap");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_67 = pizza("pizza_67");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_BYE = pizza("pizza_bye");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIZZA_VINE_BOOM = pizza("pizza_vine_boom");

    private ReactionSoundEvents() {
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }

    public static SoundEvent forReaction(ReactionType reaction) {
        if (reaction == ReactionType.CHEER) {
            return CHEER_POP.get();
        }
        if (reaction == ReactionType.ANGRY) {
            return ANGRY_ROBLOX.get();
        }
        if (reaction == ReactionType.POUT) {
            return POUT_STOMP.get();
        }
        return switch (reaction) {
            case PIZZA_HI -> PIZZA_HI.get();
            case PIZZA_SAD -> PIZZA_SAD.get();
            case PIZZA_LAUGH -> PIZZA_LAUGH.get();
            case PIZZA_FEAR -> PIZZA_FEAR.get();
            case PIZZA_WOAH -> PIZZA_WOAH.get();
            case PIZZA_EW -> PIZZA_EW.get();
            case PIZZA_CLAP -> PIZZA_CLAP.get();
            case PIZZA_67 -> PIZZA_67.get();
            case PIZZA_VINE_BOOM -> PIZZA_VINE_BOOM.get();
            default -> REACTION_POP.get();
        };
    }

    public static float volumeForReaction(ReactionType reaction) {
        return reaction == ReactionType.PIZZA_VINE_BOOM ? 0.38F : 0.85F;
    }

    public static SoundEvent bye() {
        return PIZZA_BYE.get();
    }

    private static DeferredHolder<SoundEvent, SoundEvent> pizza(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ArthousReactions.id(name)));
    }
}
