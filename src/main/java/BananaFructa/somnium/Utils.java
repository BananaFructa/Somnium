package BananaFructa.somnium;

import BananaFructa.somnium.service.OllamaServiceHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> getMissingModels(OllamaServiceHandler serviceHandler, String... required) {
        List<String> left = new ArrayList<>();
        try {
            List<String> models = serviceHandler.getAvailableModels();
            for (int i = 0; i < required.length; i++) {
                final String req = required[i];
                if (!models.stream().anyMatch(m -> m.equals(req)) && !left.stream().anyMatch(m -> m.equals(req))) {
                    left.add(required[i]);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return left;
    }

    public static <T> T readDeclaredField(Class<?> targetType, Object target, String name) {
        try {
            Field f = targetType.getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public static void writeDeclaredField(Class<?> targetType, Object target, String name, Object value,boolean final_) {
        try {
            Field f = targetType.getDeclaredField(name);
            f.setAccessible(true);
            if (final_) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            }
            f.set(target,value);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static void writeDeclaredDouble(Class<?> targetType, Object target, String name, double value,boolean final_) {
        try {
            Field f = targetType.getDeclaredField(name);
            f.setAccessible(true);
            if (final_) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            }
            f.setDouble(target,value);

        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static Method getDeclaredMethod(Class<?> targetClass, String name, Class<?>... parameters) {
        try {
            Method m = targetClass.getDeclaredMethod(name, parameters);
            m.setAccessible(true);
            return m;
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
