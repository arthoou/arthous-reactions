package com.arthou.arthousreactions.client;

import com.arthou.arthousreactions.reaction.ReactionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public final class ReactionWheelScreen extends Screen {
    private static final int PAGE_SIZE = 12;
    private static final int SLOT_SIZE = 60;
    private static final int SLOT_DISTANCE = 136;
    private static final int OUTER_RING = 176;
    private static final int INNER_RING = 72;
    private static final int PAGE_BUTTON_SIZE = 24;
    private int pageIndex;

    public ReactionWheelScreen() {
        super(Component.translatable("screen.arthousreactions.title"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (isOverPrevPageButton(mouseX, mouseY)) {
                changePage(-1);
                return true;
            }

            if (isOverNextPageButton(mouseX, mouseY)) {
                changePage(1);
                return true;
            }

            ReactionType hovered = findHoveredReaction(this.width / 2, this.height / 2, mouseX, mouseY);
            if (hovered != null) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && hovered == ReactionType.PIZZA_HI) {
                    selectReactionWithByeSound(hovered);
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    selectReaction(hovered);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || isOpenWheelKey(keyCode)) {
            onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            changePage(-1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            changePage(1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            ReactionType hovered = findHoveredReaction(this.width / 2, this.height / 2, scaledMouseX(), scaledMouseY());
            if (hovered != null) {
                selectReaction(hovered);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0D) {
            changePage(-1);
            return true;
        }
        if (scrollY < 0.0D) {
            changePage(1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        List<ReactionType> reactions = currentPageReactions();
        ReactionType hovered = findHoveredReaction(centerX, centerY, mouseX, mouseY);

        guiGraphics.fill(0, 0, this.width, this.height, 0x7D05070A);
        fillCircle(guiGraphics, centerX, centerY, OUTER_RING, 0x99272E36);
        fillCircle(guiGraphics, centerX, centerY, OUTER_RING - 16, 0xCC313B45);
        fillCircle(guiGraphics, centerX, centerY, INNER_RING, 0xE511161C);

        for (int slot = 0; slot < reactions.size(); slot++) {
            ReactionType reaction = reactions.get(slot);
            double angle = Math.toRadians(-90.0D + slot * (360.0D / reactions.size()));
            int slotCenterX = centerX + (int) Math.round(Math.cos(angle) * SLOT_DISTANCE);
            int slotCenterY = centerY + (int) Math.round(Math.sin(angle) * SLOT_DISTANCE);
            boolean selected = reaction == hovered;
            int outerColor = selected ? 0xFFF4D08B : 0xC82A3138;
            int innerColor = selected ? 0xFFF8F2E7 : 0xE26A7682;
            int radius = selected ? 35 : 31;
            int iconSize = selected ? 48 : 42;

            fillCircle(guiGraphics, slotCenterX, slotCenterY, radius, outerColor);
            fillCircle(guiGraphics, slotCenterX, slotCenterY, radius - 3, innerColor);
            drawReaction(guiGraphics, reaction, slotCenterX, slotCenterY, iconSize, selected);
        }

        if (hovered != null) {
            drawReaction(guiGraphics, hovered, centerX, centerY - 26, 64, true);
            guiGraphics.drawCenteredString(this.font, Component.translatable(hovered.translationKey()), centerX, centerY + 18, 0xFFF6F8FB);
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.arthousreactions.click"), centerX, centerY + 34, 0xFFD1D8E0);
        } else {
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.arthousreactions.click"), centerX, centerY - 2, 0xFFF6F8FB);
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.arthousreactions.close"), centerX, centerY + 14, 0xFFD1D8E0);
        }

        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.arthousreactions.page", pageIndex + 1, pageCount()), centerX, centerY + 48, 0xFFD1D8E0);
        if (pageCount() > 1) {
            renderPageButton(guiGraphics, centerX - 44, centerY + 42, "<", isOverPrevPageButton(mouseX, mouseY));
            renderPageButton(guiGraphics, centerX + 20, centerY + 42, ">", isOverNextPageButton(mouseX, mouseY));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void drawReaction(GuiGraphics guiGraphics, ReactionType reaction, int centerX, int centerY, int size, boolean selected) {
        if (reaction.hasEmoji()) {
            guiGraphics.blit(reaction.texture(), centerX - size / 2, centerY - size / 2, size, size, 0.0F, 0.0F, ReactionType.TEXTURE_SIZE, ReactionType.TEXTURE_SIZE, ReactionType.TEXTURE_SIZE, ReactionType.TEXTURE_SIZE);
            return;
        }

        guiGraphics.drawCenteredString(this.font, "CHEER", centerX, centerY - 5, selected ? 0xFF1A1E24 : 0xFFF6F8FB);
        guiGraphics.drawCenteredString(this.font, "✦", centerX, centerY + 8, selected ? 0xFFD9952B : 0xFFE0B96D);
    }

    private ReactionType findHoveredReaction(int centerX, int centerY, double mouseX, double mouseY) {
        List<ReactionType> reactions = currentPageReactions();
        ReactionType closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int slot = 0; slot < reactions.size(); slot++) {
            ReactionType reaction = reactions.get(slot);
            double angle = Math.toRadians(-90.0D + slot * (360.0D / reactions.size()));
            double slotCenterX = centerX + Math.cos(angle) * SLOT_DISTANCE;
            double slotCenterY = centerY + Math.sin(angle) * SLOT_DISTANCE;
            double distance = Math.hypot(mouseX - slotCenterX, mouseY - slotCenterY);
            if (distance <= SLOT_SIZE * 0.72D && distance < closestDistance) {
                closest = reaction;
                closestDistance = distance;
            }
        }

        return closest;
    }

    private List<ReactionType> currentPageReactions() {
        List<ReactionType> all = Arrays.asList(ReactionType.values());
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());
        return all.subList(start, end);
    }

    private int pageCount() {
        return Math.max(1, (ReactionType.values().length + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private void changePage(int delta) {
        int totalPages = pageCount();
        if (totalPages <= 1) {
            return;
        }
        pageIndex = Math.floorMod(pageIndex + delta, totalPages);
    }

    private void renderPageButton(GuiGraphics guiGraphics, int x, int y, String label, boolean hovered) {
        int background = hovered ? 0xFFE0B96D : 0xCC2A3138;
        int foreground = hovered ? 0xFF1A1E24 : 0xFFF3F5F8;
        fillCircle(guiGraphics, x + PAGE_BUTTON_SIZE / 2, y + PAGE_BUTTON_SIZE / 2, PAGE_BUTTON_SIZE / 2, background);
        guiGraphics.drawCenteredString(this.font, label, x + PAGE_BUTTON_SIZE / 2, y + 8, foreground);
    }

    private boolean isOverPrevPageButton(double mouseX, double mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        return isInside(mouseX, mouseY, centerX - 44, centerY + 42, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE);
    }

    private boolean isOverNextPageButton(double mouseX, double mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        return isInside(mouseX, mouseY, centerX + 20, centerY + 42, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean isOpenWheelKey(int keyCode) {
        return ReactionClientBootstrap.openWheelKey() != null && keyCode == ReactionClientBootstrap.openWheelKey().getKey().getValue();
    }

    private double scaledMouseX() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.mouseHandler.xpos() * this.width / minecraft.getWindow().getScreenWidth();
    }

    private double scaledMouseY() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.mouseHandler.ypos() * this.height / minecraft.getWindow().getScreenHeight();
    }

    private void fillCircle(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            int height = (int) Math.floor(Math.sqrt(radiusSq - dx * dx));
            guiGraphics.fill(centerX + dx, centerY - height, centerX + dx + 1, centerY + height + 1, color);
        }
    }

    private void selectReaction(ReactionType reaction) {
        ReactionClientBootstrap.sendReaction(reaction);
        onClose();
    }

    private void selectReactionWithByeSound(ReactionType reaction) {
        ReactionClientBootstrap.sendReactionWithByeSound(reaction);
        onClose();
    }
}
