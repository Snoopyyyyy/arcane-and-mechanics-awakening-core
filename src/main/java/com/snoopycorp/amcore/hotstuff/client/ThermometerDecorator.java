package com.snoopycorp.amcore.hotstuff.client;

import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

/**
 * Small graduated thermometer drawn in the upper left corner of hot items in inventories. Its tube fills up and
 * changes color with the heat state: one mark when warm, two when hot, full when scorching.
 */
public final class ThermometerDecorator implements IItemDecorator {
    public static final ThermometerDecorator INSTANCE = new ThermometerDecorator();

    /**
     * Pixel art of the thermometer: {@code o} outline, {@code t} graduation mark, {@code f} always filled,
     * {@code 1} to {@code 4} tube rows filled from that heat level.
     */
    private static final String[] PIXELS = {
            "..ooo.",
            "t.o4o.",
            "..o3o.",
            "t.o2o.",
            "..o1o.",
            ".oofoo",
            ".offfo",
            ".offfo",
            "..ooo."
    };
    /** Tube level reached by each heat state, from the coolest to the hottest. */
    private static final int[] LEVELS = {1, 2, 4};
    private static final int OUTLINE = 0xFF2B1B17;
    private static final int EMPTY = 0xFFE0E0E0;
    /** Position of the thermometer in the 16x16 item area. */
    private static final int X = 0;
    private static final int Y = 0;
    /** Same depth as the stack count, above the item. */
    private static final float Z = 200;

    private ThermometerDecorator() {
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof HotStuffItem item)) {
            return false;
        }

        int fill = 0xFF000000 | item.getState().getIconColor();
        int level = LEVELS[Math.min(item.getState().ordinal(), LEVELS.length - 1)];

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z);

        for (int row = 0; row < PIXELS.length; row++) {
            for (int column = 0; column < PIXELS[row].length(); column++) {
                char pixel = PIXELS[row].charAt(column);
                int color = switch (pixel) {
                    case 'o', 't' -> OUTLINE;
                    case 'f' -> fill;
                    case '1', '2', '3', '4' -> pixel - '0' <= level ? fill : EMPTY;
                    default -> 0;
                };

                if (color != 0) {
                    int x = xOffset + X + column;
                    int y = yOffset + Y + row;
                    guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 1, y + 1, color);
                }
            }
        }

        guiGraphics.pose().popPose();
        return false;
    }
}
