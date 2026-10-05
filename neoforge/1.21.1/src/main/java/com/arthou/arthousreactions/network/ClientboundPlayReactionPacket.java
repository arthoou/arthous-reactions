package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.client.ReactionClientBootstrap;
import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientboundPlayReactionPacket(UUID playerId, int reactionIndex, int burstSeed) implements CustomPacketPayload {
    public static final Type<ClientboundPlayReactionPacket> TYPE = new Type<>(ArthousReactions.id("play_reaction"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundPlayReactionPacket> STREAM_CODEC = StreamCodec.of(
        (buffer, packet) -> {
            buffer.writeUUID(packet.playerId);
            buffer.writeVarInt(packet.reactionIndex);
            buffer.writeInt(packet.burstSeed);
        },
        buffer -> new ClientboundPlayReactionPacket(buffer.readUUID(), buffer.readVarInt(), buffer.readInt())
    );

    @Override
    public Type<ClientboundPlayReactionPacket> type() {
        return TYPE;
    }

    public static void handle(ClientboundPlayReactionPacket packet, IPayloadContext context) {
        ReactionType reaction = ReactionType.byIndex(packet.reactionIndex);
        if (reaction != null) {
            ReactionClientBootstrap.playReaction(packet.playerId, reaction, packet.burstSeed);
        }
    }
}
