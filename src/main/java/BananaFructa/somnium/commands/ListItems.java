package BananaFructa.somnium.commands;

import BananaFructa.somnium.mechanics.items.ProgrammableItemProvider;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ListItems {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("somnium_list_items").requires((sender)->{
            return sender.hasPermission(2); // Technically this info is also available to the player, but this makes sure that its not easy to cheat it
        }).executes((commandContext) -> {
            if (!(commandContext.getSource().getEntity() instanceof ServerPlayer)) return 1;
            ServerPlayer player = (ServerPlayer)  commandContext.getSource().getEntity();
            List<String> items = ProgrammableItemProvider.getItems();
            for (String k : items) {
                player.sendSystemMessage(Component.literal(k));
            }
            return 1;
        });
    }

}
