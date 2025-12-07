package BananaFructa.somnium.pyinterpreter.objects;

public class Python_Bool extends Python_Object {

    public boolean b = false;

    public Python_Bool(boolean b) {
        this.b = b;
    }

    public Python_Bool(Object o) {
        if (o instanceof Python_Number) {
            b = !((Python_Number)o).equals(Python_Number.ZERO);
        } else throw new RuntimeException("LID Python: Cannot cast to boolean.");
    }

    @Override
    public String toString() {
        if (b) return "True";
        else return "False";
    }

    @Override
    public Python_Object copy() {
        return new Python_Bool(b);
    }
}
