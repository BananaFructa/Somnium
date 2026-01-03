package BananaFructa.somnium.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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
        S2CShowMessageHandler.handle(this);
    }
}
