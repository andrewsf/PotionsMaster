package com.thevortex.potionsmaster.render.util.xray;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.thevortex.potionsmaster.render.util.BlockInfo;
import com.thevortex.potionsmaster.render.util.OutlineColor;
import com.thevortex.potionsmaster.render.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.List;


public class Render {
    private static final int GL_FRONT_AND_BACK = 1032;
    private static final int GL_LINE = 6913;
    private static final int GL_FILL = 6914;
    private static final int GL_LINES = 1;

    /**
     * Thread safety: acquire oresLock before working with this collection
     */
    private static final List<BlockInfo> ores = new ArrayList<>();

    /**
     * All threads must acquire this lock to use ores list
     */
    private static final Object oresLock = new Object();

    @OnlyIn(Dist.CLIENT)
    public static void drawOres(RenderWorldLastEvent event) {

        Vector3d view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        MatrixStack stack = event.getMatrixStack();

        try {
            stack.pushPose();
            RenderSystem.pushMatrix();

            stack.translate(-view.x, -view.y, -view.z); // translate
            RenderSystem.multMatrix(stack.last().pose());

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            Profile.BLOCKS.apply(); // Sets GL state for block drawing

            synchronized (oresLock) {
                for (BlockInfo b : ores) {
                    if (b != null) {
                        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                        Util.renderBlock(buffer, b);

                        tessellator.end();
                    }
                }
            }

            Profile.BLOCKS.clean();
        } finally {
            stack.popPose();
            RenderSystem.popMatrix();
        }
    }

    public static void replaceOres(List<BlockInfo> renderQueue) {
        synchronized (oresLock) {
            Render.ores.clear();
            Render.ores.addAll(renderQueue);
        }
    }

    public static void removeOre(BlockPos pos) {
        synchronized (oresLock) {
            Render.ores.remove(new BlockInfo(pos, null, 0.0));
        }
    }

    public static void addOre(BlockPos pos, OutlineColor color, double alpha) {
        synchronized (oresLock) {
            Render.ores.add(new BlockInfo(pos, color, alpha));
        }
    }

    public static void clearOres() {
        synchronized (oresLock) {
            Render.ores.clear();
        }
    }

    /**
     * OpenGL Profiles used for rendering blocks and entities
     */
    @OnlyIn(Dist.CLIENT)
    private enum Profile {
        BLOCKS {
            @Override
            public void apply() {
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.polygonMode(GL_FRONT_AND_BACK, GL_LINE);
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.enableBlend();
                RenderSystem.lineWidth((float) 3.0);
            }

            @Override
            public void clean() {

                RenderSystem.polygonMode(GL_FRONT_AND_BACK, GL_FILL);
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.enableTexture();
            }
        },
        ENTITIES {
            @Override
            public void apply() {

            }

            @Override
            public void clean() {

            }
        };

        Profile() {
        }

        public abstract void apply();

        public abstract void clean();
    }
}
