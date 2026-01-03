package BananaFructa.somnium.mechanics.projectiles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.Nullable;

public class TrailParticleProvider  implements ParticleProvider<TrailParticleOptions> {

    private final SpriteSet sprites;

    public TrailParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public @Nullable Particle createParticle(TrailParticleOptions type, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        TrailParticle particle = new TrailParticle(level,x,y,z,vx,vy,vz);
        particle.pickSprite(sprites);
        particle.setColor(type.r, type.g,type.b);
        particle.setQuad(type.size);
        particle.withGravity(type.gravity);
        particle.setInteractionSystem(type.interactionSystem);
        return particle;
    }
}
