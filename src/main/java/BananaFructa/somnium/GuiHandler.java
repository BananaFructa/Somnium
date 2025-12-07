package BananaFructa.somnium;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class GuiHandler {

    static float schInfoCounter = 0;
    static float onScreeMessageCounter = 0;

    static Queue<String> messages = new PriorityQueue<>();
    static boolean finished = false;

    static List<String> schInfo;
    static List<String> times;

    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.hasTag()) {
            if (stack.getTag().contains("somnium_item")) {
                CompoundTag tag = (CompoundTag) stack.getTag().get("somnium_item");
                if (tag.contains("description")) event.getToolTip().add(1,Component.literal("\u00a77" + tag.getString("description")));
            }
        }
    }

    @SubscribeEvent
    public static void onScreen(RenderGuiEvent.Post event) {
        if (onScreeMessageCounter < 11.5*20) onScreeMessageCounter += event.getPartialTick();
        else {
            if (!finished) {
                finished = true;
                messages.poll();
            }
            if (!messages.isEmpty()) {
                onScreeMessageCounter = 0;
                finished = false;
            }
        }
        GuiGraphics graphics = event.getGuiGraphics();
        if (schInfoCounter < 3*20) {
            schInfoCounter += event.getPartialTick();
            if (schInfo != null && times != null) {
                int largestWidth = 0;
                for (int i = 0; i < schInfo.size(); i++) {
                    ClientTooltipComponent component = ClientTooltipComponent.create(Component.translatable(schInfo.get(i)).getVisualOrderText());
                    component.renderText(Minecraft.getInstance().font, 0, i * 12, graphics.pose().last().pose(), graphics.bufferSource());
                    int width = component.getWidth(Minecraft.getInstance().font);
                    if (largestWidth < width) {
                        largestWidth = width;
                    }
                }
                for (int i = 0; i < times.size(); i++) {
                    ClientTooltipComponent component = ClientTooltipComponent.create(Component.translatable(times.get(i)).getVisualOrderText());
                    component.renderText(Minecraft.getInstance().font, largestWidth + 10, (i + 1) * 12, graphics.pose().last().pose(), graphics.bufferSource());
                }
            }
        }
        if (!messages.isEmpty()) {
            renderOnScreen(graphics, messages.peek());
        }
    }

    public static void scheduleMessage(String message) {
        messages.add(message);
    }

    public static void updateTimeInfo(List<String> text, List<String> time) {
        schInfoCounter = 0;
        schInfo = text;
        times = time;
    }

    public static void renderOnScreen(GuiGraphics g, String text) {

        float alpha = 0;
        float height = 1;
        boolean renderText = false;

        if (onScreeMessageCounter < 20) {
            height = 1 - onScreeMessageCounter / (20.0f);
            alpha = 1;
        } else if (onScreeMessageCounter > 9*20 && onScreeMessageCounter < 11 * 20) {
            height = 0;
            alpha = (1 - (onScreeMessageCounter - 9*20)/(2.0f*20));
            renderText = true;
        } else if (onScreeMessageCounter < 9 * 20) {
            height = 0;
            alpha = 1;
            renderText = true;
        }

        ClientTooltipComponent component = ClientTooltipComponent.create(Component.translatable(text).getVisualOrderText());
        int width = component.getWidth(Minecraft.getInstance().font);
        int x = g.guiWidth()/2 - width/2 - 5;
        int y = (int)(g.guiHeight()*(2.6f/4));

        int alphaChannel = ((int)(alpha*0xaa)) << 24;
        RenderSystem.setShaderColor(1,1,1,1);
        TooltipRenderUtil.renderTooltipBackground(g,x,(int)(y + 17.0f/2 * height),width + 10,(int)(17 - 17 * height),400,0x00ffc800 | alphaChannel,0x00ffc800 | alphaChannel,0x00ffbf00 | alphaChannel,0x00ffd900 | alphaChannel);
        if (renderText) {
            RenderSystem.setShaderColor(1, 1, 1, alpha);
            component.renderText(Minecraft.getInstance().font, x + 5, y + 4, g.pose().last().pose(), g.bufferSource());
        }
    }

}
