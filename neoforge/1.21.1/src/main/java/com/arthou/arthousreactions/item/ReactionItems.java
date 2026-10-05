package com.arthou.arthousreactions.item;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReactionItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArthousReactions.MOD_ID);

    public static final DeferredHolder<Item, Item> ANGEL_HALO = ITEMS.register("angel_halo", () -> new Item(new Item.Properties()));

    private ReactionItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
