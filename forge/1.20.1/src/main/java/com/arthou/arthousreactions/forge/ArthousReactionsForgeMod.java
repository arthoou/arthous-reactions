package com.arthou.arthousreactions.forge;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.arthou.arthousreactions.network.ReactionNetwork;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArthousReactions.MOD_ID)
public final class ArthousReactionsForgeMod {
    public ArthousReactionsForgeMod() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ReactionSoundEvents.register(modBus);
        modBus.addListener(this::onCommonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ReactionClientBootstrap.init(modBus));
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ReactionNetwork::register);
    }
}
