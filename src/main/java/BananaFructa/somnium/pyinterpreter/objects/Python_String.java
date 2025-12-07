package BananaFructa.somnium.pyinterpreter.objects;

public class Python_String extends Python_Object {
    public java.lang.String s;

    public Python_String(java.lang.String name) {
        this.s = name;
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public Python_Object copy() {
        return new Python_String(s);
    }
}
