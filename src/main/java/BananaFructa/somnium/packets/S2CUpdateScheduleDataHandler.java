package BananaFructa.somnium.packets;

import BananaFructa.somnium.GuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class S2CUpdateScheduleDataHandler {

    public static void handle(S2CUpdateScheduleData message) {
        Minecraft.getInstance().execute(() -> {
            GuiHandler.updateTimeInfo(message.text,message.times);
        });
    }

}
