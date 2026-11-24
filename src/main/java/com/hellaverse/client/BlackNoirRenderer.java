package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
import com.hellaverse.entity.BlackNoirEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderiza o Black Noir com o modelo de jogador (bracos classicos) e a skin enviada. */
public class BlackNoirRenderer extends HumanoidMobRenderer<BlackNoirEntity, PlayerModel<BlackNoirEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HellaverseMod.MODID, "textures/entity/black_noir.png");

    public BlackNoirRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackNoirEntity entity) {
        return TEXTURE;
    }
}
