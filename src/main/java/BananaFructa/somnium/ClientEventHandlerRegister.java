package BananaFructa.somnium;

import BananaFructa.somnium.mechanics.projectiles.PulseRenderer;
import BananaFructa.somnium.mechanics.projectiles.TrailParticleProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Somnium.MODID,bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientEventHandlerRegister {

    @SubscribeEvent
    public static void registerRender(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                Entities.pulseProjectile.get(),
                PulseRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(Somnium.TRAIL_PARTICLE.get(), TrailParticleProvider::new);
    }

}
