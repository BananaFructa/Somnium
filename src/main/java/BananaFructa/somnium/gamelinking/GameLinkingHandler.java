package BananaFructa.somnium.gamelinking;

import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.gamelinking.objects.Python_Entity;
import BananaFructa.somnium.pyinterpreter.Function2;
import BananaFructa.somnium.pyinterpreter.JavaPythonShadower;
import BananaFructa.somnium.pyinterpreter.ShadowedPythonCode;
import BananaFructa.somnium.pyinterpreter.objects.Python_NoneType;
import BananaFructa.somnium.pyinterpreter.objects.Python_Object;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class GameLinkingHandler {

    public static String currentCacheKey;

    private static List<LinkedFunctionInfo> linkedFunctions = new ArrayList<>();
    private static HashMap<String, List<String>> symbolicLinks = new HashMap<>();
    private static HashMap<String, ShadowedPythonCode> cachedInterpreted = new HashMap<>();

    public static String globalStorageKey = "global";

    public static void registerGameLinkerDefiner(Class<?> clazz, Object instance) {
        List<Tuple<Method,PythonMethodLink>> methods = getLinkedMethods(clazz);
        for (Tuple<Method,PythonMethodLink> annMethod : methods) {
            Method m = annMethod.getA();
            String docs = annMethod.getB().docs();
            String desc = annMethod.getB().desc();
            int order = annMethod.getB().order().ordinal();
            if (m.getParameterCount() == 0) throw new RuntimeException("Python linked function needs to have a caller as the first parameter.");
            Parameter[] parameters = m.getParameters();
            if (!ShadowedPythonCode.class.isAssignableFrom(parameters[0].getType())) throw new RuntimeException("Python linked function needs to have a caller of type ShadowedPythonCode.class.");
            if (!Python_Object.class.isAssignableFrom(m.getReturnType())) throw new RuntimeException("Python linked function needs to have a return of type PType.");
            Class<?>[] parameterTypes = new Class[parameters.length-1];
            for (int i = 1;i < parameters.length;i++) {
                parameterTypes[i-1] = parameters[i].getType();
            }
            Function2<ShadowedPythonCode, Python_Object[], Python_Object> func = (caller, inputs) -> {
                try {
                    assertType(inputs,parameterTypes);
                    return (Python_Object) m.invoke(instance, ArrayUtils.addAll(new Object[]{caller},inputs));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            List<String> parameterNames = new ArrayList<>();
            for (int i = 1;i < parameters.length;i++) {
                parameterNames.add(parameters[i].getName());
            }
            linkedFunctions.add(new LinkedFunctionInfo(order,m.getName(),parameterNames,func,docs,desc));
            symbolicLinks.put(m.getName(),parameterNames);
        }
    }

    private static void assertType(Python_Object[] objects, Class<?>... types) {
        if (objects.length != types.length) throw new RuntimeException("Somium: Non-default python linked function called with the wrong number of parameters, " + objects.length + " instead of " + types.length + ".");
        for (int i = 0;i < objects.length;i++) {
            if (!types[i].isInstance(objects[i])) throw new RuntimeException("Somium: Non-default python linked function called with wrong type on parameter, " + objects[i].getClass().toString() + " is not of type " + types[i].toString() + ".");
        }
    }

    public static long getNextCustomItemId() {
        return Somnium.INSTANCE.worldData.getNextItemId();
    }

    /**
     * @brief Executes python code
     * @param cacheKey = Unique key to cache the interpreted code for repetitive executions. Can be null for no caching.
     * @param functionName = The name of the python function to be called from the code. If null the python code will be executed normally.
     * @param code = The python code to be executed
     */
    public static Python_Object runPythonCode(String storageKey, String cacheKey, String code, String functionName, Python_Object... parameters) {
        Python_Object ret = Python_NoneType.None;
        ShadowedPythonCode interpretedCode;
        boolean hit = false;
        currentCacheKey = storageKey;
        if (cacheKey != null && cachedInterpreted.containsKey(cacheKey)) {
            interpretedCode = cachedInterpreted.get(cacheKey);
            hit = true;
        } else {
            try {
                interpretedCode = JavaPythonShadower.interpret(code, symbolicLinks);
            } catch (Exception err) {
                // TODO: notify player when this happens ?
                Somnium.LOGGER.error("Error during internal python code interpretation. More details below. Please report!");
                err.printStackTrace();
                return ret;
            }
        }
        try {
            if (!hit) {
                for (LinkedFunctionInfo functionInfo : linkedFunctions) {
                    interpretedCode.linkFunction(functionInfo.name, functionInfo.function);
                }
            }
            if (functionName != null) ret = interpretedCode.executeFunction(functionName,parameters);
            else interpretedCode.execute();
            if (cacheKey != null && !hit) cachedInterpreted.put(cacheKey,interpretedCode);
        } catch (Exception err) {
            // TODO: notify player when this happens ?
            Somnium.LOGGER.error("Error during internal python code execution. More details below. Please report!");
            err.printStackTrace();
            return ret;
        }
        currentCacheKey = null;
        return ret;
    }

    public static void clearCache(String cacheKey) {
        cachedInterpreted.remove(cacheKey);
    }

    public static boolean isSomniumItem(ItemStack stack) {
        if (stack.hasTag()) {
           return stack.getTag().contains("somnium_item");
        }
        return false;
    }

    public static boolean triggerItemImpl(ItemStack stack, LivingEntity player, String implKey) {
        if (isSomniumItem(stack)) {
            CompoundTag tag = (CompoundTag) stack.getTag().get("somnium_item");
            if (tag.contains(implKey)) {
                int id = tag.getInt("id");
                String code = tag.getString("code");
                String onUse = tag.getString(implKey);
                GameLinkingHandler.runPythonCode("item_" + id, "item_" + id, code, onUse,new Python_Entity(player.getUUID()));
            }
            return true;

        }
        return false;
    }

    private static List<Tuple<Method,PythonMethodLink>> getLinkedMethods(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        List<Tuple<Method,PythonMethodLink>> annotMethods = new ArrayList<>();
        for (Method m : methods) {
            PythonMethodLink pyAnnotation = m.getAnnotation(PythonMethodLink.class);
            if (pyAnnotation != null) {
                annotMethods.add(new Tuple<>(m,pyAnnotation));
            }
        }
        return annotMethods;
    }

    public static String getAPIPromptDescription() {
        List<LinkedFunctionInfo> sorted = linkedFunctions.stream().sorted(Comparator.comparingInt(a -> a.order)).collect(Collectors.toCollection(ArrayList::new));
        String suffix = "";
        for (LinkedFunctionInfo info : sorted) {
            suffix += info.docs + "\n";
            String function = info.name + "(";
            for (int i = 0;i < info.params.size();i++) {
                function += info.params.get(i);
                if (i != info.params.size() - 1) function += ",";
            }
            function += ")";
            suffix += function + "\n\n";
        }
        return suffix;
    }

    public static String getInteractionList() {
        List<LinkedFunctionInfo> sorted = linkedFunctions.stream().sorted(Comparator.comparingInt(a -> a.order)).collect(Collectors.toCollection(ArrayList::new));
        String list = "";
        for (LinkedFunctionInfo info : sorted) {
            if (info.desc.equals("NONE") || info.desc.equals("TODO")) continue;
            list += info.desc + "\n";
        }
        return list;
    }

}
