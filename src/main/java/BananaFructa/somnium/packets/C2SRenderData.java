package BananaFructa.somnium.packets;

import BananaFructa.somnium.Somnium;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SRenderData {

    byte[] bytes;
    long CoACaller;

    public C2SRenderData(long CoACAller, byte[] bytes) {
        this.bytes = bytes;
        this.CoACaller = CoACAller;
    }

    public C2SRenderData(FriendlyByteBuf buf) {
        bytes = buf.readByteArray();
        CoACaller = buf.readLong();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByteArray(bytes);
        buf.writeLong(CoACaller);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().getSender().getServer().execute(new Runnable() {
            @Override
            public void run() {
                // TODO: check later about CoACaller
                Somnium.INSTANCE.universalContext.renderDataArrived(contextSupplier.get().getSender(), bytes);

            }
        });
    }
}
