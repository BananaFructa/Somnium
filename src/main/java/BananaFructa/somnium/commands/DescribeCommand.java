package BananaFructa.somnium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DescribeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("describe").requires((sender)->{
            return sender.hasPermission(2); // Technically this info is also available to the player, but this makes sure that its not easy to cheat it
        }).executes((commandContext -> {
            Player player = ((Player)commandContext.getSource().getEntity());
            ItemStack stack = player.getMainHandItem();
            if (!stack.hasTag()) {
                player.sendSystemMessage(Component.literal("Not holding an item with somnium modifiers."));
            } else {
                CompoundTag tag = stack.getTag();
                if (!tag.contains("somnium_item")) player.sendSystemMessage(Component.literal("Not holding an item with somnium modifiers."));
                else {
                    CompoundTag somnium = (CompoundTag) tag.get("somnium_item");
                    player.sendSystemMessage(Component.literal("Somnium item NBT data:"));
                    if (somnium.contains("id")) {
                        player.sendSystemMessage(Component.literal("- id: " + somnium.getInt("id")));
                    }
                    if (somnium.contains("on_use_impl")) {
                        player.sendSystemMessage(Component.literal("- on_use_impl: " + somnium.getString("on_use_impl")));
                    } else {
                        player.sendSystemMessage(Component.literal("- on_use_impl: None"));
                    }
                    if (somnium.contains("on_tick_impl")) {
                        player.sendSystemMessage(Component.literal("- on_tick_impl: " + somnium.getString("on_tick_impl")));
                    } else {
                        player.sendSystemMessage(Component.literal("- on_tick_impl: None"));
                    }
                    if (somnium.contains("code")) {
                        player.sendSystemMessage(Component.literal("Python Implementation:"));
                        String code = somnium.getString("code");
                        for (String line: code.split("\n")) {
                            player.sendSystemMessage(Component.literal(line));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("No code."));
                    }
                }
            }
            return 1;
        }));
    }

}
