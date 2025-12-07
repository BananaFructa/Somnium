package BananaFructa.somnium.pyinterpreter.objects;

public abstract class Python_Object {

    public static String getTypeName(Python_Object type) {
        if (type instanceof Python_Bool) return "Bool";
        if (type instanceof Python_Function) return "Function";
        if (type instanceof Python_List) return "List";
        if (type instanceof Python_NoneType) return "None";
        if (type instanceof Python_Number) return "Number";
        return "InvalidType";
    }

    public abstract Python_Object copy();

}
