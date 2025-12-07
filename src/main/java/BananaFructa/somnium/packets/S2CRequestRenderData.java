package BananaFructa.somnium.packets;

import BananaFructa.somnium.Utils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;
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

    public static NativeImage resize(NativeImage original) {

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        int newWidth = 512;
        float scaleRatio = (float)newWidth / originalWidth;
        int newHeight = (int)(originalHeight * scaleRatio);

        NativeImage resized = new NativeImage(newWidth, newHeight, false);

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int srcX = (int)(x / scaleRatio);
                int srcY = (int)(y / scaleRatio);

                // To avoid approximation shenanigans
                if (srcX < originalWidth && srcY < originalHeight) {
                    int color = original.getPixelRGBA(srcX, srcY);
                    resized.setPixelRGBA(x, y, color);
                }
            }
        }

        return resized;
    }

    public static Method m = Utils.getDeclaredMethod(Minecraft.class,"m_91383_",boolean.class);

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        Minecraft.getInstance().execute(() -> {
            // Hide gui
            boolean prev = Minecraft.getInstance().options.hideGui;
            Minecraft.getInstance().options.hideGui = true;
            try {
                // Render the game again
                m.invoke(Minecraft.getInstance(),true);
            } catch (Exception err) {
                err.printStackTrace();
            }
            // Get the rendered screen
            NativeImage renderedGame = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
            Minecraft.getInstance().options.hideGui = prev;
            // Re-render the screen with the gui back on
            try {
                m.invoke(Minecraft.getInstance(),true);
            } catch (Exception err) {
                err.printStackTrace();
            }
            // This method is not the most optimal as it renders the screen twice, but I can't be bothered
            try {
                NativeImage scaled = resize(renderedGame);
                PacketRegistry.toServer(new C2SRenderData(CoACaller,scaled.asByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
