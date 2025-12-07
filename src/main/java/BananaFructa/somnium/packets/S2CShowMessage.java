package BananaFructa.somnium.packets;

import BananaFructa.somnium.GuiHandler;
import BananaFructa.somnium.Utils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.function.Supplier;

public class S2CShowMessage {

    public String text;

    public S2CShowMessage(String text) {
        this.text = text;
    }

    public S2CShowMessage(FriendlyByteBuf buf) {
        text = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(text);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        Minecraft.getInstance().execute(() -> {
            GuiHandler.scheduleMessage(text);
        });
    }
}
