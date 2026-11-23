package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Ceu do Anel do Orgulho: sem sol/lua, cor de ceu vem do bioma (vermelho) e um
 * pentagrama gigante gira lentamente no alto.
 */
public class PrideSkyEffects extends DimensionSpecialEffects {
    public static final ResourceLocation ID = new ResourceLocation(HellaverseMod.MODID, "orgulho");
    private static final ResourceLocation PENTAGRAM =
            new ResourceLocation(HellaverseMod.MODID, "textures/environment/pentagram.png");

    public PrideSkyEffects() {
        super(Float.NaN, false, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PENTAGRAM);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((ticks + partialTick) * 0.05F));
        Matrix4f matrix = poseStack.last().pose();

        float size = 70.0F;
        float height = 100.0F;
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, -size, height, -size).uv(0.0F, 0.0F).endVertex();
        buffer.vertex(matrix, -size, height, size).uv(0.0F, 1.0F).endVertex();
        buffer.vertex(matrix, size, height, size).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, size, height, -size).uv(1.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        return true; // pula o ceu padrao (sem sol, lua e estrelas)
    }
}
