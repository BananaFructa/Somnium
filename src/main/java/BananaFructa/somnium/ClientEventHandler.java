package BananaFructa.somnium;

import BananaFructa.somnium.mechanics.projectiles.TrailParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        TrailParticle.spaceBinning.clear();
        for (TrailParticle particle : TrailParticle.particles) {
            Vec3 dPos = particle.getPos();
            BlockPos pos = new BlockPos((int)dPos.x,(int)dPos.y,(int)dPos.z);
            if (!TrailParticle.spaceBinning.containsKey(pos)) {
                TrailParticle.spaceBinning.put(pos,new ArrayList<>());
            }
            TrailParticle.spaceBinning.get(pos).add(particle);
        }
    }

}
