package BananaFructa.somnium.pyinterpreter.objects;

public class Python_NoneType extends Python_Object {

    public static Python_NoneType None = new Python_NoneType();

    @Override
    public String toString() {
        return "None";
    }

    @Override
    public Python_Object copy() {
        return None;
    }
}
