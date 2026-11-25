package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Renderer humanoide (modelo de jogador, bracos classicos) que usa textures/entity/<nome>.png.
 * Para trocar a aparencia, basta substituir o PNG (64x64) por uma skin de verdade.
 */
public class SimpleHumanoidRenderer<T extends Mob> extends HumanoidMobRenderer<T, PlayerModel<T>> {
    private final ResourceLocation texture;
    private float scale = 1.0F;

    public SimpleHumanoidRenderer(EntityRendererProvider.Context context, String textureName) {
        this(context, textureName, false);
    }

    /** @param slim true para skins de bracos finos (Alex, 3 px). */
    public SimpleHumanoidRenderer(EntityRendererProvider.Context context, String textureName, boolean slim) {
        super(context, new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5F);
        this.texture = new ResourceLocation(HellaverseMod.MODID, "textures/entity/" + textureName + ".png");
    }

    /** Redimensiona o modelo (ex.: 0,65 para criaturas pequenas). */
    public SimpleHumanoidRenderer<T> withScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
