package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.util.Mth;

public record ActiveReaction(ReactionType type, long startedAtMs, int burstSeed) {
    public boolean isExpired(long now) {
        return now - startedAtMs > lifetimeMs();
    }

    public float normalizedAge(long now) {
        return Mth.clamp((now - startedAtMs) / (float) lifetimeMs(), 0.0F, 1.0F);
    }

    public float ageSeconds(long now) {
        return Math.max(0.0F, (now - startedAtMs) / 1000.0F);
    }

    private long lifetimeMs() {
        return Math.round(switch (type) {
            case CHEER -> 2.84F;
            case ANGRY -> 0.92F;
            case POUT -> 1.72F;
            case PIZZA_HI -> 1.75F;
            case PIZZA_SAD -> 2.35F;
            case PIZZA_LAUGH -> 1.45F;
            case PIZZA_CLAP -> 1.9F;
            case PIZZA_FEAR -> 1.25F;
            case PIZZA_WOAH -> 1.15F;
            case PIZZA_EW -> 1.05F;
            case PIZZA_ANGEL -> 2.2F;
            case PIZZA_HEARTS -> 1.85F;
            case PIZZA_67 -> 2.4F;
            case PIZZA_VINE_BOOM -> 1.2F;
        } * 1000.0F);
    }
}
