package BananaFructa.somnium;

import BananaFructa.somnium.mechanics.projectiles.Pulse;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Entities {
    public static RegistryObject<EntityType<Pulse>> pulseProjectile;

    public static void register(IEventBus bus) {
        DeferredRegister<EntityType<?>> entities = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,Somnium.MODID);
        pulseProjectile = entities.register("pulse",()->
           EntityType.Builder.of(Pulse::new, MobCategory.MISC)
                   .sized(0.25f,0.25f)
                   .clientTrackingRange(1000)
                   .updateInterval(1)
                   .build("pulse")
        );
        entities.register(bus);
    }
}
