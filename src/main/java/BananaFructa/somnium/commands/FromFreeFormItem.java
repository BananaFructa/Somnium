package BananaFructa.somnium.commands;

import BananaFructa.somnium.gamelinking.LinkedGameDefinitions;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class FromFreeFormItem {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("from_free_form_item").requires((sender)->{
            return sender.hasPermission(2); // Technically this info is also available to the player, but this makes sure that its not easy to cheat it
        }).then(Commands.argument("free_form", StringArgumentType.greedyString()).executes((commandContext -> {
            String freeForm = StringArgumentType.getString(commandContext,"free_form");
            Item i = LinkedGameDefinitions.getItemFromFreeForm(freeForm);
            Entity e = commandContext.getSource().getEntity();
            if (e instanceof ServerPlayer) {
                ((ServerPlayer)e).sendSystemMessage(Component.literal(i.toString()));
            }
            return 1;
        })));
    }

}
