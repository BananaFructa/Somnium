package BananaFructa.somnium.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRequestRenderData {

    public long CoACaller;

    public S2CRequestRenderData(long CoACaller) {
        this.CoACaller = CoACaller;
    }

    public S2CRequestRenderData(FriendlyByteBuf buf) {
        CoACaller = buf.readLong();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(CoACaller);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        S2CRequestRenderDataHandler.handle(this);
    }
}
