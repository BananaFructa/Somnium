package BananaFructa.somnium.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CUpdateScheduleData {
    public List<String> text = new ArrayList<>();
    public List<String> times = new ArrayList<>();

    public S2CUpdateScheduleData(List<String> text, List<String> times) {
        this.text = text;
        this.times = times;
    }

    public S2CUpdateScheduleData(FriendlyByteBuf buf) {
        int tSize = buf.readInt();
        for (int i = 0;i < tSize;i++) {
            text.add(buf.readUtf());
        }
        int timeSize = buf.readInt();
        for (int i = 0;i < timeSize;i++) {
            times.add(buf.readUtf());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(text.size());
        for (String l : text) {
            buf.writeUtf(l);
        }
        buf.writeInt(times.size());
        for (String l : times) {
            buf.writeUtf(l);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        S2CUpdateScheduleDataHandler.handle(this);
    }
}
