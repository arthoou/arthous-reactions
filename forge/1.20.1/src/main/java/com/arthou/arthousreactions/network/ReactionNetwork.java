package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.ArthousReactions;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ReactionNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static boolean registered;
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ArthousReactions.id("main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private ReactionNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        CHANNEL.registerMessage(packetId++, ServerboundSelectReactionPacket.class, ServerboundSelectReactionPacket::encode, ServerboundSelectReactionPacket::decode, ServerboundSelectReactionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(packetId++, ClientboundPlayReactionPacket.class, ClientboundPlayReactionPacket::encode, ClientboundPlayReactionPacket::decode, ClientboundPlayReactionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}

