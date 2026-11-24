package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
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

    public SimpleHumanoidRenderer(EntityRendererProvider.Context context, String textureName) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.texture = new ResourceLocation(HellaverseMod.MODID, "textures/entity/" + textureName + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
