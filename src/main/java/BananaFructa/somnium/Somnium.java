package BananaFructa.somnium;

import BananaFructa.somnium.commands.*;
import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import BananaFructa.somnium.gamelinking.LinkedGameDefinitions;
import BananaFructa.somnium.mechanics.effects.ProgrammableEffectProvider;
import BananaFructa.somnium.mechanics.projectiles.TrailParticleOptions;
import BananaFructa.somnium.packets.PacketRegistry;
import BananaFructa.somnium.service.ChainOfAgentsContextProcessor;
import BananaFructa.somnium.service.OllamaServiceHandler;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Somnium.MODID)
public class Somnium {
    public static final String MODID = "somnium";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAME = "Somnium";

    public static Somnium INSTANCE;

    public ProgrammableEffectProvider effectProvider = new ProgrammableEffectProvider(64);
    public OllamaServiceHandler ollamaService;
    public ChainOfAgentsContextProcessor universalContext;
    public SomniumWorldData worldData;

    public Somnium(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.register(this);

        INSTANCE = this;

        effectProvider.register(context.getModEventBus());
        Entities.register(context.getModEventBus());
        PARTICLES.register(context.getModEventBus());

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        PacketRegistry.init();
        GameLinkingHandler.registerGameLinkerDefiner(LinkedGameDefinitions.class,null);
    }

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES,MODID);
    public static final RegistryObject<ParticleType<TrailParticleOptions>> TRAIL_PARTICLE = Somnium.PARTICLES.register("trail_particle", () -> new ParticleType<>(false, TrailParticleOptions.DESERIALIZER) {
        @Override
        public Codec<TrailParticleOptions> codec() {
            return TrailParticleOptions.codec;
        }
    });;

    public void init(MinecraftServer server) {
        ollamaService = new OllamaServiceHandler(Config.ollamaServiceAddress);
        universalContext = new ChainOfAgentsContextProcessor(0,ollamaService);
        DimensionDataStorage storage = server.getLevel(Level.OVERWORLD).getDataStorage();
        worldData = storage.get(SomniumWorldData::load,"somnium_data");
        if (worldData == null) {
            worldData = new SomniumWorldData();
            storage.set("somnium_data",worldData);
        }
        LinkedGameDefinitions.loadRegistryNames();
    }

    /*@SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ollamaService = new OllamaServiceHandler(Config.ollamaServiceAddress);
        LinkedGameDefinitions.loadRegistryNames();
        universalContext = new ChainOfAgentsContextProcessor(0,ollamaService);
        DimensionDataStorage storage = event.getServer().getLevel(Level.OVERWORLD).getDataStorage();
        worldData = storage.get(SomniumWorldData::load,"somnium_data");
        if (worldData == null) {
            worldData = new SomniumWorldData();
            storage.set("somnium_data",worldData);
        }
    }*/

    @SubscribeEvent
    public void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(DescribeCommand.register());
        event.getDispatcher().register(PromptCommand.register());
        event.getDispatcher().register(FromFreeForm.register());
        event.getDispatcher().register(FromFreeFormItem.register());
        event.getDispatcher().register(GetItem.register());
        event.getDispatcher().register(ListItems.register());
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

    }
}
