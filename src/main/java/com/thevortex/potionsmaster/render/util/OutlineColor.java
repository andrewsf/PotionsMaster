package com.thevortex.potionsmaster.render.util;

public class OutlineColor {
    /**
     * Range: 0–255
     */
    private final int r;

    /**
     * Range: 0–255
     */
    private final int g;

    /**
     * Range: 0–255
     */
    private final int b;

    public OutlineColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed() {
        return this.r;
    }

    public int getGreen() {
        return this.g;
    }

    public int getBlue() {
        return this.b;
    }
}
