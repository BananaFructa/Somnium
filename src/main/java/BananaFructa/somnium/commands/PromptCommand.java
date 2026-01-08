package BananaFructa.somnium.commands;

import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import BananaFructa.somnium.gamelinking.objects.Python_Entity;
import BananaFructa.somnium.service.OllamaTask;
import BananaFructa.somnium.service.TaskScheduler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;

public class PromptCommand {
///prompt_coder Create an item called crystal of health that heals the player 1 point each second for 20 seconds
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("prompt_coder").requires((sender)->{
            return sender.hasPermission(2); // Technically this info is also available to the player, but this makes sure that its not easy to cheat it
        }).then(Commands.argument("prompt", StringArgumentType.greedyString()).executes((commandContext -> {
            if (!(commandContext.getSource().getEntity() instanceof LivingEntity)) return 1;
            String prompt = StringArgumentType.getString(commandContext,"prompt");
            OllamaTask task = new OllamaTask("Item Generation Command") {
                @Override
                public void run() {
                    nextStage();
                    String code = Somnium.INSTANCE.universalContext.generateCode(prompt);
                    System.out.println(code);
                    commandContext.getSource().getServer().execute(()->{
                        GameLinkingHandler.runPythonCode(GameLinkingHandler.globalStorageKey,null,code,"main",new Python_Entity(commandContext.getSource().getEntity().getUUID()));
                    });
                }

                @Override
                public String[] stages() {
                    return new String[]{"Code Generation"};
                }
            };
            TaskScheduler.schedule(task);
            return 1;
        })));
    }

}
