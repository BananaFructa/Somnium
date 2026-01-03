package BananaFructa.somnium.gamelinking;

import BananaFructa.somnium.pyinterpreter.Function2;
import BananaFructa.somnium.pyinterpreter.ShadowedPythonCode;
import BananaFructa.somnium.pyinterpreter.objects.Python_Object;

import java.util.List;

public class LinkedFunctionInfo {

    public int order;
    public String name;
    public List<String> params;
    public Function2<ShadowedPythonCode, Python_Object[], Python_Object> function;
    public String docs;
    public String desc;

    public LinkedFunctionInfo(int order, String name, List<String> params, Function2<ShadowedPythonCode, Python_Object[], Python_Object> func, String docs, String desc) {
        this.name = name;
        this.function = func;
        this.docs = docs;
        this.order = order;
        this.params = params;
        this.desc = desc;
    }

}
