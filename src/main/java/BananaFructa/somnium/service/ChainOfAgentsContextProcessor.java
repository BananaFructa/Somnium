package BananaFructa.somnium.service;

import BananaFructa.somnium.ActionTrigger;
import BananaFructa.somnium.Config;
import BananaFructa.somnium.EventHandler;
import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import BananaFructa.somnium.gamelinking.objects.Python_Entity;
import BananaFructa.somnium.packets.PacketRegistry;
import BananaFructa.somnium.packets.S2CRequestRenderData;
import BananaFructa.somnium.packets.S2CShowMessage;
import BananaFructa.somnium.pyinterpreter.JavaPythonShadower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;

import java.util.UUID;

public class ChainOfAgentsContextProcessor {

    public OllamaServiceHandler ollamaService;

    public long disc;
    public String contextGeneratorSession;

    public ChainOfAgentsContextProcessor(long discriminator, OllamaServiceHandler ollamaService) {
        this.disc = discriminator;
        this.ollamaService = ollamaService;
        contextGeneratorSession = "ctx_gen_" + discriminator;
        //filterSession = "filter_" + discriminator;
    }

    // TODO: this is not good needs to be changed
    private String lastHeader;

    public boolean actionTrigger(ServerPlayer player, ActionTrigger trigger) {
        if (!Somnium.INSTANCE.worldData.canInteract(player)) return false;
        lastHeader = /*"Trigger: " + trigger.description + "\n" +*/ // maybe later
                parseDescription(player.getUUID());
        requestRenderData(player);
        return true;
    }

    public void renderDataArrived(ServerPlayer player, byte[] image) {
        if (ollamaService.isDown()) {
            return;
        }

        OllamaTask task = new OllamaTask("Player Action Interact") {
            @Override
            public void run() {
                nextStage();
                String responseDesc = ollamaService.prompt(Config.ollamaDescriptorModel,null,Config.descriptorPrimer, image,Config.descriptorContextWindow,false);
                Somnium.LOGGER.info("[CoA | Descriptor]: {}", responseDesc);
                nextStage();
                String responseFilter = ollamaService.prompt(Config.ollamaFilterModel,null,Config.filterPriming + "\n" + responseDesc,null,Config.filterContextWindow,false);
                Somnium.LOGGER.info("[CoA | Filter]: {}", responseFilter);
                if (responseFilter.contains("Yes")) {
                    Somnium.LOGGER.info("[CoA | System]: Valid setup, proceeding to context update!");

                    String contextPrompt = lastHeader + "\n" + "Surroundings:" + responseDesc;

                    String sessionName = contextGeneratorSession + "_" + player.getUUID().toString();
                    sessionName = null; // TODO: Remove this before release

                    if (!Somnium.INSTANCE.ollamaService.hasSession(sessionName)) {
                        contextPrompt = Config.contextGeneratorPriming + "\n" + contextPrompt + "\nThe allowed interactions are:\n"+GameLinkingHandler.getInteractionList();
                    }
                    Somnium.LOGGER.info("[CoA | Context Prompt | " + sessionName + "] : {}", contextPrompt);
                    nextStage();
                    String contextResponse = ollamaService.prompt(Config.ollamaContextGeneratorModel, sessionName, contextPrompt, null, Config.contextGeneratorContextWindow,false);
                    Somnium.INSTANCE.worldData.setDirty(); // Save the session history
                    Somnium.LOGGER.info("[CoA | Context Generator]: {}", contextResponse);
                    nextStage();
                    String code = generateCode(contextResponse);
                    Somnium.LOGGER.info("[CoA | Generated Code]: {}",code);
                    player.getServer().execute(()->{
                        GameLinkingHandler.runPythonCode(GameLinkingHandler.globalStorageKey,null, code,"main",new Python_Entity(player.getUUID()));
                        EventHandler.playActionSounds(player);
                    });
                } else {
                    PacketRegistry.toPlayer(new S2CShowMessage("Nothing happened"),player);
                }
            }

            @Override
            public String[] stages() {
                return new String[]{"Processing Environment Data","Environment Filtering","Context Generation","Code Generation"};
            }
        };

        TaskScheduler.schedule(task);
    }

    public String generateCode(String prompt) {
        String finalPrompt = Config.coderTopPriming + "\n\n" + prompt + "\n" + GameLinkingHandler.getAPIPromptDescription() + "\n" + Config.coderBottomPriming;
        String response = ollamaService.prompt(Config.ollamaCoderModel, null, finalPrompt, null, Config.coderContextWindow, true);
        String code = "";
        boolean started = false;
        boolean ended = false;
        for (String l : response.split("\n")) {
            if (started) {
                if (l.startsWith("```")) {
                    ended = true;
                    break;
                } else code += l + "\n";
            }
            if (l.startsWith("```python")) started = true;

        }
        if (started && ended) return code;
        return null;
    }

    public static String parseDescription(UUID player) {
        String burned = EventHandler.getItemsBurnedFor(player);
        if (burned == null) return "Items burned: None";
        else return "Items burned: " + burned;
    }

    private static void async(Runnable async) {
        Thread t = new Thread(async::run);
        t.start();
    }

    public void requestRenderData(ServerPlayer player) {
        PacketRegistry.toPlayer(new S2CRequestRenderData(disc),player);
    }

}
