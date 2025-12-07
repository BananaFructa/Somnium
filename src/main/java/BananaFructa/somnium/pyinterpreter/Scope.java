package BananaFructa.somnium.pyinterpreter;

import BananaFructa.somnium.pyinterpreter.objects.Python_Object;

import java.util.HashMap;

public class Scope {

    boolean returnFlag = false;
    boolean isFunctionScope = false;
    boolean isLoopScope = false;
    HashMap<String, Python_Object> variables = new HashMap<>();

    public Scope(boolean isFunctionScope, boolean isLoopScope) {
        this.isFunctionScope = isFunctionScope;
        this.isLoopScope = isLoopScope;
    }

    public boolean hasVar(String name) {
        return variables.containsKey(name);
    }

    public Python_Object getVar(String name) {
        return variables.get(name);
    }

    public void registerVar(String name, Python_Object val) {
        variables.put(name,val);
    }

}
