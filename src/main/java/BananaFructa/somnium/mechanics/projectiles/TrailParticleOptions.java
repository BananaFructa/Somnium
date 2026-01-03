package BananaFructa.somnium.mechanics.projectiles;

import BananaFructa.somnium.Somnium;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class TrailParticleOptions implements ParticleOptions {
    public static final ParticleOptions.Deserializer<TrailParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<TrailParticleOptions>() {
        public TrailParticleOptions fromCommand(ParticleType<TrailParticleOptions> p_123846_, StringReader p_123847_) {
            return null;
        }

        public TrailParticleOptions fromNetwork(ParticleType<TrailParticleOptions> p_123849_, FriendlyByteBuf buf) {
            float r = buf.readFloat();
            float g = buf.readFloat();
            float b = buf.readFloat();
            float size = buf.readFloat();
            float gravity = buf.readFloat();
            boolean interactionSystem = buf.readBoolean();
            return new TrailParticleOptions(r,g,b,size,gravity,interactionSystem);
        }
    };
    public static final Codec<TrailParticleOptions> codec = RecordCodecBuilder.create(instace ->
        instace.group(
                Codec.FLOAT.fieldOf("r").forGetter(t->t.r),
                Codec.FLOAT.fieldOf("g").forGetter(t->t.g),
                Codec.FLOAT.fieldOf("b").forGetter(t->t.b),
                Codec.FLOAT.fieldOf("size").forGetter(t->t.size),
                Codec.FLOAT.fieldOf("gravity").forGetter(t->t.gravity),
                Codec.BOOL.fieldOf("interactionSystem").forGetter(t->t.interactionSystem)
        ).apply(instace, TrailParticleOptions::new)
    );

    public float r,g,b;
    public float size;
    public float gravity;
    public boolean interactionSystem;

    public TrailParticleOptions(float r, float g, float b, float size, float gravity, boolean interactionSystem) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.size = size;
        this.gravity = gravity;
        this.interactionSystem = interactionSystem;
    }

    public ParticleType<?> getType() {
        return Somnium.TRAIL_PARTICLE.get();
    }

    public Codec<TrailParticleOptions> codec() {
        return this.codec;
    }

    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeFloat(size);
        buf.writeFloat(gravity);
        buf.writeBoolean(interactionSystem);
    }

    public String writeToString() {
        return null;
    }
}