package com.arthou.arthousreactions.reaction;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.resources.ResourceLocation;

public enum ReactionType {
    CHEER("cheer"),
    POUT("pout"),
    ANGRY("angry"),
    PIZZA_HI("pizza_hi"),
    PIZZA_CLAP("pizza_clap"),
    PIZZA_WOAH("pizza_woah"),
    PIZZA_LAUGH("pizza_laugh"),
    PIZZA_SAD("pizza_sad"),
    PIZZA_FEAR("pizza_fear"),
    PIZZA_EW("pizza_ew"),
    PIZZA_HEARTS("pizza_hearts"),
    PIZZA_ANGEL("pizza_angel"),
    PIZZA_67("pizza_67"),
    PIZZA_VINE_BOOM("pizza_vine_boom");

    public static final int TEXTURE_SIZE = 64;
    private static final ReactionType[] VALUES = values();

    private final String name;
    private final boolean emoji;
    private final ResourceLocation texture;

    ReactionType(String name) {
        this(name, true);
    }

    ReactionType(String name, boolean emoji) {
        this.name = name;
        this.emoji = emoji;
        this.texture = emoji ? ArthousReactions.id("textures/reactions/" + name + ".png") : null;
    }

    public int index() {
        return ordinal();
    }

    public String translationKey() {
        return "reaction." + ArthousReactions.MOD_ID + "." + name;
    }

    public boolean hasEmoji() {
        return emoji;
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
