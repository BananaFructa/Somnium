package BananaFructa.somnium;

import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import BananaFructa.somnium.gamelinking.LinkedGameDefinitions;
import BananaFructa.somnium.packets.PacketRegistry;
import BananaFructa.somnium.packets.S2CUpdateScheduleData;
import BananaFructa.somnium.service.ChainOfAgentsContextProcessor;
import BananaFructa.somnium.service.OllamaServiceHandler;
import BananaFructa.somnium.service.TaskScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

// This ended up not being used for now
@Mod.EventBusSubscriber
public class EventHandler {

    private boolean samePlayer(Player first, Player second) {
        return first.getUUID().equals(second.getUUID());
    }

    final static long timeout = 20*20;

    static HashMap<UUID,Long> dropTimers = new HashMap<>();
    static HashMap<UUID,Long> burnTimers = new HashMap<>();
    static HashMap<UUID,List<ItemStack>> droppedItems = new HashMap<>();
    static HashMap<UUID,List<ItemStack>> burned = new HashMap<>();

    static int schUpdateTimer = 10;
    static int connectionCheckTimer = 100;
    public static boolean serviceIsDown = false;
    public static List<String> missingModels = new ArrayList<>();

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Somnium.INSTANCE.init(event.getServer());
        missingModels = getMissingModels();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            if (serviceIsDown) {
                player.sendSystemMessage(Component.literal("[\u00a76Somnium\u00a7r] Ollama server is \u00a7coffline\u00a7r! Features are unavailable!"));
            } else {
                player.sendSystemMessage(Component.literal("[\u00a76Somnium\u00a7r] Ollama server is \u00a7aonline\u00a7r!"));
                if (!missingModels.isEmpty()) {
                    sendMissingModels(player);
                }
            }
        }
    }

    public static void playActionSounds(ServerPlayer player) {
        if (player.level().isClientSide) return;
        for (int i = 0;i < 20;i++) { // about right
            RandomSource source = player.level().random;
            player.level().playSound(null, player.getX() +(5-10*source.nextFloat()),player.getY()+(5-10*source.nextFloat()),player.getZ()+(5-10*source.nextFloat()), SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
        }
    }

    public static List<String> getMissingModels() {
        return Utils.getMissingModels(Somnium.INSTANCE.ollamaService,Config.ollamaDescriptorModel,Config.ollamaFilterModel,Config.ollamaContextGeneratorModel,Config.ollamaCoderModel,Config.embeddingModel);
    }

    public static List<String> compareMissingModels(List<String> old, List<String> newList) {
        return old.stream().filter(o->(newList.stream().noneMatch(e->e.equals(o)))).toList();
    }

    // TODO: this should also be in the logs
    public static void sendMissingModels(ServerPlayer player) {
        if (!missingModels.isEmpty()) {
            player.sendSystemMessage(Component.literal("[\u00a76Somnium\u00a7r] The following required models are \u00a7cmissing\u00a7r from the Ollama library! For somnium to work install the models below and restart the game."));
            String modelList = "";
            for (int i = 0;i < missingModels.size();i++) {
                modelList += "\u00a7c" + missingModels.get(i) + "\u00a7r";
                if (i != missingModels.size() - 1) modelList += ", ";
            }
            player.sendSystemMessage(Component.literal("[\u00a76Somnium\u00a7r] " + modelList));
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {

        TaskScheduler.update();

        schUpdateTimer--;
        if (schUpdateTimer == 0) {
            if (!TaskScheduler.tasks.isEmpty()) {
                S2CUpdateScheduleData schUpdate = new S2CUpdateScheduleData(TaskScheduler.info(), TaskScheduler.times());
                for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                    PacketRegistry.toPlayer(schUpdate, player);
                }
            }
            schUpdateTimer = 10;
        }

        connectionCheckTimer--;
        if (connectionCheckTimer == 0) {
            connectionCheckTimer = 100;
            boolean down = Somnium.INSTANCE.ollamaService.isDown();
            if (down && !serviceIsDown) {
                for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                    player.sendSystemMessage(Component.literal("[\u00a76Somnium\u00a7r] Ollama server is \u00a74offline\u00a7r! Features are unavailable!"));
                }
                Somnium.LOGGER.error("Ollama server is \u00a74offline\u00a7r! Features are unavailable!");
            }
            if (!down && serviceIsDown) {
                Somnium.LOGGER.info("Ollama server is \u00a72online\u00a7r!");
                for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                    player.sendSystemMessage(Component.literal("[\u00a76Somnium\u00a7r] Ollama server is \u00a72online\u00a7r!"));
                    missingModels = getMissingModels();
                }
            }

            serviceIsDown = down;
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity) {
                    ItemStack stack = ((LivingEntity) entity).getItemInHand(InteractionHand.MAIN_HAND);
                    if (stack != null) GameLinkingHandler.triggerItemImpl(stack,(LivingEntity) entity,"on_tick_impl");
                }
            }
        }

        for (UUID uuid : dropTimers.keySet()) {
            long currentDrop = dropTimers.get(uuid);
            if (currentDrop > 0) {
                dropTimers.put(uuid, currentDrop - 1);
                if (currentDrop == 1) {
                    droppedItems.get(uuid).clear();
                }
            }
        }
        for (UUID uuid : burnTimers.keySet()) {
            long currentBurn = burnTimers.get(uuid);
            if (currentBurn > 0) {
                burnTimers.put(uuid, currentBurn - 1);
                if (currentBurn == 1) {
                    burned.get(uuid).clear();
                }
            }
        }
    }

    @SubscribeEvent
    public static void itemDropped(ItemTossEvent event) {
        if (event.getPlayer().level().isClientSide) return;
        UUID uuid = event.getPlayer().getUUID();
        if (!droppedItems.containsKey(uuid)) {
            droppedItems.put(uuid,new ArrayList<>());
        }
        dropTimers.put(uuid,timeout);
        droppedItems.get(uuid).add(event.getEntity().getItem());
    }

    public static void itemBurned(ItemStack stack) {
        for (UUID uuid : droppedItems.keySet()) {
            List<ItemStack> stacks = droppedItems.get(uuid);
            for (ItemStack burnedStack : stacks) {
                if (burnedStack == stack) {
                    if (!burned.containsKey(uuid)) burned.put(uuid,new ArrayList<>());
                    burned.get(uuid).add(burnedStack);
                    burnTimers.put(uuid,timeout);
                }
            }
        }
    }

    public static String getItemsBurnedFor(UUID player) {
        if (burned.containsKey(player)) {
            return getTextListFromItemList(burned.get(player));
        } else {
            return "None";
        }
    }

    public static String getTextListFromItemList(List<ItemStack> items) {
        if (items.isEmpty()) return "None";
        HashMap<String,Integer> counts = new HashMap<>();
        for (ItemStack i : items) {
            MutableComponent comp =  Component.empty().append(i.getItem().getName(i));
            String name = comp.getString();
            if (counts.containsKey(name)) counts.put(name,counts.get(name) + i.getCount());
            else counts.put(name,i.getCount());
        }
        String text = "";
        boolean first = true;
        for (String name : counts.keySet()) {
            if (!first) {
                text += ", ";
            } else {
                first = false;
            }
            text += name + " x" + counts.get(name);
        }
        return text;
    }

    public static void potionDrunk(LivingEntity entity, ItemStack potion) {
        if (entity.level().isClientSide) return;
        ItemStack copy = potion.copy();
        GameLinkingHandler.triggerItemImpl(copy,entity,"on_use_impl");
    }

    public static void entityAte(LivingEntity entity, ItemStack stack) {
        if (entity.level().isClientSide) return;
        ItemStack copy = stack.copy();
        GameLinkingHandler.triggerItemImpl(copy,entity,"on_use_impl");
    }

    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level().isClientSide) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEdible() || stack.getItem() instanceof PotionItem) return;
        GameLinkingHandler.triggerItemImpl(stack,(ServerPlayer) event.getEntity(),"on_use_impl");
    }

    public static void onBlockPlaced(Player player, ItemStack stack) {
        if (player.level().isClientSide) return;
        if (stack.getItem() instanceof BlockItem) {
            GameLinkingHandler.triggerItemImpl(stack,(ServerPlayer) player,"on_use_impl");
        }
    }

    public static void hoooooooorn(Player player, ItemStack stack) {
        if (player.level().isClientSide) return;
        Somnium.INSTANCE.universalContext.actionTrigger((ServerPlayer) player, ActionTrigger.HORN);
    }

}
