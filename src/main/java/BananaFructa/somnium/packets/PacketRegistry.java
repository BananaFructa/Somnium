package BananaFructa.somnium.packets;

import BananaFructa.somnium.Somnium;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistry {

    public static SimpleChannel INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(Somnium.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE.messageBuilder(S2CRequestRenderData.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CRequestRenderData::new)
                .encoder(S2CRequestRenderData::encode)
                .consumerMainThread(S2CRequestRenderData::handle)
                .add();

        INSTANCE.messageBuilder(C2SRenderData.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SRenderData::new)
                .encoder(C2SRenderData::encode)
                .consumerMainThread(C2SRenderData::handle)
                .add();

        INSTANCE.messageBuilder(S2CShowMessage.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CShowMessage::new)
                .encoder(S2CShowMessage::encode)
                .consumerMainThread(S2CShowMessage::handle)
                .add();

        INSTANCE.messageBuilder(S2CUpdateScheduleData.class,3,NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CUpdateScheduleData::new)
                .encoder(S2CUpdateScheduleData::encode)
                .consumerMainThread(S2CUpdateScheduleData::handle)
                .add();

        INSTANCE.messageBuilder(S2CEffectUpdate.class,4,NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CEffectUpdate::new)
                .encoder(S2CEffectUpdate::encode)
                .consumerMainThread(S2CEffectUpdate::handle)
                .add();
    }

    public static <T> void toServer(T message) {
        INSTANCE.sendToServer(message);
    }

    public static <T> void toPlayer(T message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),message);
    }

}
