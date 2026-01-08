package BananaFructa.somnium;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Somnium.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final int defaultMinInteractionPeriod = 20*60*5;
    private static final int defaultDescriptorContextWindow = 4096;
    private static final int defaultFilterContextWindow = 4096;
    private static final int defaultContextGeneratorContextWindow = 10000;
    private static final int defaultCoderContextWindow = 10000;
    private static final String defaultOllamaServiceAddress = "http://127.0.0.1:11434";
    private static final String defaultDescriptorModel = "qwen2.5vl:7b";
    private static final String defaultDescriptorPriming = """
    Does the Minecraft screenshot appear to contain a place of significance ?
    Generate a short description, including what materials seem to be used.
    Be descriptive.""";
    private static final String defaultFilterModel = "qwen2.5vl:7b";
    private static final String defaultFilterPriming = "You are receiving a description of a Minecraft Screenshot. Say \"Yes\" if the screenshot signifies an unusual construction or some man-made magical place, say \"No\" otherwise.";
    private static final String defaultContextGeneratorModel = "ALIENTELLIGENCE/gamemasterroleplaying:latest";
    private static final String defaultContextGeneratorPriming = """
    You are part of a magic Minecraft Mod.

    You will receive reports about the ritual that the player attempts, with information regarding what they are doing and their surroundings.
    You must decide what the outcomes will be.
    You are only allowed to give the player items with certain properties or potions with certain effects. Provide a description of what they do.
    Based on the input you can decide the outcome to be anywhere from highly positive to highly negative.
    You cannot spawn entities, blocks or influence crafting recopies.
    There are no magical elements or mechanics that already exist in the game, trying to reference them does not work.
    For each ritual provide a short cryptic message (under 10 words) that embodies the ritual.
    Be pragmatic. Example: Sword that sets blocks on fire or potion that heals but also blinds the player.

    The first report is:""";
    private static final String defaultCoderModel = "qwen3:8b";
    private static final String defaultCoderPrimingTop = """
    You are receiving the description of a ritual outcome in a Minecraft Mode. Your job is to generate code that best implements the prompted outcome. Additionally, deliver the cryptic message written in the ritual report to the player when enacting the ritual. Enclose the generated code in a ```python ``` block.""";
    private static final String defaultCoderPrimingBottom = """
    Only use the provided functions (i.e. do not import anything).
    You will have to do the best you can with the available information.
    Do not forget to register effects before you use them.
    Do not forget that creating items doesn't automatically give them to the player.
    When forced to compromise in how a functionality is implemented update descriptions to reflected the implemented functionality.
    Wrap all code in a main function which takes in an input parameter of the entity which initialised the interaction.
    def main(initiatorEntity):
        # All code here""";
    private static final String defaultEmbeddingModel = "snowflake-arctic-embed:110m";
    private static final int defaultPythonLoopLimit = 1000;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> OLLAMA_SERVICE_ADDRESS = BUILDER.comment("Address for the ollama service. By default it is the localhost. Ollama doesn't have authentication or rate limiting by default, if you don't know what you are doing I wouldn't change this for now.").define("ollamaAddress",defaultOllamaServiceAddress);
    private static final ForgeConfigSpec.ConfigValue<String> OLLAMA_DESCRIPTOR_MODEL = BUILDER.comment("Ollama library model that is used for generating the environment description based on the provided image data").define("ollamaDescriptorModel",defaultDescriptorModel);
    private static final ForgeConfigSpec.ConfigValue<String> DESCRIPTOR_PRIMING = BUILDER.comment("Prompt primer for the descriptor model").define("descriptorPriming",defaultDescriptorPriming);
    private static final ForgeConfigSpec.ConfigValue<String> OLLAMA_FILTER_MODEL = BUILDER.comment("Ollama library model that is used for filtering environment descriptions").define("ollamaFilterModel",defaultFilterModel);
    private static final ForgeConfigSpec.ConfigValue<String> FILTER_PRIMING = BUILDER.comment("Prompt primer for the filter model").define("filterPriming",defaultFilterPriming);
    private static final ForgeConfigSpec.ConfigValue<String> OLLAMA_CONTEXT_GENERATOR_MODEL = BUILDER.comment("Ollama library model used to generate the game context").define("ollamaContextGeneratorModel",defaultContextGeneratorModel);
    private static final ForgeConfigSpec.ConfigValue<String> CONTEXT_GENERATOR_PRIMING = BUILDER.comment("Prompt primer for the first prompt of the context generator").define("contextGeneratorPriming",defaultContextGeneratorPriming);
    private static final ForgeConfigSpec.ConfigValue<String> OLLAMA_CODER_MODEL = BUILDER.comment("Ollama library model used to generate python implementations").define("ollamaCoderModel",defaultCoderModel);
    private static final ForgeConfigSpec.ConfigValue<String> CODER_TOP_PRIMING = BUILDER.comment("Prompt primer for the top prompt of the coder model").define("coderTopPriming",defaultCoderPrimingTop);
    private static final ForgeConfigSpec.ConfigValue<String> CODER_BOTTOM_PRIMING = BUILDER.comment("Prompt primer for the bottom prompt of the coder model").define("coderBottomPriming",defaultCoderPrimingBottom);
    private static final ForgeConfigSpec.ConfigValue<Boolean> ONLY_ONE_LOADED = BUILDER.comment("If set to true, after a model has finished responding to a prompt it will be unloaded from memory (RAM/VRAM). Only set this to false if it is running an a machine which can sustain all models being loaded at the same time. For default settings this lands at a recommended minimum value of 28GB of RAM/VRAM to keep all models in memory.").define("onlyOneLoaded",true);
    private static final ForgeConfigSpec.ConfigValue<Integer> CONTEXT_WINDOW_DESCRIPTOR = BUILDER.comment("Context window size for the descriptor model").define("contextWindowDescriptor",defaultDescriptorContextWindow);
    private static final ForgeConfigSpec.ConfigValue<Integer> CONTEXT_WINDOW_FILTER = BUILDER.comment("Context window size for the filter model").define("contextWindowFilter",defaultFilterContextWindow);
    private static final ForgeConfigSpec.ConfigValue<Integer> CONTEXT_WINDOW_CONTEXT_GENERATOR = BUILDER.comment("Context window size for the context generator model").define("contextWindowContextGenerator",defaultContextGeneratorContextWindow);
    private static final ForgeConfigSpec.ConfigValue<Integer> CONTEXT_WINDOW_CODER = BUILDER.comment("Context window size for the coder model").define("contextWindowCoder",defaultCoderContextWindow);
    private static final ForgeConfigSpec.ConfigValue<String> OLLAMA_EMBEDDING_MODEL = BUILDER.comment("Ollama library model used to embedd free form text").define("ollamaEmbeddingModel",defaultEmbeddingModel);
    private static final ForgeConfigSpec.ConfigValue<Integer> PYTHON_LOOP_LIMIT = BUILDER.comment("Maximum number of times a loop can run in the generated python code").define("pythonLoopLimit",defaultPythonLoopLimit);
    private static final ForgeConfigSpec.ConfigValue<Integer> MIN_INTERACTION_PERIOD = BUILDER.comment("Minimum number of ticks before a new interaction can be performed by the player").define("minInteractionPeriod",defaultMinInteractionPeriod);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String ollamaServiceAddress;
    public static String ollamaDescriptorModel;
    public static String descriptorPrimer;
    public static String ollamaFilterModel;
    public static String filterPriming;
    public static String ollamaContextGeneratorModel;
    public static String contextGeneratorPriming;
    public static String ollamaCoderModel;
    public static String coderTopPriming;
    public static String coderBottomPriming;
    public static String embeddingModel;
    public static boolean onlyOneLoaded;
    public static int descriptorContextWindow;
    public static int filterContextWindow;
    public static int contextGeneratorContextWindow;
    public static int coderContextWindow;
    public static int pythonLoopLimit;
    public static int minInteractionPeriod;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ollamaServiceAddress = OLLAMA_SERVICE_ADDRESS.get();
        ollamaDescriptorModel = OLLAMA_DESCRIPTOR_MODEL.get();
        descriptorPrimer = DESCRIPTOR_PRIMING.get();
        ollamaFilterModel = OLLAMA_FILTER_MODEL.get();
        filterPriming = FILTER_PRIMING.get();
        ollamaContextGeneratorModel = OLLAMA_CONTEXT_GENERATOR_MODEL.get();
        contextGeneratorPriming = CONTEXT_GENERATOR_PRIMING.get();
        ollamaCoderModel = OLLAMA_CODER_MODEL.get();
        coderTopPriming = CODER_TOP_PRIMING.get();
        coderBottomPriming = CODER_BOTTOM_PRIMING.get();
        embeddingModel = OLLAMA_EMBEDDING_MODEL.get();
        onlyOneLoaded = ONLY_ONE_LOADED.get();
        descriptorContextWindow = CONTEXT_WINDOW_DESCRIPTOR.get();
        filterContextWindow = CONTEXT_WINDOW_FILTER.get();
        contextGeneratorContextWindow = CONTEXT_WINDOW_CONTEXT_GENERATOR.get();
        coderContextWindow = CONTEXT_WINDOW_CODER.get();
        pythonLoopLimit = PYTHON_LOOP_LIMIT.get();
        minInteractionPeriod = MIN_INTERACTION_PERIOD.get();
    }
}
