package com.hellaverse.client;

import com.hellaverse.entity.AsmodeusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Modelo humanoide ampliado para acompanhar a altura atual (5 blocos em pe, menor quando agachado). */
public class AsmodeusRenderer extends SimpleHumanoidRenderer<AsmodeusEntity> {
    public AsmodeusRenderer(EntityRendererProvider.Context context) {
        super(context, "asmodeus");
        this.shadowRadius = 1.4F;
    }

    @Override
    protected void scale(AsmodeusEntity entity, PoseStack poseStack, float partialTick) {
        float scale = entity.getBbHeight() / 1.8F;
        poseStack.scale(scale, scale, scale);
    }
}
