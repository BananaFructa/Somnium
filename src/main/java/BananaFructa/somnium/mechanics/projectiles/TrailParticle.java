package BananaFructa.somnium.mechanics.projectiles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TrailParticle extends TextureSheetParticle {


    public static List<TrailParticle> particles = new ArrayList<>();
    public static HashMap<BlockPos,List<TrailParticle>> spaceBinning = new HashMap<>();

    public static Random random = new Random();

    public boolean interactionSystem = false;

    protected TrailParticle(ClientLevel client, double x, double y, double z,double vx,double vy, double vz) {
        super(client,x,y,z,0,0,0);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lifetime = 80 + (int)(random.nextGaussian(0,1)*10);
        this.alpha = 0.5f;
        this.setColor(1,0,0);
    }

    public void setInteractionSystem(boolean interactionSystem) {
        this.interactionSystem = interactionSystem;
        if (interactionSystem) {
            particles.add(this);
        }
    }

    @Override
    public void tick() {
        Random source = new Random((int)(x + y + z));
        if (interactionSystem) {
            Vec3 f = force();
            this.xd += f.x + source.nextGaussian(0,0.1)*0.01;
            this.yd += f.y + source.nextGaussian(0,0.1)*0.01;
            this.zd += f.z + source.nextGaussian(0,0.1)*0.01;
        }
        super.tick();

    }

    public Vec3 force() {
        Vec3 f = new Vec3(0,0,0);
        BlockPos bposition = new BlockPos((int)x,(int)y,(int)z);
        int range = 1;
        int loop = 0;
        int maxLoop = 100;
        boolean b = false;
        for (int i = -range;i <= range;i++) {
            for (int j = -range;j <= range;j++) {
                for (int k = -range;k <= range;k++) {
                    BlockPos pos = bposition.offset(new Vec3i(i,j,k));
                    if (spaceBinning.containsKey(pos)) {
                        List<TrailParticle> ps = spaceBinning.get(pos);
                        for (TrailParticle p : ps) {
                            if (++loop == maxLoop) {
                                b = true;
                                break;
                            }
                            if (this != p) {
                                double dist = this.getPos().distanceTo(p.getPos())*7-1;
                                Vec3 dir = p.getPos().subtract(this.getPos()).normalize();
                                f = f.add(dir.scale(dist*dist*dist*0.00001*(1/(1+Math.exp(dist-3)))));
                            }
                        }
                        if (b) break;
                    }
                }
                if (b) break;
            }
            if (b) break;
        }
        return f.subtract(new Vec3(xd,yd,zd).scale(0.0001));
    }

    public void setQuad(float q) {
        this.quadSize = q;
    }

    public TrailParticle withGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    @Override
    public void remove() {
        if (interactionSystem) particles.remove(this);
        super.remove();
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return 0xf000f0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
