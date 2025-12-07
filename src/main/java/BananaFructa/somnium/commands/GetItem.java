package BananaFructa.somnium.commands;

import BananaFructa.somnium.mechanics.items.ProgrammableItemProvider;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GetItem {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("somnium_get_item").requires((sender)->{
            return sender.hasPermission(2); // Technically this info is also available to the player, but this makes sure that its not easy to cheat it
        }).then(Commands.argument("somnium_item", StringArgumentType.greedyString()).executes((commandContext -> {
            if (!(commandContext.getSource().getEntity() instanceof ServerPlayer)) return 1;
            String id = StringArgumentType.getString(commandContext,"somnium_item");
            ServerPlayer player = (ServerPlayer)  commandContext.getSource().getEntity();
            ItemStack stack = ProgrammableItemProvider.getItem(id,1);
            if (stack != null) {
                player.addItem(stack);
            }
            return 1;
        })));
    }

}
