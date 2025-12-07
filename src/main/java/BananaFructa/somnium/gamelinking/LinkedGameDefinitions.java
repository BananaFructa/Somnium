package BananaFructa.somnium.gamelinking;

import BananaFructa.somnium.Config;
import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.gamelinking.objects.Python_EffectModifier;
import BananaFructa.somnium.mechanics.items.ProgrammableItem;
import BananaFructa.somnium.mechanics.items.ProgrammableItemProvider;
import BananaFructa.somnium.packets.PacketRegistry;
import BananaFructa.somnium.packets.S2CShowMessage;
import BananaFructa.somnium.pyinterpreter.ShadowedPythonCode;
import BananaFructa.somnium.pyinterpreter.objects.*;
import BananaFructa.somnium.pyinterpreter.objects.Python_Number;
import BananaFructa.somnium.service.OllamaTask;
import BananaFructa.somnium.service.TaskScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LinkedGameDefinitions {

    public static boolean registryNamesLoaded = false;

    public static List<Tuple<ResourceLocation,List<Double>>> blockResources = new ArrayList<>();
    public static HashMap<String, Block> blockNamingCache = new HashMap<>();

    public static List<Tuple<ResourceLocation,List<Double>>> itemResources = new ArrayList<>();
    public static HashMap<String, Item> itemNamingCache = new HashMap<>();

    //private static Sift4 stringMatch = new Sift4();

    ///prompt_coder Give the player an item called "The candle" that if used around gold blocks it gives the player another item called "The Wind". The latter item when used it increases player velocity by 50% for 50 seconds

    public static void loadRegistryNames() {
        if (registryNamesLoaded) return;
        List<ResourceLocation> blockResourceLocations = ForgeRegistries.BLOCKS.getKeys().stream().toList();
        List<ResourceLocation> itemResourceLocations = ForgeRegistries.ITEMS.getKeys().stream().toList();

        List<String> blockNames = ForgeRegistries.BLOCKS.getKeys().stream().map(x->x.toString()).toList();
        List<String> itemNames = ForgeRegistries.ITEMS.getKeys().stream().map(x->x.toString()).toList();

        OllamaTask task = new OllamaTask("Item & Block Name Registry") {
            @Override
            public void run() {
                nextStage();
                List<List<Double>> blockEmbeddings = Somnium.INSTANCE.ollamaService.embed(Config.embeddingModel,blockNames);
                List<List<Double>> itemEmbeddings = Somnium.INSTANCE.ollamaService.embed(Config.embeddingModel,itemNames);

                for (int i = 0;i < blockNames.size();i++) {
                    blockResources.add(new Tuple<>(blockResourceLocations.get(i),blockEmbeddings.get(i)));
                }

                for (int i = 0;i < itemNames.size();i++) {
                    itemResources.add(new Tuple<>(itemResourceLocations.get(i),itemEmbeddings.get(i)));
                }

                registryNamesLoaded = true;
            }

            @Override
            public String[] stages() {
                return new String[]{"Name Vectorisation"};
            }
        };
        TaskScheduler.schedule(task);
    }

    public static double secondNormSq(List<Double> first, List<Double> second) {
        double sum = 0;
        for (int i = 0;i < first.size();i++) {
            sum += Math.pow(first.get(i) - second.get(i),2);
        }
        return sum;
    }

    public static Block getBlockFromFreeForm(String freeFormName) {
        if (blockNamingCache.containsKey(freeFormName)) return blockNamingCache.get(freeFormName);
        List<Double> freeFormVec = Somnium.INSTANCE.ollamaService.embed(Config.embeddingModel,freeFormName);
        ResourceLocation min = blockResources.stream().min((first,second)->{
            double v = secondNormSq(first.getB(),freeFormVec) - secondNormSq(second.getB(),freeFormVec);
            if (v == 0) return 0;
            return v < 0 ? -1 : 1;
        }).get().getA();
        Block block = ForgeRegistries.BLOCKS.getDelegate(min).get().value();;
        blockNamingCache.put(freeFormName,block);
        return block;
    }

    public static Item getItemFromFreeForm(String freeFormName) {
        if (itemNamingCache.containsKey(freeFormName)) return itemNamingCache.get(freeFormName);
        List<Double> freeFormVec = Somnium.INSTANCE.ollamaService.embed(Config.embeddingModel,freeFormName);
        ResourceLocation min = itemResources.stream().min((first,second)->{
            double v = secondNormSq(first.getB(),freeFormVec) - secondNormSq(second.getB(),freeFormVec);
            if (v == 0) return 0;
            return v < 0 ? -1 : 1;
        }).get().getA();
        Item item = ForgeRegistries.ITEMS.getDelegate(min).get().value();;
        itemNamingCache.put(freeFormName,item);
        return item;
    }

    @PythonMethodLink(docs = """
    # Return a random float between 0 and 1""",order = 0)
    public static Python_Object random(ShadowedPythonCode caller) {
        return new Python_Number((float)Math.random());
    }

    @PythonMethodLink(docs = """
    # Return float world time in seconds, starts from when the world is first created""",order = 1)
    public static Python_Object world_timer(ShadowedPythonCode caller) {
        return new Python_Number(GameLinkingHandler.target.level().getGameTime() / 20.0f);
    }

    @PythonMethodLink(docs = """
    # Return the player velocity, 1 is walking, 2 is sprinting, independent of actual speed factor""",order = 2)
    public static Python_Object get_player_current_velocity(ShadowedPythonCode caller) {
        return new Python_Number((float)GameLinkingHandler.target.getDeltaMovement().length());
    }

    @PythonMethodLink(docs = """
    # @return Returns the player health (between 0 and 20)""",order = 3)
    public static Python_Object get_player_health(ShadowedPythonCode caller) {
        return new Python_Number(GameLinkingHandler.target.getHealth());
    }

    @PythonMethodLink(docs = """
    # @return Returns the player satiation (between 0 and 20)""",order = 4)
    public static Python_Object get_player_hunger(ShadowedPythonCode caller) {
        if (GameLinkingHandler.target instanceof ServerPlayer) {
            return new Python_Number(((ServerPlayer)GameLinkingHandler.target).getFoodData().getFoodLevel());
        }
        return new Python_Number(0);
    }

    /*@PythonMethodLink(docs = """
    # @param amount = absolute amount to damage the player. A player has 20 health.""",order = 5)
    public static Python_Object damage_player(ShadowedPythonCode caller, Python_Number amount) {
        GameLinkingHandler.target.hurt(GameLinkingHandler.target.damageSources().magic(),amount.anyAsFloat());
        return Python_NoneType.None;
    }*/

    @PythonMethodLink(docs = """
    # @param amount = Absolute amount to change player health. Positive values heal , negative values damage, A player has 20 health.""",order = 6)
    public static Python_Object add_health_player(ShadowedPythonCode caller, Python_Number amount) {
        if (amount.anyAsFloat() >= 0) {
            GameLinkingHandler.target.heal(amount.anyAsFloat());
        } else {
            GameLinkingHandler.target.hurt(GameLinkingHandler.target.damageSources().magic(),amount.anyAsFloat());
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @param = Absolute amount to add to player satiation. Positive values increase satiation, negative values make the player hungry A player has 20 points.""",order = 7)
    public static Python_Object add_satiation_player(ShadowedPythonCode caller, Python_Number amount) {
        if (GameLinkingHandler.target instanceof ServerPlayer) {
            ((ServerPlayer)GameLinkingHandler.target).getFoodData().eat((int) amount.anyAsFloat(), 0);
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @param amount = Absolute amount to damage the entities. A player has 20 health.
    # @param radius = Radius in blocks""",order = 8)
    public static Python_Object damage_entities_around_player(ShadowedPythonCode caller, Python_Number amount, Python_Number radius){
        AABB aabb = new AABB(
                GameLinkingHandler.target.getX() + radius.anyAsFloat(), GameLinkingHandler.target.getY() + radius.anyAsFloat(), GameLinkingHandler.target.getZ() + radius.anyAsFloat(),
                GameLinkingHandler.target.getX() - radius.anyAsFloat(), GameLinkingHandler.target.getY() - radius.anyAsFloat(), GameLinkingHandler.target.getZ() - radius.anyAsFloat()
        );
        GameLinkingHandler.target.level().getEntities(GameLinkingHandler.target,aabb).stream().forEach((e)->{
            if (e instanceof LivingEntity) {
                e.hurt(e.damageSources().magic(), amount.anyAsFloat());
            }
        });
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @param amount = Absolute amount to heal the entities. A player has 20 health.
    # @param radius = Radius in blocks""",order = 9)
    public static Python_Object heal_entities_around_player(ShadowedPythonCode caller, Python_Number amount, Python_Number radius){
        AABB aabb = new AABB(
                GameLinkingHandler.target.getX() + radius.anyAsFloat(), GameLinkingHandler.target.getY() + radius.anyAsFloat(), GameLinkingHandler.target.getZ() + radius.anyAsFloat(),
                GameLinkingHandler.target.getX() - radius.anyAsFloat(), GameLinkingHandler.target.getY() - radius.anyAsFloat(), GameLinkingHandler.target.getZ() - radius.anyAsFloat()
        );
        GameLinkingHandler.target.level().getEntities(GameLinkingHandler.target,aabb).stream().forEach((e)->{
            if (e instanceof LivingEntity) {
                ((LivingEntity) e).heal(amount.anyAsFloat());
            }
        });
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @return Returns the number of specific blocks around the player
    # @param block_name = Name of the block
    # @param radius = Radius around the player in which to search""",order = 10)
    public static Python_Object get_block_nearby(ShadowedPythonCode caller, Python_String block_name, Python_Number radius) {
        Block b = getBlockFromFreeForm(block_name.s);
        int r = radius.anyAsInt();
        int count = 0;
        for (int i = -r;i < r;i++) {
            for (int j = -r;j < r;j++) {
                for (int k = -r;k < r;k++) {
                    Vec3 v = GameLinkingHandler.target.position().add(i,j,k);
                    if (GameLinkingHandler.target.level().getBlockState(new BlockPos((int)v.x,(int)v.y,(int)v.z)).getBlock().equals(b)) count++;
                }
            }
        }
        return new Python_Number(count);
    }

    @PythonMethodLink(docs = """
    # @brief Breaks specific blocks around the player
    # @param block_name = Name of the block
    # @param radius = Radius around the player in which to search""",order = 11)
    public static Python_Object break_block_nearby(ShadowedPythonCode caller, Python_String block_name, Python_Number radius) {
        Block b = getBlockFromFreeForm(block_name.s);
        int r = radius.anyAsInt();
        for (int i = -r;i < r;i++) {
            for (int j = -r;j < r;j++) {
                for (int k = -r;k < r;k++) {
                    Vec3 v = GameLinkingHandler.target.position().add(i,j,k);
                    BlockPos bp = new BlockPos((int)v.x,(int)v.y,(int)v.z);
                    if (GameLinkingHandler.target.level().getBlockState(new BlockPos((int)v.x,(int)v.y,(int)v.z)).getBlock().equals(b)) {
                        // TODO: currently targeting the 0 type
                        GameLinkingHandler.target.level().setBlockAndUpdate(bp,Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief Reveals to the player facts about the internal magic system""",order = 12)
    public static Python_Object give_magical_insight(ShadowedPythonCode caller) {
        // TODO: supposed to give some info related to the internal LLM context
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief Randomly teleports the player""",order = 13)
    public static Python_Object random_teleport_player(ShadowedPythonCode caller) {
        // TODO: implement this, teleport the target randomly in a range of 50 blocks on the ground
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief deletes a random item from the player inventory""",order = 14)
    public static Python_Object delete_random_item(ShadowedPythonCode caller) {
        // TODO: implement
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief give a random item to the player""",order = 15)
    public static Python_Object give_random_item(ShadowedPythonCode caller) {
        // TODO: implement
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief Retrieved to the player written information around their environment""",order = 16)
    public static Python_Object transcribe_information(ShadowedPythonCode caller) {
        // TODO: somehow give the player all written information from the surrounding environment
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief sets the block the player looks at on fire""",order = 17)
    public static Python_Object set_on_fire(ShadowedPythonCode caller) {
        GameLinkingHandler.target.setRemainingFireTicks(20*5);
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief It gives a previously created item to the player
    # @param item_id = String identification id of the registered item, use register_item(...)
    # @param quantity = Amount of the item to give""",order = 18)
    public static Python_Object give_item_player(ShadowedPythonCode caller, Python_String item_id, Python_Number quantity) {
        if (!(GameLinkingHandler.target instanceof ServerPlayer)) return Python_NoneType.None;
        ItemStack stack = ProgrammableItemProvider.getItem(item_id.s, quantity.anyAsInt());
        ((ServerPlayer)GameLinkingHandler.target).addItem(stack);
        return Python_NoneType.None;
    }

    /*@PythonMethodLink(docs = """
    # @brief Creates an item
    # @param name = Name of the potion
    # @param desc = Description of the potion effect
    # @param on_use_impl = Function which implements the effects of using the item (right click), cannot be a string
    # @param on_tick_impl = Function that runs every tick, every 0.05 seconds, cannot be a string
    # @param minecraft_item = The name of the minecraft items that should be displayed as the item icon, the string can be in free natural form, for potions use minecraft:potion
    # @returns Returns an item object""",order = 19)
    public static Python_Object create_item(ShadowedPythonCode caller, Python_String name, Python_String description, Python_String minecraft_item, Python_Object on_use_impl, Python_Object on_tick_impl) {
        String onTick = null;
        String onUse = null;
        if (on_tick_impl instanceof Python_Function) onTick = ((Python_Function) on_tick_impl).name;
        if (on_use_impl instanceof Python_Function) onUse = ((Python_Function) on_use_impl).name;

        return new Python_Item(name.s,description.s,minecraft_item.s,caller.originalCode,onUse,onTick);
    }*/
    @PythonMethodLink(docs = """
    # @brief Creates an item
    # @param item_id = String id to identify the item definition
    # @param name = Name of the item
    # @param description = Description of the item
    # @param minecraft_item = The name of the minecraft items that should be displayed as the item icon, the string can be in free natural form, for potions use minecraft:potion
    # @param on_use_impl = Function which implements the effects of using the item (right click), cannot be a string
    # @param on_tick_impl = Function that runs every tick, every 0.05 seconds, cannot be a string""",order = 19)
    public static Python_Object create_item(ShadowedPythonCode caller,Python_String item_id, Python_String name, Python_String description, Python_String minecraft_item, Python_Object on_use_impl, Python_Object on_tick_impl) {
        String onTick = null;
        String onUse = null;
        if (on_tick_impl instanceof Python_Function) onTick = ((Python_Function) on_tick_impl).name;
        if (on_use_impl instanceof Python_Function) onUse = ((Python_Function) on_use_impl).name;
        ProgrammableItemProvider.createItem(item_id.s,name.s,description.s,minecraft_item.s,caller.originalCode,onUse,onTick);

        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @param effect_name = Name of the registered effect to give to the player, use register_effect(...) first
    # @param duration = Duration in seconds (floating point number)""",order = 21)
    public static Python_Object give_effect_player(ShadowedPythonCode caller, Python_String effect_id, Python_Number duration) {
        MobEffect effect = Somnium.INSTANCE.effectProvider.getEffect(effect_id.s);
        if (effect != null) {
            GameLinkingHandler.target.addEffect(new MobEffectInstance(effect, (int) (duration.anyAsFloat() * 20)) {
                @Override
                public boolean tick(LivingEntity p_19553_, Runnable p_19554_) {
                    boolean ret =  super.tick(p_19553_, p_19554_);
                    p_19554_.run();
                    return ret;
                }
            });
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief Create an effect with a certain implementation
    # @param effect_id = String id to identify the effect definition
    # @param name = Display name that the player sees attached to the effect
    # @param description = Description of the effect
    # @param effect_impl_tick = Function effect implementation, runs every tick, must return a list of modifiers""",order = 22)
    public static Python_Object create_effect(ShadowedPythonCode caller, Python_String effect_id, Python_String name, Python_String description, Python_Object effect_impl_tick) {
        if (effect_impl_tick instanceof Python_Function) {
            Somnium.INSTANCE.effectProvider.createEffect(effect_id.s,name.s,caller.originalCode,((Python_Function) effect_impl_tick).name);
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @param amount_per_second = Absolute amount of health change. Positives heal, negatives damage. A player has 20 health.
    # @return Returns a modifier object that can be applied to an effect""",order = 23)
    public static Python_Object modifier_creator_player_health(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.HEALTH,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @param amount_per_second = Absolute amount of satiation change. Positives satiate, negatives hunger. A player has 20 hunger points.
    # @return Returns a modifier object that can be applied to an effect""",order = 24)
    public static Python_Object modifier_creator_player_saturation(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.SATURATION,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @param percent = Percentage by which player speed is modified. >0 increases the speed <0 lowers it.
    # @return Returns a modifier object that can be applied to an effect""",order = 25)
    public static Python_Object modifier_creator_player_speed(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.SPEED,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @param percent = Percentage by which the player is protected from damage. >0 increases protection <0 makes the player more vulnerable
    # @return Returns a modifier object that can be applied to an effect""",order = 26)
    public static Python_Object modifier_creator_resistance(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.RESISTANCE,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @param factor = Percentage by which the player is protected from fire damage. >0 increases protection <0 makes the player more vulnerable
    # @return Returns a modifier object that can be applied to an effect""",order = 27)
    public static Python_Object modifier_creator_fire_resistance(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.FIRE_RESISTANCE,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @param factor = Percentage by which the player jump height is changed. >0 increases jump <0 decreases jump
    # @return Returns a modifier object that can be applied to an effect""",order = 28)
    public static Python_Object modifier_creator_player_jump(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.JUMP,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @param factor = Percentage by which the player mine speed is changed. >0 increases mine <0 decreases mine
    # @return Returns a modifier object that can be applied to an effect""", order = 29)
    public static Python_Object modifier_creator_player_mining_speed(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.MINING_SPEED,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(docs = """
    # @return Returns a modifier object that can be applied to an effect""",order = 30)
    public static Python_Object modifier_creator_blind_player(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.BLIND,0);
    }

    @PythonMethodLink(docs = """
    # @return Returns a modifier object that can be applied to an effect""",order = 31)
    public static Python_Object modifier_creator_invisible_player(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.INVISIBLE,0);
    }

    @PythonMethodLink(docs = """
    # @brief Allows the player to pass through solid blocks
    # @return Returns a modifier object that can be applied to an effect""",order = 32)
    public static Python_Object modifier_creator_block_phasing(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.PHASING,0);
    }

    @PythonMethodLink(docs = """
    # @brief Disrupts redstone circuitry around player
    # @return Returns a modifier object that can be applied to an effect""",order = 33)
    public static Python_Object modifier_creator_disrupt_redstone(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.DISRUPT_REDSTONE,0);
    }

    @PythonMethodLink(docs = """
    # @brief Allows the player to see valuables
    # @return Returns a modifier object that can be applied to an effect""",order = 34)
    public static Python_Object modifier_creator_show_valuables(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.SHOW_VALUABLES,0);
    }

    @PythonMethodLink(docs = """
    # @brief Allows the player to see dangers
    # @return Returns a modifier object that can be applied to an effect""",order = 35)
    public static Python_Object modifier_creator_reveal_dangers(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.REVEAL_DANGERS,0);
    }

    @PythonMethodLink(docs = """
    # @brief Allows the player to see through blocks
    # @return Returns a modifier object that can be applied to an effect""",order = 36)
    public static Python_Object modifier_creator_see_through_blocks(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.SEE_THROUGH_BLOCKS,0);
    }

    @PythonMethodLink(docs = """
    # @brief Saves an integer value
    # @parameter string_key = String key for the key-int pair
    # @parameter value = The value to be stored""",order = 37)
    public static Python_Object save_local_data_number(ShadowedPythonCode caller, Python_String string_key,Python_Number value) {
        if (GameLinkingHandler.currentCacheKey == null) throw new RuntimeException("Cannot save local data by a script with no cache key! Please report.");
        Somnium.INSTANCE.worldData.localStoreInt(GameLinkingHandler.currentCacheKey, string_key.s, value.anyAsInt());
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @parameter string_key = String key for the key-int pair
    # @parameter def = The value that is returned if the key is not found
    # @return Returns the value store for the key, 0 if no key is found""",order = 38)
    public static Python_Object get_local_data_number(ShadowedPythonCode caller, Python_String string_key, Python_Number def) {
        if (GameLinkingHandler.currentCacheKey == null) throw new RuntimeException("Cannot get local data by a script with no cache key! Please report.");
        return new Python_Number(Somnium.INSTANCE.worldData.localGetInt(GameLinkingHandler.currentCacheKey, string_key.s, def.anyAsInt()));
    }

    @PythonMethodLink(docs = """
    # @brief Spawns particles around the player
    # @param count = Number of particles to spawn around the player
    # @param color = Color in RGB format (e.g. 0xff0000 - red)
    # @param lifetime = Lifetime in seconds""",order = 39)
    public static Python_Object spawn_particle(ShadowedPythonCode caller,Python_Number count, Python_Number color, Python_Number lifetime) {
        int rgb = color.i;
        float r = ((rgb & 0xff0000) >> 16)/255.0f;
        float g = ((rgb & 0x00ff00) >> 8)/255.0f;
        float b = (rgb & 0x0000ff)/255.0f;
        DustParticleOptions options = new DustParticleOptions(new Vector3f(r,g,b),1);
        for (int i = 0;i < count.anyAsInt();i++) {
            int x = (int)GameLinkingHandler.target.getX() + (int)(GameLinkingHandler.target.level().random.nextFloat() * 10 - 5);
            int y = (int)GameLinkingHandler.target.getY() + (int)(GameLinkingHandler.target.level().random.nextFloat() * 10 - 5);
            int z = (int)GameLinkingHandler.target.getZ() + (int)(GameLinkingHandler.target.level().random.nextFloat() * 10 - 5);
            ((ServerLevel)GameLinkingHandler.target.level()).sendParticles(options,x,y,z,0,0,0,0,0);
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(docs = """
    # @brief displays an on-screen message to the player usually reserved for cryptic message after each ritual
    # @param message = String containing the message""",order = 40)
    public static Python_Object on_screen_message(ShadowedPythonCode caller, Python_String message) {
        if (GameLinkingHandler.target instanceof ServerPlayer) {
            PacketRegistry.toPlayer(new S2CShowMessage(message.s), (ServerPlayer) GameLinkingHandler.target);
        }
        return Python_NoneType.None;
    }

}
