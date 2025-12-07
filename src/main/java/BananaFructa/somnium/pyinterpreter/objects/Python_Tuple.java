package BananaFructa.somnium.pyinterpreter.objects;

import java.util.ArrayList;

public class Python_Tuple extends Python_Object {

    public java.util.List<Python_Object> elements = new ArrayList<>();

    @Override
    public String toString() {
        String s = "(";
        for (int i = 0;i < elements.size();i++) {
            s += elements.get(i).toString();
            if (i != elements.size() - 1) {
                s+=",";
            }
        }
        s += ")";
        return s;
    }

    @Override
    public Python_Object copy() {
        Python_Tuple second = new Python_Tuple();
        second.elements.addAll(elements);
        return second;
    }
}
