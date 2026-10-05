package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.util.Mth;

public record ActiveReaction(ReactionType type, long startedAtMs, int burstSeed) {
    public static final long LIFETIME_MS = 2300L;

    public boolean isExpired(long now) {
        return now - startedAtMs > LIFETIME_MS;
    }

    public float normalizedAge(long now) {
        return Mth.clamp((now - startedAtMs) / (float) LIFETIME_MS, 0.0F, 1.0F);
    }
}
