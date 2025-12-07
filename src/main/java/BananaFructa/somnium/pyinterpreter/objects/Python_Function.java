package BananaFructa.somnium.pyinterpreter.objects;

public class Python_Function extends Python_Object {
    public String name;

    public Python_Function(String name) {
        this.name = name;
    }

    @Override
    public Python_Object copy() {
        return new Python_Function(name);
    }
}
