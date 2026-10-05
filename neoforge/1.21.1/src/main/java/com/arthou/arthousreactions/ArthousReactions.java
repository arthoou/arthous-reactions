package com.arthou.arthousreactions;

import net.minecraft.resources.ResourceLocation;

public final class ArthousReactions {
    public static final String MOD_ID = "arthousreactions";

    private ArthousReactions() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
