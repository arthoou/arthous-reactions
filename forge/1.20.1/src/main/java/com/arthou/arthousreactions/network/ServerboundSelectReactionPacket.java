package com.arthou.arthousreactions.network;

import com.arthou.arthousreactions.reaction.ReactionType;
import com.arthou.arthousreactions.sound.ReactionSoundEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public final class ServerboundSelectReactionPacket {
    private final int reactionIndex;

    public ServerboundSelectReactionPacket(int reactionIndex) {
        this.reactionIndex = reactionIndex;
    }

    public static void encode(ServerboundSelectReactionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.reactionIndex);
    }

    public static ServerboundSelectReactionPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundSelectReactionPacket(buffer.readVarInt());
    }

    public static void handle(ServerboundSelectReactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ReactionType reaction = ReactionType.byIndex(packet.reactionIndex);
            if (player == null || reaction == null) {
                return;
            }

            ReactionNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ClientboundPlayReactionPacket(player.getUUID(), reaction.index(), player.getRandom().nextInt()));
            player.level().playSound(null, player.getX(), player.getY() + player.getBbHeight() * 0.7D, player.getZ(), ReactionSoundEvents.REACTION_POP.get(), SoundSource.PLAYERS, 0.75F, 0.96F + player.level().random.nextFloat() * 0.08F);
        });
        context.setPacketHandled(true);
    }
}

