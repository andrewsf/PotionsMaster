package com.thevortex.potionsmaster.render.util;

import net.minecraft.util.math.vector.Vector3i;

public class BlockInfo extends Vector3i {
    public final OutlineColor color;

    /**
     * Range: 0–255
     */
    public final int alpha;

    /**
     * @param x the block x position
     * @param y the block y position
     * @param z the block z position
     * @param color the outline color
     * @param alpha the opacity. Range: 0.0–1.0
     */
    public BlockInfo(int x, int y, int z, OutlineColor color, double alpha) {
        super(x, y, z);
        this.color = color;
        this.alpha = (int)(255 * alpha);
    }

    /**
     * @param pos the block position
     * @param color the outline color
     * @param alpha the opacity Range: 0.0–1.0
     */
    public BlockInfo(Vector3i pos, OutlineColor color, double alpha) {
        this(pos.getX(), pos.getY(), pos.getZ(), color, alpha);
    }

}
