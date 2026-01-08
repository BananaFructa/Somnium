package BananaFructa.somnium.gamelinking;

import BananaFructa.somnium.Config;
import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.gamelinking.objects.Python_EffectModifier;
import BananaFructa.somnium.gamelinking.objects.Python_Entity;
import BananaFructa.somnium.mechanics.items.ProgrammableItemProvider;
import BananaFructa.somnium.mechanics.projectiles.ProgrammableProjectileProvider;
import BananaFructa.somnium.mechanics.projectiles.ProgrammableProjectile;
import BananaFructa.somnium.packets.PacketRegistry;
import BananaFructa.somnium.packets.S2CShowMessage;
import BananaFructa.somnium.pyinterpreter.ShadowedPythonCode;
import BananaFructa.somnium.pyinterpreter.objects.*;
import BananaFructa.somnium.pyinterpreter.objects.Python_Number;
import BananaFructa.somnium.service.OllamaTask;
import BananaFructa.somnium.service.TaskScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LinkedGameDefinitions {

    public static boolean registryNamesLoaded = false;

    public static List<Tuple<ResourceLocation,List<Double>>> blockResources = new ArrayList<>();
    public static HashMap<String, Block> blockNamingCache = new HashMap<>();

    public static List<Tuple<ResourceLocation,List<Double>>> itemResources = new ArrayList<>();
    public static HashMap<String, Item> itemNamingCache = new HashMap<>();

    //private static Sift4 stringMatch = new Sift4();

    // TODO: all python object serializable

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

    @PythonMethodLink(
            docs = """
            # Return a random float between 0 and 1""",
            desc = "NONE",
            order = MethodLinkOrder.RANDOM
    )
    public static Python_Object random(ShadowedPythonCode caller) {
        return new Python_Number((float)Math.random());
    }

    /*@PythonMethodLink(
            docs = """
            # Return float world time in seconds, starts from when the world is first created""",
            desc = "NONE",
            order = MethodLinkOrder.WORLD_TIMER
    )
    public static Python_Object world_timer(ShadowedPythonCode caller) {
        return new Python_Number(GameLinkingHandler.target.level().getGameTime() / 20.0f);
    }*/

    private static Entity getEntity(UUID uuid) {
        for (ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e != null) return e;
        }
        return null;
    }

    @PythonMethodLink(
            docs = """
            # @brief Return the entity velocity
            # @param entity = The entity who's velocity to return""",
            desc = "Detect entity velocity",
            order = MethodLinkOrder.GET_PLAYER_CURRENT_VELOCITY
    )
    public static Python_Object get_entity_current_velocity(ShadowedPythonCode caller, Python_Entity entity) {
        return new Python_Number((float)getEntity(entity.entityUUID).getDeltaMovement().length());
    }

    @PythonMethodLink(
            docs = """
            # @return Returns the entity health
            # @param entity = The entity who's health to return""",
            desc="Detect entity health",
            order = MethodLinkOrder.GET_PLAYER_HEALTH
    )
    public static Python_Object get_entity_health(ShadowedPythonCode caller, Python_Entity entity) {
        Entity e = getEntity(entity.entityUUID);
        if (!(e instanceof LivingEntity)) return new Python_Number(0);
        return new Python_Number(((LivingEntity)e).getHealth());
    }

    @PythonMethodLink(
            docs = """
            # @return Returns the entity satiation (between 0 and 20)
            # @param playerEntity = The player entity who's hunger to return""",
            desc = "Detect entity hunger",
            order = MethodLinkOrder.GET_PLAYER_HUNGER
    )
    public static Python_Object get_player_hunger(ShadowedPythonCode caller, Python_Entity playerEntity) {
        Entity e = getEntity(playerEntity.entityUUID);
        if (e instanceof ServerPlayer) {
            return new Python_Number(((ServerPlayer)e).getFoodData().getFoodLevel());
        }
        return new Python_Number(0);
    }

    @PythonMethodLink(
            docs = """
            # @param entity = The entity who's health will be changed
            # @param amount = Absolute amount to change entity's health. Positive values heal , negative values damage, A player has 20 health.""",
            desc = "Heal or damage entities",
            order = MethodLinkOrder.ADD_PLAYER_HEALTH
    )
    public static Python_Object add_health_entity(ShadowedPythonCode caller, Python_Entity entity, Python_Number amount) {
        Entity e = getEntity(entity.entityUUID);
        if (e instanceof LivingEntity) {
            if (amount.anyAsFloat() >= 0) {
                ((LivingEntity)e).heal(amount.anyAsFloat());
            } else {
                System.out.println(amount.anyAsFloat());
                e.hurt(e.damageSources().magic(), -amount.anyAsFloat());
                System.out.println(((LivingEntity) e).getHealth());
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @param playerEntity = The player entity who's hunger will be changed
            # @param amount = Absolute amount to add to player satiation. Positive values increase satiation, negative values make the player hungry A player has 20 points.""",
            desc = "Add saturation to the player",
            order = MethodLinkOrder.ADD_SATIATION_PLAYER
    )
    public static Python_Object add_satiation_player(ShadowedPythonCode caller, Python_Entity playerEntity, Python_Number amount) {
        Entity e = getEntity(playerEntity.entityUUID);
        if (e instanceof ServerPlayer) {
            ((ServerPlayer)e).getFoodData().eat((int) amount.anyAsFloat(), 0);
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @param entity = The entity around which to search for other entities
            # @param radius = Radius in blocks in which to search around the target entity
            # @return Returns a list of entities
            """,
            desc = "Influence entities around the player or other entities",
            order = MethodLinkOrder.GET_ENTITIES_AROUND
    )
    public static Python_Object get_entities_around_entity(ShadowedPythonCode caller, Python_Entity entity, Python_Number radius) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return new Python_List();
        Python_List list = new Python_List();
        AABB aabb = new AABB(
                e.getX() + radius.anyAsFloat(), e.getY() + radius.anyAsFloat(), e.getZ() + radius.anyAsFloat(),
                e.getX() - radius.anyAsFloat(), e.getY() - radius.anyAsFloat(), e.getZ() - radius.anyAsFloat()
        );
        list.elements.addAll(e.level().getEntities(e,aabb).stream().map(en -> new Python_Entity(en.getUUID())).toList());
        return list;
    }

    @PythonMethodLink(
            docs = """
            # @return Returns the number of specific blocks around the entity
            # @param entity = The entity around which to search for blocks
            # @param block_name = Name of the block
            # @param radius = Radius around the entity in which to search""",
            desc = "Detect if blocks of a certain kind are nearby",
            order = MethodLinkOrder.GET_BLOCK_NEARBY
    )
    public static Python_Object get_blocks_nearby(ShadowedPythonCode caller, Python_Entity entity, Python_String block_name, Python_Number radius) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return new Python_Number(0);
        Block b = getBlockFromFreeForm(block_name.s);
        int r = radius.anyAsInt();
        int count = 0;
        for (int i = -r;i < r;i++) {
            for (int j = -r;j < r;j++) {
                for (int k = -r;k < r;k++) {
                    Vec3 v = e.position().add(i,j,k);
                    if (e.level().getBlockState(new BlockPos((int)v.x,(int)v.y,(int)v.z)).getBlock().equals(b)) count++;
                }
            }
        }
        return new Python_Number(count);
    }

    @PythonMethodLink(
            docs = """
            # @brief Breaks all blocks around the entity in a certain radius
            # @param entity = The entity around which to break blocks
            # @param radius = Radius around the entity in which to search""",
            desc = "Break blocks of a certain kind nearby",
            order = MethodLinkOrder.BREAK_ALL_BLOCKS_NEARBY
    )
    public static Python_Object break_all_blocks_nearby(ShadowedPythonCode caller, Python_Entity entity, Python_Number radius) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        int r = radius.anyAsInt();
        for (int i = -r;i < r;i++) {
            for (int j = -r;j < r;j++) {
                for (int k = -r;k < r;k++) {
                    Vec3 v = e.position().add(i,j,k);
                    BlockPos bp = new BlockPos((int)v.x,(int)v.y,(int)v.z);
                    e.level().destroyBlock(bp,e.level().random.nextInt(10)==0);
                }
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Breaks specific blocks around the entity
            # @param entity = The entity around which to break blocks
            # @param block_name = Name of the block
            # @param radius = Radius around the entity in which to search""",
            desc = "Break blocks of a certain kind nearby",
            order = MethodLinkOrder.BREAK_CERTAIN_BLOCKS_NEARBY
    )
    public static Python_Object break_certain_blocks_nearby(ShadowedPythonCode caller, Python_Entity entity, Python_String block_name, Python_Number radius) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        Block b = getBlockFromFreeForm(block_name.s);
        int r = radius.anyAsInt();
        for (int i = -r;i < r;i++) {
            for (int j = -r;j < r;j++) {
                for (int k = -r;k < r;k++) {
                    Vec3 v = e.position().add(i,j,k);
                    BlockPos bp = new BlockPos((int)v.x,(int)v.y,(int)v.z);
                    if (e.level().getBlockState(new BlockPos((int)v.x,(int)v.y,(int)v.z)).getBlock().equals(b)) {
                        e.level().destroyBlock(bp,e.level().random.nextInt(10)==0);
                    }
                }
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Replaces all nearby blocks with a certain block
            @ @param entity = The entity around which to replace blocks
            # @param block_name = Name of the block
            # @param radius = Radius around the player in which to replace""",
            desc = "Turn all blocks nearby into a certain blocks",
            order = MethodLinkOrder.REPLACE_ALL_BLOCKS_NEARBY
    )
    public static Python_Object replace_all_blocks_nearby(ShadowedPythonCode caller, Python_Entity entity, Python_String block_name, Python_Number radius) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        Block b = getBlockFromFreeForm(block_name.s);
        int r = radius.anyAsInt();
        for (int i = -r;i < r;i++) {
            for (int j = -r;j < r;j++) {
                for (int k = -r;k < r;k++) {
                    Vec3 v = e.position().add(i,j,k);
                    BlockPos bp = new BlockPos((int)v.x,(int)v.y,(int)v.z);
                    if (e.level().getBlockState(new BlockPos((int)v.x,(int)v.y,(int)v.z)).isAir()) {
                        e.level().setBlockAndUpdate(bp,b.defaultBlockState());
                    }
                }
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Randomly teleports the player
            # @param entity = The entity to teleport""",
            desc = "Randomly teleport entities",
            order = MethodLinkOrder.RANDOM_TELEPORT_PLAYER
    )
    public static Python_Object random_teleport_entity(ShadowedPythonCode caller, Python_Entity entity) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        ServerLevel level = (ServerLevel) e.level();
        Vec3 vec = e.getViewVector(1.0f);
        for (int t = 0;t < 20;t++) {
            int deltaX = level.random.nextInt(-2500 + (int) (vec.x() * 500), 2500 + (int) (vec.x() * 500));
            int deltaZ = level.random.nextInt(-2500 + (int) (vec.z() * 500), 2500 + (int) (vec.z() * 500));
            int newX = deltaX + (int) e.getX();
            int newZ = deltaZ + (int) e.getZ();
            for (int i = level.getHeight() - 1; i > level.getMinBuildHeight() - 1; i--) {
                if (
                        level.getBlockState(new BlockPos(newX, i, newZ)).getBlock().defaultBlockState().isAir() &&
                        level.getBlockState(new BlockPos(newX, i - 1, newZ)).isAir() &&
                        !level.getBlockState(new BlockPos(newX, i - 2, newZ)).isAir()
                ) {
                    e.teleportTo(newX, i, newZ);
                    return Python_NoneType.None;
                }
            }
        }
        return Python_NoneType.None; // Means it failed to find a suitable spot
    }

    @PythonMethodLink(
            docs = """
            # @brief give a random item to the player
            # @param playerEntity = The player entity to which to give the item""",
            desc = "Give a random item to the player",
            order = MethodLinkOrder.GIVE_RANDOM_ITEM
    )
    public static Python_Object give_random_item(ShadowedPythonCode caller, Python_Entity playerEntity) {
        Entity e = getEntity(playerEntity.entityUUID);
        if (!(e instanceof Player)) return Python_NoneType.None;
        BlockPos pos = e.blockPosition();
        RandomSource rand = e.level().random;
        pos = pos.offset(20000 * (rand.nextBoolean() ? -1 : 1) + rand.nextInt(10),rand.nextInt(10),20000 * (rand.nextBoolean() ? -1 : 1) + rand.nextInt(10));
        ChunkAccess chunkAccess = e.level().getChunk(
                pos.getX() >> 4,
                pos.getZ() >> 4,
                ChunkStatus.FULL,
                true
        );
        if (chunkAccess == null) return Python_NoneType.None;
        System.out.println(chunkAccess.getBlockState(pos).getBlock());
        ItemStack item = new ItemStack(chunkAccess.getBlockState(pos).getBlock());
        ((Player)e).addItem(item);
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Sets an entity on fire
            # @param entity = The entity to set on fire""",
            desc = "Set entities on fire",
            order = MethodLinkOrder.SET_PLAYER_ON_FIRE
    )
    public static Python_Object set_entity_on_fire(ShadowedPythonCode caller, Python_Entity entity) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        e.setRemainingFireTicks(20*5);
        return Python_NoneType.None;
    }

    private static boolean tryPlaceFire(Level level, BlockPos herePos) {
        BlockState here = level.getBlockState(herePos);
        BlockState below = level.getBlockState(herePos.offset(0,-1,0));
        if (here.isAir() && !below.isAir() && BaseFireBlock.canBePlacedAt(level,herePos, Direction.UP)) {
            level.setBlock(herePos,BaseFireBlock.getState(level,herePos),11);
            return true;
        }
        return false;
    }

    @PythonMethodLink(
            docs = """
            # @brief Sets the blocks around en entity on fire
            # @param entity = The entity around which to set blocks on fire
            # @param radius = Radius around entity for which blocks will be set on fire
            """,
            desc = "Set blocks around the player on fire",
            order = MethodLinkOrder.SET_BLOCKS_ON_FIRE
    )
    public static Python_Object set_blocks_on_fire(ShadowedPythonCode caller, Python_Entity entity, Python_Number radius) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        BlockPos pos = e.blockPosition();
        Level level = e.level();
        int r = radius.anyAsInt();
        for (int x = -r;x <= r;x++) {
            for (int z = -r;z <= r;z++) {
                boolean set = false;
                for (int y = 0;y < r;y++) {
                    if (tryPlaceFire(level,pos.offset(x,y,z))) {
                        set = true;
                        break;
                    }
                }
                if (set) continue;
                for (int y = 0;y > -r;y--) {
                    if (tryPlaceFire(level,pos.offset(x,y,z))) break;
                }
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief It gives a previously registered item to the player
            # @param entityPlayer = The entity player to which to give the item
            # @param item_id = String identification id of the registered item, use register_item(...)
            # @param quantity = Amount of the item to give""",
            desc = "Give custom items to the player",
            order = MethodLinkOrder.GIVE_ITEM_PLAYER
    )
    public static Python_Object give_item_player(
            ShadowedPythonCode caller,
            Python_Entity entityPlayer,
            Python_String item_id,
            Python_Number quantity
    ) {
        Entity e = getEntity(entityPlayer.entityUUID);
        if (e == null) return Python_NoneType.None;
        if (!(e instanceof ServerPlayer)) return Python_NoneType.None;
        ItemStack stack = ProgrammableItemProvider.getItem(item_id.s, quantity.anyAsInt());
        ((ServerPlayer)e).addItem(stack);
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Registers an item
            # @param item_id = String id to identify the item definition
            # @param name = Name of the item
            # @param description = Description of the item
            # @param minecraft_item = The name of the minecraft items that should be displayed as the item icon, the string can be in free natural form, for potions use minecraft:potion
            # @param on_use_impl = Function which implements the effects of using the item (right click), takes one input parameter of the entity which is using the item
            # @param on_tick_impl = Function that runs every tick, every 0.05 seconds, cannot be a string, takes one input parameter of the entity which is using the item""",
            desc = "Create custom items",
            order = MethodLinkOrder.CREATE_ITEM
    )
    public static Python_Object register_item(
            ShadowedPythonCode caller,
            Python_String item_id,
            Python_String name,
            Python_String description,
            Python_String minecraft_item,
            Python_Object on_use_impl,
            Python_Object on_tick_impl
    ) {
        String onTick = null;
        String onUse = null;
        if (on_tick_impl instanceof Python_Function) onTick = ((Python_Function) on_tick_impl).name;
        if (on_use_impl instanceof Python_Function) onUse = ((Python_Function) on_use_impl).name;
        ProgrammableItemProvider.createItem(item_id.s,name.s,description.s,minecraft_item.s,caller.originalCode,onUse,onTick);

        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Registers a magic projectile with a certain implementation
            # @param projectile_id = String id to indentify the projectile definition
            # @param name = Display name of the projectile
            # @param lifetime = Number of seconds for which the projectile is alive
            # @param color = Color of the projectile in RGB format (e.g. 0xff0000 - red)
            # @param on_hit_entity = Function which implements what happens when the projectile hits an entity, takes one parameter which is the entity that was hit by the projectile
            # @param on_block_hit = Function which implements what happens when the projectile hits a block, takes one parameter which is the projectile entity itself
            # @param on_tick = Function which runs every tick, must return a list of projectile modifiers, takes one parameter which is the projectile entity itself
            # @param lifetime = Number of seconds the projectile stays in the air before expiring""",
            desc = "Create custom magical projectiles",
            order = MethodLinkOrder.CREATE_PROJECTILE
    )
    public static Python_Object register_magic_projectile(
            ShadowedPythonCode caller,
            Python_String projectile_id,
            Python_String name,
            Python_String description,
            Python_Number color,
            Python_Number lifetime,
            Python_Object on_hit_entity,
            Python_Object on_block_hit,
            Python_Object on_tick
    ) {
        String onEntityName = null;
        String onBlockName = null;
        String onTickName = null;
        if (on_hit_entity instanceof Python_Function) onEntityName = ((Python_Function) on_hit_entity).name;
        if (on_block_hit instanceof Python_Function) onBlockName = ((Python_Function) on_block_hit).name;
        if (on_tick instanceof Python_Function) onTickName = ((Python_Function) on_tick).name;
        int rgb = color.i;
        float r = ((rgb & 0xff0000) >> 16)/255.0f;
        float g = ((rgb & 0x00ff00) >> 8)/255.0f;
        float b = (rgb & 0x0000ff)/255.0f;
        ProgrammableProjectileProvider.createProjectile(projectile_id.s, name.s, description.s, caller.originalCode,onEntityName,onBlockName,onTickName,(int)(lifetime.anyAsFloat() * 20),r,g,b);
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief spawns a projectile
            # @param entity = The entity which will shoot the projectile
            # @param projectile_id = Id of the registered projectile to spawn, use register_magic_projectile(...) first
            # @param yaw = Relative yaw offset from where the shooter is looking (in degrees)
            # @param pitch = Relative pitch offset from where the shooter is looking (in degrees)
            # @param speed = Speed of the projectile in blocks/second
            """,
            desc = "Spawn projectile",
            order = MethodLinkOrder.SPAWN_PROJECTILE
    )
    public static Python_Object spawn_projectile(
            ShadowedPythonCode caller,
            Python_Entity entity,
            Python_String projectile_id,
            Python_Number yaw,
            Python_Number pitch,
            Python_Number speed
    ) {
        Entity e = getEntity(entity.entityUUID);
        if (e instanceof LivingEntity) {
            ProgrammableProjectile programmableProjectile = ProgrammableProjectileProvider.getPulse(projectile_id.s,e.level(),(LivingEntity) e);
            if (programmableProjectile == null) return Python_NoneType.None;
            double sYaw = Math.sin(Math.toDegrees(yaw.anyAsFloat()));
            double cYaw = Math.cos(Math.toDegrees(yaw.anyAsFloat()));
            double sPitch = Math.sin(Math.toDegrees(pitch.anyAsFloat()));
            double cPitch = Math.cos(Math.toDegrees(pitch.anyAsFloat()));
            Vec3 dir = programmableProjectile.getDeltaMovement();
            double speedTick = speed.anyAsFloat()/20;
            Vec3 newDir = new Vec3(
                    dir.x*cYaw*cPitch-dir.y*sYaw+dir.z*cYaw*sPitch,
                    dir.x*sPitch*cPitch+dir.y*cYaw+dir.z*sYaw*sPitch,
                    -dir.x*sPitch+dir.z*cPitch
            ).multiply(speedTick,speedTick,speedTick);
            programmableProjectile.setDeltaMovement(newDir);

            e.level().addFreshEntity(programmableProjectile);
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @param amount_per_second = Absolute amount of satiation change. Positives satiate, negatives hunger. A player has 20 hunger points.
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give saturation the player",
            order = null
    )
    public static Python_Object modifier_projectile_homing() {

    }

    @PythonMethodLink(
            docs = """
            # @param effect_name = Name of the registered effect to give to the player, use register_effect(...) first
            # @param entity = The entity to which to give the effect
            # @param duration = Duration in seconds (floating point number)""",
            desc = "Give entities custom effects",
            order = MethodLinkOrder.GIVE_EFFECT_PLAYER
    )
    public static Python_Object give_effect_entity(
            ShadowedPythonCode caller,
            Python_Entity entity,
            Python_String effect_id,
            Python_Number duration
    ) {
        Entity e = getEntity(entity.entityUUID);
        if (e instanceof LivingEntity) {
            MobEffect effect = Somnium.INSTANCE.effectProvider.getEffect(effect_id.s);
            if (effect != null) {
                ((LivingEntity)e).addEffect(new MobEffectInstance(effect, (int) (duration.anyAsFloat() * 20)) {
                    @Override
                    public boolean tick(LivingEntity p_19553_, Runnable p_19554_) {
                        boolean ret = super.tick(p_19553_, p_19554_);
                        p_19554_.run();
                        return ret;
                    }
                });
            }
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief Create an effect with a certain implementation
            # @param effect_id = String id to identify the effect definition
            # @param name = Display name that the player sees attached to the effect
            # @param effect_impl_tick = Function effect implementation, runs every tick, must return a list of modifiers, takes in a parameter which is the entity which has the effect""",
            desc = "Create custom effects",
            order = MethodLinkOrder.CREATE_EFFECT
    )
    public static Python_Object register_effect(ShadowedPythonCode caller, Python_String effect_id, Python_String name, Python_Object effect_impl_tick) {
        if (effect_impl_tick instanceof Python_Function) {
            Somnium.INSTANCE.effectProvider.createEffect(effect_id.s,name.s,caller.originalCode,((Python_Function) effect_impl_tick).name);
        }
        return Python_NoneType.None;
    }

    /*@PythonMethodLink(
            docs = """
            # @param amount_per_second = Absolute amount of health change. Positives heal, negatives damage. A player has 20 health.
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects damage or heal the player",
            order = MethodLinkOrder.MODIFIER_CREATOR_PLAYER_HEALTH
    )
    public static Python_Object modifier_effect_entity_health(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.HEALTH,percent.anyAsFloat()/100.0f);
    }*/

    @PythonMethodLink(
            docs = """
            # @param amount_per_second = Absolute amount of satiation change. Positives satiate, negatives hunger. A player has 20 hunger points.
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give saturation the player",
            order = MethodLinkOrder.MODIFIER_CREATOR_PLAYER_SATURATION
    )
    public static Python_Object modifier_creator_player_saturation(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.SATURATION,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(
            docs = """
            # @param percent = Percentage by which player speed is modified. >0 increases the speed <0 lowers it.
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give the player speed",
            order = MethodLinkOrder.MODIFIER_CREATOR_PLAYER_SPEED
    )
    public static Python_Object modifier_creator_player_speed(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.SPEED,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(
            docs = """
            # @param percent = Percentage by which the player is protected from damage. >0 increases protection <0 makes the player more vulnerable
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give the player damage resistance",
            order = MethodLinkOrder.MODIFIER_CREATOR_RESISTANCE
    )
    public static Python_Object modifier_creator_resistance(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.RESISTANCE,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(
            docs = """
            # @param factor = Percentage by which the player is protected from fire damage. >0 increases protection <0 makes the player more vulnerable
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give the player fire resistance",
            order = MethodLinkOrder.MODIFIER_CREATOR_FIRE_RESISTANCE
    )
    public static Python_Object modifier_creator_fire_resistance(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.FIRE_RESISTANCE,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(
            docs = """
            # @param factor = Percentage by which the player jump height is changed. >0 increases jump <0 decreases jump
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give the player jump boost",
            order = MethodLinkOrder.MODIFIER_CREATOR_PLAYER_JUMP
    )
    public static Python_Object modifier_creator_player_jump(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.JUMP,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(
            docs = """
            # @param factor = Percentage by which the player mine speed is changed. >0 increases mine <0 decreases mine
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that give the player mining speed",
            order = MethodLinkOrder.MODIFIER_CREATOR_PLAYER_MINING_SPEED
    )
    public static Python_Object modifier_creator_player_mining_speed(ShadowedPythonCode caller, Python_Number percent) {
        return new Python_EffectModifier(PotionModifiersType.MINING_SPEED,percent.anyAsFloat()/100.0f);
    }

    @PythonMethodLink(
            docs = """
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that blind the player",
            order = MethodLinkOrder.MODIFIER_CREATOR_BLIND_PLAYER
    )
    public static Python_Object modifier_creator_blind_player(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.BLIND,0);
    }

    @PythonMethodLink(
            docs = """
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that make the player invisible",
            order = MethodLinkOrder.MODIFIER_CREATOR_INVISIBLE_PLAYER
    )
    public static Python_Object modifier_creator_invisible_player(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.INVISIBLE,0);
    }

    @PythonMethodLink(
            docs = """
            # @brief Allows the player to pass through solid blocks
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that let the player pass through blocks",
            order = MethodLinkOrder.MODIFIER_CREATOR_BLOCK_PHASING
    )
    public static Python_Object modifier_creator_block_phasing(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.PHASING,0);
    }

    @PythonMethodLink(
            docs = """
            # @brief Disrupts redstone circuitry around player
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that disrupt surrounding redstone",
            order = MethodLinkOrder.MODIFIER_CREATOR_DISRUPT_REDSTONE
    )
    public static Python_Object modifier_creator_disrupt_redstone(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.DISRUPT_REDSTONE,0);
    }

    @PythonMethodLink(
            docs = """
            # @brief Allows the player to see valuables
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that allows the player to see valuables",
            order = MethodLinkOrder.MODIFIER_CREATOR_SHOW_VALUABLES
    )
    public static Python_Object modifier_creator_show_valuables(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.SHOW_VALUABLES,0);
    }

    @PythonMethodLink(
            docs = """
            # @brief Allows the player to see dangers
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that allows the player to see dangers",
            order = MethodLinkOrder.MODIFIER_CREATOR_REVEAL_DANGERS
    )
    public static Python_Object modifier_creator_reveal_dangers(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.REVEAL_DANGERS,0);
    }

    @PythonMethodLink(
            docs = """
            # @brief Allows the player to see through blocks
            # @return Returns a modifier object that can be applied to an effect""",
            desc = "Create effects that allows the player to see through blocks",
            order = MethodLinkOrder.MODIFIER_CREATOR_SEE_THROUGH_BLOCKS
    )
    public static Python_Object modifier_creator_see_through_blocks(ShadowedPythonCode caller) {
        return new Python_EffectModifier(PotionModifiersType.SEE_THROUGH_BLOCKS,0);
    }

    @PythonMethodLink(
            docs = """
            # @brief Saves an integer value
            # @parameter string_key = String key for the key-int pair
            # @parameter value = The value to be stored""",
            desc = "NONE",
            order = MethodLinkOrder.SAVE_LOCAL_DATA_NUMBER
    )
    public static Python_Object save_local_data_number(ShadowedPythonCode caller, Python_String string_key,Python_Number value) {
        if (GameLinkingHandler.currentCacheKey == null) throw new RuntimeException("Cannot save local data by a script with no cache key! Please report.");
        Somnium.INSTANCE.worldData.localStoreInt(GameLinkingHandler.currentCacheKey, string_key.s, value.anyAsInt());
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @parameter string_key = String key for the key-int pair
            # @parameter def = The value that is returned if the key is not found
            # @return Returns the value store for the key, 0 if no key is found""",
            desc = "NONE",
            order = MethodLinkOrder.GET_LOCAL_DATA_NUMBER
    )
    public static Python_Object get_local_data_number(ShadowedPythonCode caller, Python_String string_key, Python_Number def) {
        if (GameLinkingHandler.currentCacheKey == null) throw new RuntimeException("Cannot get local data by a script with no cache key! Please report.");
        return new Python_Number(Somnium.INSTANCE.worldData.localGetInt(GameLinkingHandler.currentCacheKey, string_key.s, def.anyAsInt()));
    }

    @PythonMethodLink(
            docs = """
            # @brief Spawns particles around the player
            # @param entity = The entity around which to spawn the projectiles
            # @param count = Number of particles to spawn around the player
            # @param color = Color in RGB format (e.g. 0xff0000 - red)
            # @param lifetime = Lifetime in seconds""",
            desc = "Spawn particles",
            order = MethodLinkOrder.SPAWN_PARTICLE
    )
    public static Python_Object spawn_particle(ShadowedPythonCode caller,Python_Entity entity, Python_Number count, Python_Number color, Python_Number lifetime) {
        Entity e = getEntity(entity.entityUUID);
        if (e == null) return Python_NoneType.None;
        int rgb = color.i;
        float r = ((rgb & 0xff0000) >> 16)/255.0f;
        float g = ((rgb & 0x00ff00) >> 8)/255.0f;
        float b = (rgb & 0x0000ff)/255.0f;
        DustParticleOptions options = new DustParticleOptions(new Vector3f(r,g,b),1);
        for (int i = 0;i < count.anyAsInt();i++) {
            int x = (int)e.getX() + (int)(e.level().random.nextFloat() * 10 - 5);
            int y = (int)e.getY() + (int)(e.level().random.nextFloat() * 10 - 5);
            int z = (int)e.getZ() + (int)(e.level().random.nextFloat() * 10 - 5);
            ((ServerLevel)e.level()).sendParticles(options,x,y,z,0,0,0,0,0);
        }
        return Python_NoneType.None;
    }

    @PythonMethodLink(
            docs = """
            # @brief displays an on-screen message to the player usually reserved for cryptic message after each ritual
            # @param playerEntity = The player entity to which to display the message
            # @param message = String containing the message""",
            desc = "NONE",
            order = MethodLinkOrder.ON_SCREEN_MESSAGE
    )
    public static Python_Object on_screen_message(ShadowedPythonCode caller, Python_Entity playerEntity, Python_String message) {
        Entity e = getEntity(playerEntity.entityUUID);
        if (e == null) return Python_NoneType.None;
        if (e instanceof ServerPlayer) {
            PacketRegistry.toPlayer(new S2CShowMessage(message.s), (ServerPlayer) e);
        }
        return Python_NoneType.None;
    }



}
