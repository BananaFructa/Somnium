package BananaFructa.somnium.packets;

import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.mechanics.effects.ProgrammableEffect;
import net.minecraft.client.Minecraft;

public class S2CEffectUpdateHandler {

    public static void handle(S2CEffectUpdate message) {
        if (Minecraft.getInstance().hasSingleplayerServer()) return; // If the server is running on the same side effects are update automatically
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (message.nbt.contains("id")) {
                    int id = message.nbt.getInt("id");
                    ((ProgrammableEffect) (Minecraft.getInstance().player.getEffect(Somnium.INSTANCE.effectProvider.getEffect(id))).getEffect()).readClientNBT(message.nbt);
                }
            }
        });
    }

}
