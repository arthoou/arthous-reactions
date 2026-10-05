package com.arthou.arthousreactions;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.arthou.arthousreactions.item.ReactionItems;
import com.arthou.arthousreactions.network.ReactionNetwork;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(ArthousReactions.MOD_ID)
public final class ArthousReactionsNeoForgeMod {
    public ArthousReactionsNeoForgeMod(IEventBus modBus) {
        ReactionItems.register(modBus);
        ReactionSoundEvents.register(modBus);
        modBus.addListener(ReactionNetwork::register);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ReactionClientBootstrap.init(modBus);
        }
    }
}
