package BananaFructa.somnium.packets;

import BananaFructa.somnium.GuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class S2CShowMessageHandler {

    public static void handle(S2CShowMessage message) {
        Minecraft.getInstance().execute(() -> {
            GuiHandler.scheduleMessage(message.text);
        });
    }

}
