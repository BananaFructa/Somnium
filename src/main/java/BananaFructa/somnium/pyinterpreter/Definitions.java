package BananaFructa.somnium.pyinterpreter;

import java.util.HashMap;

public class Definitions {
    private HashMap<String, ExpressionGroup> definitions = new HashMap<>();
    public int scopeCounter = 0;

    public ExpressionGroup get(String key) {
        return definitions.get(key);
    }

    public void put(String key, ExpressionGroup value) {
        definitions.put(key,value);
    }

    public boolean containsKey(String key) {
        return definitions.containsKey(key);
    }
}
