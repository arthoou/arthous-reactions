package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.ArthousReactions;
import com.arthou.arthousreactions.item.ReactionItems;
import com.arthou.arthousreactions.network.ServerboundSelectReactionPacket;
import com.arthou.arthousreactions.reaction.ReactionType;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReactionClientBootstrap {
    private static final ResourceLocation CREATE_CONFETTI = ArthousReactions.id("textures/effect/create_confetti.png");
    private static final ResourceLocation ANGRY_SMOKE = ArthousReactions.id("textures/effect/angry_smoke.png");
    private static final ResourceLocation STOMP_DUST = ArthousReactions.id("textures/effect/stomp_dust.png");
    private static final ResourceLocation HEART_PARTICLE = ArthousReactions.id("textures/effect/heart_particle.png");
    private static final float POUT_POSE_END_SECONDS = 1.72F;
    private static final float[] PIZZA_CLAP_IMPACTS = {0.04F, 0.305F, 0.6F, 0.91F, 1.195F, 1.49F, 1.78F};
    private static final int[] CONFETTI_COLORS = {0xFFFF2F4F, 0xFFFFD400, 0xFF00E0B8, 0xFF008CFF, 0xFF9C27FF, 0xFFFF7A00, 0xFFFF4FC3};
    private static final float CHEER_POSE_END_SECONDS = 2.84F;
    private static final PoseKeyframe[] CHEER_KEYFRAMES = {
        new PoseKeyframe(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.08F, 0.0F, 0.0F, -0.95F, 0.02F, -0.04F, -0.95F, -0.02F, 0.04F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.18F, 0.0F, 0.0F, -2.35F, 0.08F, -0.12F, -2.35F, -0.08F, 0.12F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.34F, 0.0F, 0.0F, -3.02F, 0.16F, -0.22F, -3.02F, -0.16F, 0.22F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(0.72F, 0.0F, 0.0F, -2.96F, 0.2F, -0.26F, -3.06F, -0.12F, 0.18F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(1.12F, 0.0F, 0.0F, -3.06F, 0.12F, -0.18F, -2.96F, -0.2F, 0.26F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(1.6F, 0.0F, 0.0F, -3.0F, 0.18F, -0.24F, -3.0F, -0.18F, 0.24F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(2.24F, 0.0F, 0.0F, -3.0F, 0.15F, -0.22F, -3.0F, -0.15F, 0.22F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(2.62F, 0.0F, 0.0F, -1.35F, 0.04F, -0.08F, -1.35F, -0.04F, 0.08F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
        new PoseKeyframe(2.84F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
    };
    private static final Map<UUID, ActiveReaction> ACTIVE_REACTIONS = new ConcurrentHashMap<>();
    private static boolean initialized;
    private static KeyMapping openWheelKey;
    private static CameraType previousCameraType;

    private ReactionClientBootstrap() {
    }

    public static void init(IEventBus modBus) {
        if (initialized) {
            return;
        }

        initialized = true;
        openWheelKey = new KeyMapping("key.arthousreactions.open_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.arthousreactions");
        modBus.addListener(ReactionClientBootstrap::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener(ReactionClientBootstrap::onClientTick);
        NeoForge.EVENT_BUS.addListener(ReactionClientBootstrap::onRenderPlayerPost);
        NeoForge.EVENT_BUS.addListener(ReactionClientBootstrap::onClientLeave);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openWheelKey);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        long now = Util.getMillis();
        ACTIVE_REACTIONS.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

        Minecraft minecraft = Minecraft.getInstance();
        updateCheerCamera(minecraft);
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        if (minecraft.player.isShiftKeyDown() || minecraft.player.isCrouching()) {
            cancelReaction(minecraft.player);
        }

        while (openWheelKey.consumeClick()) {
            minecraft.setScreen(new ReactionWheelScreen());
        }
    }

    private static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        ActiveReaction activeReaction = ACTIVE_REACTIONS.get(player.getUUID());
        if (activeReaction != null) {
            if (player.isShiftKeyDown() || player.isCrouching()) {
                cancelReaction(player);
                return;
            }

            renderBurst(player, event.getRenderer().getModel(), activeReaction, event.getPoseStack(), event.getMultiBufferSource());
        }
    }

    private static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE_REACTIONS.clear();
        restoreCheerCamera(Minecraft.getInstance());
    }

    public static void playReaction(UUID playerId, ReactionType reaction, int burstSeed) {
        Minecraft.getInstance().execute(() -> {
            ACTIVE_REACTIONS.put(playerId, new ActiveReaction(reaction, Util.getMillis(), burstSeed));
            updateCheerCamera(Minecraft.getInstance());
        });
    }

    public static void sendReaction(ReactionType reaction) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        PacketDistributor.sendToServer(new ServerboundSelectReactionPacket(reaction.index(), false));
    }

    public static void sendReactionWithByeSound(ReactionType reaction) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        PacketDistributor.sendToServer(new ServerboundSelectReactionPacket(reaction.index(), true));
    }

    public static KeyMapping openWheelKey() {
        return openWheelKey;
    }

    private static void updateCheerCamera(Minecraft minecraft) {
        if (minecraft.player == null) {
            restoreCheerCamera(minecraft);
            return;
        }

        boolean localPlayerReacting = ACTIVE_REACTIONS.containsKey(minecraft.player.getUUID());
        if (localPlayerReacting && minecraft.options.getCameraType().isFirstPerson()) {
            previousCameraType = minecraft.options.getCameraType();
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (!localPlayerReacting) {
            restoreCheerCamera(minecraft);
        }
    }

    private static void restoreCheerCamera(Minecraft minecraft) {
        if (previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
    }

    public static void applyCheerPoseToModel(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model) {
        ActiveReaction activeReaction = ACTIVE_REACTIONS.get(player.getUUID());
        if (activeReaction == null) {
            return;
        }

        if (player.isShiftKeyDown() || player.isCrouching()) {
            cancelReaction(player);
            return;
        }

        ignoreCrouchPose(model);

        if (activeReaction.type() == ReactionType.ANGRY) {
            applyAngryPose(activeReaction, model);
            syncWearLayers(model);
            return;
        }

        if (activeReaction.type() == ReactionType.POUT) {
            applyPoutPose(activeReaction, model);
            syncWearLayers(model);
            return;
        }

        if (isPizzaEmote(activeReaction.type())) {
            applyPizzaEmotePose(player, activeReaction, model);
            syncWearLayers(model);
            return;
        }

        if (activeReaction.type() != ReactionType.CHEER) {
            return;
        }

        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        if (ageSeconds > CHEER_POSE_END_SECONDS) {
            return;
        }

        PoseKeyframe pose = sampleCheerPose(ageSeconds);

        model.rightArm.xRot = pose.rightArmXRot;
        model.rightArm.yRot = pose.rightArmYRot;
        model.rightArm.zRot = pose.rightArmZRot;
        model.leftArm.xRot = pose.leftArmXRot;
        model.leftArm.yRot = pose.leftArmYRot;
        model.leftArm.zRot = pose.leftArmZRot;

        syncWearLayers(model);
    }

    private static void cancelReaction(Player player) {
        if (ACTIVE_REACTIONS.remove(player.getUUID()) != null && player == Minecraft.getInstance().player) {
            restoreCheerCamera(Minecraft.getInstance());
        }
    }

    private static void syncWearLayers(PlayerModel<AbstractClientPlayer> model) {
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void renderBurst(Player player, PlayerModel<AbstractClientPlayer> model, ActiveReaction activeReaction, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());

        if (activeReaction.type() == ReactionType.CHEER) {
            renderConfetti(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (activeReaction.type() == ReactionType.ANGRY) {
            renderAngrySmoke(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (activeReaction.type() == ReactionType.POUT) {
            renderPoutDust(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (activeReaction.type() == ReactionType.PIZZA_ANGEL) {
            renderAngelEffect(player, model, activeReaction, ageSeconds, poseStack, bufferSource);
            return;
        }

        if (activeReaction.type() == ReactionType.PIZZA_HEARTS) {
            renderHeartBurst(activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
            return;
        }

        if (isPizzaEmote(activeReaction.type())) {
            return;
        }

        if (activeReaction.type().hasEmoji()) {
            renderEmoji(player, activeReaction, ageSeconds, poseStack, bufferSource, dispatcher);
        }
    }

    private static void renderEmoji(Player player, ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        float progress = activeReaction.normalizedAge(Util.getMillis());
        float yOffset = player.getBbHeight() + 0.52F + Mth.sin(Mth.clamp(progress * 1.2F, 0.0F, 1.0F) * Mth.PI) * 0.28F;
        float alphaFade = progress < 0.78F ? 1.0F : 1.0F - ((progress - 0.78F) / 0.22F);
        int alpha = Math.max(0, Math.round(255.0F * alphaFade * alphaFade));
        if (alpha <= 0) {
            return;
        }

        float xShake = 0.0F;
        float zShake = 0.0F;
        float rotation = 0.0F;
        float scale = 0.46F;
        poseStack.pushPose();
        poseStack.translate(xShake, yOffset, zShake);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotation(rotation));
        poseStack.scale(-scale, -scale, scale);
        renderIconQuad(poseStack, bufferSource, activeReaction.type(), alpha);
        poseStack.popPose();
    }

    private static void renderAngrySmoke(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(ANGRY_SMOKE));
        int light = LightTexture.FULL_BRIGHT;
        int seed = activeReaction.burstSeed();
        float progress = activeReaction.normalizedAge(Util.getMillis());
        float fade = progress < 0.82F ? 1.0F : 1.0F - ((progress - 0.82F) / 0.18F);
        int alphaBase = Math.max(0, Math.round(210.0F * fade));

        for (int i = 0; i < 10; i++) {
            float delay = pseudo(seed, i, 9) * 0.8F;
            float t = (ageSeconds + delay) % 1.1F;
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (0.26F + pseudo(seed, i, 0) * 0.15F) + Mth.sin(ageSeconds * 8.0F + i) * 0.025F;
            float y = 1.46F + t * 0.52F + pseudo(seed, i, 1) * 0.16F;
            float z = (pseudo(seed, i, 2) - 0.5F) * 0.28F;
            float scale = 0.13F + t * 0.08F;
            int alpha = Math.max(0, Math.round(alphaBase * (1.0F - t / 1.1F)));
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(Mth.sin(ageSeconds * 4.0F + i) * 0.18F));
            poseStack.scale(-scale, -scale, scale);
            drawTexturedQuad(poseStack, consumer, light, alpha);
            poseStack.popPose();
        }
    }

    private static void applyPoutPose(ActiveReaction activeReaction, PlayerModel<AbstractClientPlayer> model) {
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        if (ageSeconds > POUT_POSE_END_SECONDS) {
            return;
        }

        float windup = smoothPulse(ageSeconds, 0.04F, 0.22F);
        float lift = smoothPulse(ageSeconds, 0.12F, 0.42F);
        float stomp = smoothPulse(ageSeconds, 0.42F, 0.56F);
        float bounce = smoothPulse(ageSeconds, 0.56F, 0.68F) * (1.0F - smoothPulse(ageSeconds, 0.68F, 0.92F));
        float settle = 1.0F - smoothPulse(ageSeconds, 1.34F, POUT_POSE_END_SECONDS);
        float attitude = windup * settle;
        float tantrumShake = Mth.sin(ageSeconds * 38.0F) * 0.045F * attitude;

        model.head.yRot = 1.08F * attitude + tantrumShake;
        model.head.xRot = -0.5F * attitude;
        model.head.zRot = -0.12F * attitude;
        model.body.yRot = 0.28F * attitude;
        model.body.xRot = (-0.08F * attitude) - (0.08F * bounce);
        model.rightLeg.xRot = Mth.lerp(stomp, -1.42F * lift, 0.68F) * settle;
        model.rightLeg.yRot = -0.16F * attitude;
        model.rightLeg.zRot = 0.08F * bounce;
        model.leftLeg.xRot = (-0.14F * attitude) - (0.12F * bounce);
        model.leftLeg.zRot = -0.08F * attitude;
        model.rightArm.xRot = (-0.56F * attitude) + Mth.sin(ageSeconds * 20.0F) * 0.055F * attitude;
        model.leftArm.xRot = (-0.56F * attitude) - Mth.sin(ageSeconds * 20.0F) * 0.055F * attitude;
        model.rightArm.zRot = 0.48F * attitude;
        model.leftArm.zRot = -0.48F * attitude;

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void ignoreCrouchPose(PlayerModel<AbstractClientPlayer> model) {
        model.crouching = false;
        model.head.y = 0.0F;
        model.hat.copyFrom(model.head);
        model.body.y = 0.0F;
        model.jacket.copyFrom(model.body);
        model.rightArm.y = 2.0F;
        model.leftArm.y = 2.0F;
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightLeg.y = 12.0F;
        model.leftLeg.y = 12.0F;
        model.rightLeg.z = 0.1F;
        model.leftLeg.z = 0.1F;
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }

    private static void lookAtNearestPlayer(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, float weight) {
        if (player.clientLevel == null) {
            return;
        }

        AbstractClientPlayer nearestPlayer = null;
        double nearestDistance = 100.0D;
        for (AbstractClientPlayer otherPlayer : player.clientLevel.players()) {
            if (otherPlayer == player || otherPlayer.isInvisible()) {
                continue;
            }

            double distance = player.distanceToSqr(otherPlayer);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = otherPlayer;
            }
        }

        if (nearestPlayer == null) {
            return;
        }

        double dx = nearestPlayer.getX() - player.getX();
        double dz = nearestPlayer.getZ() - player.getZ();
        double dy = nearestPlayer.getEyeY() - player.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F - player.yBodyRot) * Mth.DEG_TO_RAD;
        float targetPitch = (float) (-Mth.atan2(dy, horizontal)) * Mth.DEG_TO_RAD;

        model.head.yRot = Mth.clamp(targetYaw, -1.15F, 1.15F) * weight;
        model.head.xRot = Mth.clamp(targetPitch, -0.62F, 0.62F) * weight;
    }

    private static void applyAngryPose(ActiveReaction activeReaction, PlayerModel<AbstractClientPlayer> model) {
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        float duration = 0.92F;
        if (ageSeconds > duration) {
            return;
        }

        float in = smoothPulse(ageSeconds, 0.0F, 0.14F);
        float out = 1.0F - smoothPulse(ageSeconds, duration - 0.26F, duration);
        float weight = in * out;
        float shake = Mth.sin(ageSeconds * 22.0F) * weight;

        model.head.xRot = 0.18F * weight;
        model.head.yRot = shake * 0.1F;
        model.rightArm.xRot = -0.32F * weight;
        model.leftArm.xRot = -0.32F * weight;
        model.rightArm.zRot = 0.32F * weight;
        model.leftArm.zRot = -0.32F * weight;

        model.hat.copyFrom(model.head);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void applyPizzaEmotePose(AbstractClientPlayer player, ActiveReaction activeReaction, PlayerModel<AbstractClientPlayer> model) {
        float ageSeconds = activeReaction.ageSeconds(Util.getMillis());
        float duration = pizzaPoseDuration(activeReaction.type());
        if (ageSeconds > duration) {
            return;
        }

        float in = smoothPulse(ageSeconds, 0.0F, 0.18F);
        float out = 1.0F - smoothPulse(ageSeconds, duration - 0.28F, duration);
        float weight = in * out;
        float bounce = Mth.sin(ageSeconds * 10.0F) * 0.12F * weight;
        float quick = Mth.sin(ageSeconds * 22.0F) * weight;

        switch (activeReaction.type()) {
            case PIZZA_HI -> {
                model.rightArm.xRot = -1.75F * weight;
                model.rightArm.zRot = 0.55F * weight + Mth.sin(ageSeconds * 14.0F) * 0.18F * weight;
                model.leftArm.xRot = -0.08F * weight;
                model.head.yRot = Mth.sin(ageSeconds * 5.0F) * 0.16F * weight;
            }
            case PIZZA_SAD -> {
                float sob = Mth.sin(ageSeconds * 18.0F) * 0.035F * weight;
                model.head.xRot = 0.52F * weight + sob;
                model.head.yRot = Mth.sin(ageSeconds * 8.0F) * 0.035F * weight;
                model.rightArm.xRot = -1.08F * weight;
                model.leftArm.xRot = -1.08F * weight;
                model.rightArm.yRot = -0.2F * weight;
                model.leftArm.yRot = 0.2F * weight;
                model.rightArm.zRot = 0.16F * weight;
                model.leftArm.zRot = -0.16F * weight;
            }
            case PIZZA_LAUGH -> {
                float laughNod = Mth.sin(ageSeconds * 9.0F) * 0.16F * weight;
                model.head.xRot = (-0.18F + laughNod) * weight;
                model.rightArm.xRot = -1.05F * weight;
                model.rightArm.yRot = -0.42F * weight;
                model.rightArm.zRot = 0.32F * weight;
                model.leftArm.xRot = -1.42F * weight;
                model.leftArm.yRot = 0.36F * weight;
                model.leftArm.zRot = -0.52F * weight;
            }
            case PIZZA_FEAR -> {
                model.head.xRot = 0.18F * weight;
                model.head.yRot = quick * 0.12F;
                model.rightArm.xRot = -1.45F * weight;
                model.leftArm.xRot = -1.45F * weight;
                model.rightArm.zRot = 0.72F * weight;
                model.leftArm.zRot = -0.72F * weight;
            }
            case PIZZA_WOAH -> {
                float surprise = smoothPulse(ageSeconds, 0.02F, 0.22F) * out;
                float settle = 1.0F - smoothPulse(ageSeconds, 0.68F, duration);
                model.head.xRot = -0.3F * surprise;
                model.head.yRot = Mth.sin(ageSeconds * 7.0F) * 0.045F * surprise;
                model.rightArm.xRot = -1.34F * surprise;
                model.leftArm.xRot = -1.34F * surprise;
                model.rightArm.yRot = -0.26F * surprise;
                model.leftArm.yRot = 0.26F * surprise;
                model.rightArm.zRot = 0.46F * surprise * settle;
                model.leftArm.zRot = -0.46F * surprise * settle;
                model.rightLeg.xRot = -0.04F * surprise;
                model.leftLeg.xRot = 0.04F * surprise;
            }
            case PIZZA_EW -> {
                float recoil = smoothPulse(ageSeconds, 0.02F, 0.24F) * out;
                float shiver = Mth.sin(ageSeconds * 18.0F) * 0.025F * recoil;
                model.head.xRot = -0.18F * recoil;
                model.head.yRot = -0.36F * recoil + shiver;
                model.body.xRot = -0.1F * recoil;
                model.body.yRot = -0.12F * recoil;
                model.rightArm.xRot = -1.34F * recoil;
                model.rightArm.yRot = -0.18F * recoil;
                model.rightArm.zRot = 0.18F * recoil;
                model.leftArm.xRot = -0.42F * recoil;
                model.leftArm.yRot = 0.34F * recoil;
                model.leftArm.zRot = -0.52F * recoil;
            }
            case PIZZA_ANGEL -> {
                float floaty = Mth.sin(ageSeconds * 5.0F) * 0.05F * weight;
                model.head.xRot = -0.08F * weight + floaty;
                model.rightArm.xRot = -0.36F * weight;
                model.leftArm.xRot = -0.36F * weight;
                model.rightArm.zRot = 0.24F * weight;
                model.leftArm.zRot = -0.24F * weight;
            }
            case PIZZA_HEARTS -> {
            }
            case PIZZA_67 -> {
                float armWave = Mth.sin(ageSeconds * 7.0F) * 0.55F * weight;
                model.rightArm.xRot = (-1.08F + armWave) * weight;
                model.leftArm.xRot = (-1.08F - armWave) * weight;
                model.rightArm.yRot = -0.2F * weight;
                model.leftArm.yRot = 0.2F * weight;
                model.rightArm.zRot = 0.16F * weight;
                model.leftArm.zRot = -0.16F * weight;
            }
            case PIZZA_VINE_BOOM -> {
                lookAtNearestPlayer(player, model, weight);
            }
            case PIZZA_CLAP -> {
                float clap = clapImpact(ageSeconds);
                float open = 1.0F - clap;
                model.rightArm.xRot = -1.08F * weight;
                model.leftArm.xRot = -1.08F * weight;
                model.rightArm.yRot = -(0.36F + 0.42F * clap - 0.12F * open) * weight;
                model.leftArm.yRot = (0.36F + 0.42F * clap - 0.12F * open) * weight;
                model.rightArm.zRot = 0.08F * open * weight;
                model.leftArm.zRot = -0.08F * open * weight;
            }
            default -> {
            }
        }

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    private static void renderPoutDust(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        float impactAge = ageSeconds - 0.52F;
        if (impactAge < 0.0F || impactAge > 0.72F) {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(STOMP_DUST));
        int seed = activeReaction.burstSeed();
        float impactProgress = impactAge / 0.72F;
        int alphaBase = Math.max(0, Math.round(220.0F * (1.0F - impactProgress)));

        for (int i = 0; i < 12; i++) {
            float angle = -Mth.HALF_PI + (pseudo(seed, i, 0) - 0.5F) * 1.65F;
            float distance = 0.07F + impactProgress * (0.28F + pseudo(seed, i, 1) * 0.24F);
            float x = -0.18F + Mth.cos(angle) * distance + (pseudo(seed, i, 2) - 0.5F) * 0.08F;
            float y = 0.04F + impactProgress * 0.16F + pseudo(seed, i, 3) * 0.04F;
            float z = -0.42F + Mth.sin(angle) * distance;
            float scale = 0.13F + impactProgress * 0.16F + pseudo(seed, i, 4) * 0.05F;
            int alpha = Math.max(0, Math.round(alphaBase * (0.72F + pseudo(seed, i, 5) * 0.28F)));
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(Mth.sin(ageSeconds * 7.0F + i) * 0.18F));
            poseStack.scale(-scale, -scale, scale);
            drawTexturedQuad(poseStack, consumer, LightTexture.FULL_BRIGHT, alpha);
            poseStack.popPose();
        }
    }

    private static void renderSadTears(Player player, ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        float duration = pizzaPoseDuration(activeReaction.type());
        if (ageSeconds > duration) {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(CREATE_CONFETTI));
        int seed = activeReaction.burstSeed();
        float endFade = 1.0F - Mth.clamp((ageSeconds - (duration - 0.42F)) / 0.42F, 0.0F, 1.0F);

        for (int i = 0; i < 9; i++) {
            float delay = pseudo(seed, i, 10) * 0.62F;
            float t = (ageSeconds + delay) % 0.82F;
            float y = player.getBbHeight() - 0.34F - t * (0.72F + pseudo(seed, i, 1) * 0.16F);
            if (y < 0.58F) {
                continue;
            }

            int alpha = Math.max(0, Math.round(235.0F * (1.0F - t / 0.82F) * endFade));
            if (alpha <= 0) {
                continue;
            }

            for (int sideIndex = 0; sideIndex < 2; sideIndex++) {
                float side = sideIndex == 0 ? -1.0F : 1.0F;
                float x = side * (0.105F + pseudo(seed, i, sideIndex) * 0.018F);
                float z = -0.285F + (pseudo(seed, i, sideIndex + 2) - 0.5F) * 0.025F;
                float size = 0.026F + pseudo(seed, i, sideIndex + 4) * 0.018F;
                poseStack.pushPose();
                poseStack.translate(x, y, z);
                poseStack.mulPose(dispatcher.cameraOrientation());
                poseStack.scale(0.75F, 1.35F, 1.0F);
                drawConfettiQuad(poseStack, consumer, LightTexture.FULL_BRIGHT, 0x5EC8FF, alpha, size);
                poseStack.popPose();
            }
        }
    }

    private static void renderAngelEffect(Player player, PlayerModel<AbstractClientPlayer> model, ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource) {
        float fade = effectFade(activeReaction);
        if (fade <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, player.getBbHeight() + 0.48D + Mth.sin(ageSeconds * 4.0F) * 0.012D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(0.42F, 0.42F, 0.42F);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ReactionItems.ANGEL_HALO.get()), ItemDisplayContext.HEAD, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, player.level(), activeReaction.burstSeed());
        poseStack.popPose();
    }

    private static void renderHeartBurst(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(HEART_PARTICLE));
        int seed = activeReaction.burstSeed();
        float fade = effectFade(activeReaction);

        for (int i = 0; i < 22; i++) {
            float delay = pseudo(seed, i, 8) * 0.52F;
            float t = (ageSeconds + delay) % 1.32F;
            float side = i % 2 == 0 ? -1.0F : 1.0F;
            float x = side * (0.12F + pseudo(seed, i, 0) * 0.34F) + Mth.sin(ageSeconds * 4.0F + i) * 0.06F;
            float y = 0.92F + t * (0.68F + pseudo(seed, i, 1) * 0.34F);
            float z = (pseudo(seed, i, 2) - 0.5F) * 0.46F;
            float scale = 0.12F + pseudo(seed, i, 3) * 0.065F;
            int alpha = Math.max(0, Math.round(245.0F * (1.0F - t / 1.32F) * fade));
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(Mth.sin(ageSeconds * 3.0F + i) * 0.3F));
            poseStack.scale(-scale, -scale, scale);
            drawTexturedQuad(poseStack, consumer, LightTexture.FULL_BRIGHT, alpha);
            poseStack.popPose();
        }
    }

    private static float effectFade(ActiveReaction activeReaction) {
        float progress = activeReaction.normalizedAge(Util.getMillis());
        return progress < 0.78F ? 1.0F : Mth.clamp(1.0F - ((progress - 0.78F) / 0.22F), 0.0F, 1.0F);
    }

    private static void renderIconQuad(PoseStack poseStack, MultiBufferSource bufferSource, ReactionType reaction, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(reaction.texture()));
        int light = LightTexture.FULL_BRIGHT;

        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void renderConfetti(ActiveReaction activeReaction, float ageSeconds, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher dispatcher) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(CREATE_CONFETTI));
        int light = LightTexture.FULL_BRIGHT;
        int seed = activeReaction.burstSeed();

        for (int i = 0; i < 44; i++) {
            float delay = pseudo(seed, i, 5) * 0.16F;
            float t = ageSeconds - delay;
            if (t < 0.0F) {
                continue;
            }

            float angle = pseudo(seed, i, 0) * Mth.TWO_PI;
            float horizontalSpeed = 0.18F + pseudo(seed, i, 1) * 0.74F;
            float verticalSpeed = 1.58F + pseudo(seed, i, 2) * 0.92F;
            float gravity = 1.85F + pseudo(seed, i, 6) * 0.5F;
            float wobble = Mth.sin((t * 7.0F) + pseudo(seed, i, 7) * Mth.TWO_PI) * 0.05F;
            float x = Mth.cos(angle) * horizontalSpeed * t + Mth.cos(angle + Mth.HALF_PI) * wobble;
            float z = Mth.sin(angle) * horizontalSpeed * t + Mth.sin(angle + Mth.HALF_PI) * wobble;
            float y = 2.02F + verticalSpeed * t - 0.5F * gravity * t * t;
            float size = 0.055F + pseudo(seed, i, 3) * 0.035F;
            if (y <= size) {
                continue;
            }

            int color = CONFETTI_COLORS[Math.floorMod(seed + i, CONFETTI_COLORS.length)];
            int alpha = Math.max(0, Math.round(255.0F * (1.0F - Mth.clamp((ageSeconds - 3.0F) / 0.34F, 0.0F, 1.0F))));

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(t * Mth.TWO_PI * (0.8F + pseudo(seed, i, 4) * 2.4F)));
            drawConfettiQuad(poseStack, consumer, light, color, alpha, size);
            poseStack.popPose();
        }
    }

    private static PoseKeyframe sampleCheerPose(float ageSeconds) {
        for (int i = 0; i < CHEER_KEYFRAMES.length - 1; i++) {
            PoseKeyframe from = CHEER_KEYFRAMES[i];
            PoseKeyframe to = CHEER_KEYFRAMES[i + 1];
            if (ageSeconds <= to.timeSeconds) {
                float delta = (ageSeconds - from.timeSeconds) / (to.timeSeconds - from.timeSeconds);
                float eased = delta * delta * (3.0F - 2.0F * delta);
                return PoseKeyframe.lerp(from, to, Mth.clamp(eased, 0.0F, 1.0F));
            }
        }
        return CHEER_KEYFRAMES[CHEER_KEYFRAMES.length - 1];
    }

    private static float smoothPulse(float value, float start, float end) {
        float progress = Mth.clamp((value - start) / (end - start), 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private static boolean isPizzaEmote(ReactionType reaction) {
        return reaction.ordinal() >= ReactionType.PIZZA_HI.ordinal() && reaction.ordinal() <= ReactionType.PIZZA_VINE_BOOM.ordinal();
    }

    private static float clapImpact(float ageSeconds) {
        float impact = 0.0F;
        for (float clapTime : PIZZA_CLAP_IMPACTS) {
            float distance = Math.abs(ageSeconds - clapTime);
            impact = Math.max(impact, 1.0F - Mth.clamp(distance / 0.085F, 0.0F, 1.0F));
        }
        return impact * impact * (3.0F - 2.0F * impact);
    }

    private static float pizzaPoseDuration(ReactionType reaction) {
        return switch (reaction) {
            case PIZZA_HI -> 1.75F;
            case PIZZA_SAD -> 2.35F;
            case PIZZA_LAUGH -> 1.45F;
            case PIZZA_CLAP -> 1.9F;
            case PIZZA_FEAR -> 1.25F;
            case PIZZA_WOAH -> 1.15F;
            case PIZZA_EW -> 1.05F;
            case PIZZA_ANGEL -> 2.2F;
            case PIZZA_HEARTS -> 1.85F;
            case PIZZA_67 -> 2.4F;
            case PIZZA_VINE_BOOM -> 1.2F;
            default -> 0.92F;
        };
    }

    private static void drawConfettiQuad(PoseStack poseStack, VertexConsumer consumer, int light, int color, int alpha, float size) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;

        consumer.addVertex(matrix, -size, size, 0.0F).setColor(red, green, blue, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, size, size, 0.0F).setColor(red, green, blue, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, size, -size, 0.0F).setColor(red, green, blue, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -size, -size, 0.0F).setColor(red, green, blue, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void drawTexturedQuad(PoseStack poseStack, VertexConsumer consumer, int light, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(255, 255, 255, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static float pseudo(int seed, int index, int salt) {
        int value = seed ^ (index * 73428767) ^ (salt * 912931);
        value ^= value >>> 16;
        value *= 0x7feb352d;
        value ^= value >>> 15;
        return (value & 0xFFFF) / 65535.0F;
    }

    private record PoseKeyframe(float timeSeconds, float bodyXRot, float headXRot,
                                float rightArmXRot, float rightArmYRot, float rightArmZRot,
                                float leftArmXRot, float leftArmYRot, float leftArmZRot,
                                float rightLegXRot, float leftLegXRot, float rightLegZRot, float leftLegZRot,
                                float rightLegYRot, float leftLegYRot) {
        private static PoseKeyframe lerp(PoseKeyframe from, PoseKeyframe to, float t) {
            return new PoseKeyframe(
                Mth.lerp(t, from.timeSeconds, to.timeSeconds),
                Mth.lerp(t, from.bodyXRot, to.bodyXRot),
                Mth.lerp(t, from.headXRot, to.headXRot),
                Mth.lerp(t, from.rightArmXRot, to.rightArmXRot),
                Mth.lerp(t, from.rightArmYRot, to.rightArmYRot),
                Mth.lerp(t, from.rightArmZRot, to.rightArmZRot),
                Mth.lerp(t, from.leftArmXRot, to.leftArmXRot),
                Mth.lerp(t, from.leftArmYRot, to.leftArmYRot),
                Mth.lerp(t, from.leftArmZRot, to.leftArmZRot),
                Mth.lerp(t, from.rightLegXRot, to.rightLegXRot),
                Mth.lerp(t, from.leftLegXRot, to.leftLegXRot),
                Mth.lerp(t, from.rightLegZRot, to.rightLegZRot),
                Mth.lerp(t, from.leftLegZRot, to.leftLegZRot),
                Mth.lerp(t, from.rightLegYRot, to.rightLegYRot),
                Mth.lerp(t, from.leftLegYRot, to.leftLegYRot)
            );
        }
    }

}
