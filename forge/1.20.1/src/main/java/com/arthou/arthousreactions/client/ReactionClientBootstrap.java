package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.network.ReactionNetwork;
import com.arthou.arthousreactions.network.ServerboundSelectReactionPacket;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReactionClientBootstrap {
    private static final Map<UUID, ActiveReaction> ACTIVE_REACTIONS = new ConcurrentHashMap<>();
    private static boolean initialized;
    private static KeyMapping openWheelKey;

    private ReactionClientBootstrap() {
    }

    public static void init(IEventBus modBus) {
        if (initialized) {
            return;
        }

        initialized = true;
        openWheelKey = new KeyMapping("key.arthousreactions.open_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.arthousreactions");
        modBus.addListener(ReactionClientBootstrap::onRegisterKeyMappings);

        MinecraftForge.EVENT_BUS.addListener(ReactionClientBootstrap::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ReactionClientBootstrap::onRenderPlayer);
        MinecraftForge.EVENT_BUS.addListener(ReactionClientBootstrap::onClientLeave);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openWheelKey);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        long now = Util.getMillis();
        ACTIVE_REACTIONS.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        while (openWheelKey.consumeClick()) {
            minecraft.setScreen(new ReactionWheelScreen());
        }
    }

    private static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        ActiveReaction activeReaction = ACTIVE_REACTIONS.get(player.getUUID());
        if (activeReaction == null) {
            return;
        }

        renderBurst(player, activeReaction, event.getPoseStack(), event.getMultiBufferSource());
    }

    private static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE_REACTIONS.clear();
    }

    public static void playReaction(UUID playerId, ReactionType reaction, int burstSeed) {
        Minecraft.getInstance().execute(() -> ACTIVE_REACTIONS.put(playerId, new ActiveReaction(reaction, Util.getMillis(), burstSeed)));
    }

    public static void sendReaction(ReactionType reaction) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        ReactionNetwork.CHANNEL.sendToServer(new ServerboundSelectReactionPacket(reaction.index()));
    }

    public static KeyMapping openWheelKey() {
        return openWheelKey;
    }

    private static void renderBurst(Player player, ActiveReaction activeReaction, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        long now = Util.getMillis();
        float progress = activeReaction.normalizedAge(now);
        float easedRise = progress * progress * (3.0F - 2.0F * progress);
        float startY = player.getBbHeight() * (player.isShiftKeyDown() ? 0.7F : 0.76F);
        float endY = player.getBbHeight() + (player.isShiftKeyDown() ? 0.56F : 0.72F);
        float yOffset = startY + (endY - startY) * easedRise;
        float pathAngle = (float) Math.toRadians(Math.floorMod(activeReaction.burstSeed() * 37, 360));
        float pathDistance = 0.65F + (Math.floorMod(activeReaction.burstSeed() >>> 3, 100) / 100.0F) * 0.55F;
        float driftX = (float) Math.cos(pathAngle) * pathDistance;
        float driftZ = (float) Math.sin(pathAngle) * pathDistance;
        float wobbleX = (float) Math.sin(progress * 6.6F + pathAngle * 1.3F) * 0.18F;
        float wobbleZ = (float) Math.cos(progress * 5.8F + pathAngle * 1.7F) * 0.18F;
        float xOffset = driftX * progress + wobbleX * (1.0F - progress * 0.35F);
        float zOffset = driftZ * progress + wobbleZ * (1.0F - progress * 0.35F);
        float horizontalDistance = (float) Math.sqrt(xOffset * xOffset + zOffset * zOffset);
        if (horizontalDistance > 1.5F) {
            float clampScale = 1.5F / horizontalDistance;
            xOffset *= clampScale;
            zOffset *= clampScale;
        }
        float scale = 0.42F - progress * 0.06F;
        float fade = progress < 0.72F ? 1.0F : 1.0F - ((progress - 0.72F) / 0.28F);
        int alpha = Math.max(0, Math.round(255.0F * fade * fade));

        if (alpha <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, zOffset);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-scale, -scale, scale);
        renderIconQuad(poseStack, bufferSource, activeReaction.type(), alpha);
        poseStack.popPose();
    }

    private static void renderIconQuad(PoseStack poseStack, MultiBufferSource bufferSource, ReactionType reaction, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(reaction.texture()));
        int light = LightTexture.FULL_BRIGHT;

        consumer.vertex(matrix, -0.5F, 0.5F, 0.0F).color(255, 255, 255, alpha).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, 0.5F, 0.5F, 0.0F).color(255, 255, 255, alpha).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, 0.5F, -0.5F, 0.0F).color(255, 255, 255, alpha).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix, -0.5F, -0.5F, 0.0F).color(255, 255, 255, alpha).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
    }
}
