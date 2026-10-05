package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public final class ClientboundPlayReactionPacket {
    private final UUID playerId;
    private final int reactionIndex;
    private final int burstSeed;

    public ClientboundPlayReactionPacket(UUID playerId, int reactionIndex, int burstSeed) {
        this.playerId = playerId;
        this.reactionIndex = reactionIndex;
        this.burstSeed = burstSeed;
    }

    public static void encode(ClientboundPlayReactionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeVarInt(packet.reactionIndex);
        buffer.writeInt(packet.burstSeed);
    }

    public static ClientboundPlayReactionPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundPlayReactionPacket(buffer.readUUID(), buffer.readVarInt(), buffer.readInt());
    }

    public static void handle(ClientboundPlayReactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ReactionType reaction = ReactionType.byIndex(packet.reactionIndex);
            if (reaction != null) {
                ReactionClientBootstrap.playReaction(packet.playerId, reaction, packet.burstSeed);
            }
        });
        context.setPacketHandled(true);
    }
}
