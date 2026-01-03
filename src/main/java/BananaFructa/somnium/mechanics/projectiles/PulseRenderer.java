package BananaFructa.somnium.mechanics.projectiles;

import BananaFructa.somnium.Somnium;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PulseRenderer extends EntityRenderer<Pulse> {
    public PulseRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public ResourceLocation getTextureLocation(Pulse p_114482_) {
        return ResourceLocation.fromNamespaceAndPath(Somnium.MODID,"pulse.png");
    }
}
