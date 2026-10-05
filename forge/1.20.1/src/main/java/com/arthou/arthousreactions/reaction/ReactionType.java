package com.arthou.arthousreactions.reaction;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.resources.ResourceLocation;

public enum ReactionType {
    TEARY("teary"),
    HAPPY("happy"),
    GRIN("grin"),
    LAUGH("laugh"),
    BIG_GRIN("big_grin"),
    SQUEEZE_LAUGH("squeeze_laugh"),
    RELIEF("relief"),
    CRYING_LAUGH("crying_laugh"),
    WINK("wink"),
    BLUSH("blush"),
    GOOFY_LAUGH("goofy_laugh"),
    HALO("halo"),
    HEARTS("hearts"),
    LOVE_EYES("love_eyes"),
    STAR_EYES("star_eyes"),
    ANGRY("angry"),
    SLEEPY("sleepy"),
    DROOL("drool"),
    MASK("mask"),
    WORRIED("worried"),
    BANDAGED("bandaged"),
    NAUSEATED("nauseated"),
    VOMITING("vomiting"),
    SNEEZE("sneeze"),
    DIZZY("dizzy"),
    SPIRAL("spiral"),
    PARTY("party"),
    SUNGLASSES("sunglasses"),
    SHOCK("shock"),
    NERD("nerd"),
    CRY("cry"),
    NEUTRAL("neutral"),
    SAD("sad"),
    COLD_SWEAT("cold_sweat"),
    HOT("hot"),
    KISS("kiss");

    public static final int TEXTURE_SIZE = 64;
    private static final ReactionType[] VALUES = values();

    private final String name;
    private final ResourceLocation texture;

    ReactionType(String name) {
        this.name = name;
        this.texture = ArthousReactions.id("textures/reactions/" + name + ".png");
    }

    public int index() {
        return ordinal();
    }

    public String translationKey() {
        return "reaction." + ArthousReactions.MOD_ID + "." + name;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public static ReactionType byIndex(int index) {
        if (index < 0 || index >= VALUES.length) {
            return null;
        }
        return VALUES[index];
    }
}
