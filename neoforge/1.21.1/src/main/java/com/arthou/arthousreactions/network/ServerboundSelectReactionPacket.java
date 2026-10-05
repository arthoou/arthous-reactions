package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSelectReactionPacket(int reactionIndex, boolean byeSound) implements CustomPacketPayload {
    public static final Type<ServerboundSelectReactionPacket> TYPE = new Type<>(ArthousReactions.id("select_reaction"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSelectReactionPacket> STREAM_CODEC = StreamCodec.of(
        (buffer, packet) -> {
            buffer.writeVarInt(packet.reactionIndex);
            buffer.writeBoolean(packet.byeSound);
        },
        buffer -> new ServerboundSelectReactionPacket(buffer.readVarInt(), buffer.readBoolean())
    );

    @Override
    public Type<ServerboundSelectReactionPacket> type() {
        return TYPE;
    }

    public static void handle(ServerboundSelectReactionPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        ReactionType reaction = ReactionType.byIndex(packet.reactionIndex);
        if (reaction == null) {
            return;
        }

        ClientboundPlayReactionPacket playPacket = new ClientboundPlayReactionPacket(player.getUUID(), reaction.index(), player.getRandom().nextInt());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, playPacket);
        player.level().playSound(null, player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ(), packet.byeSound && reaction == ReactionType.PIZZA_HI ? ReactionSoundEvents.bye() : ReactionSoundEvents.forReaction(reaction), SoundSource.PLAYERS, ReactionSoundEvents.volumeForReaction(reaction), 0.96F + player.level().random.nextFloat() * 0.08F);
    }
}
