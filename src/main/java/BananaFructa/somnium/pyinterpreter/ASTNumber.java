package BananaFructa.somnium.pyinterpreter;

import BananaFructa.somnium.pyinterpreter.objects.Python_Number;

public class ASTNumber extends ASTNode{

    Python_Number value;

    public ASTNumber(String name) {
        super(name);
        if (name.contains(".")) {
            value = new Python_Number(Float.parseFloat(name));
        } else if(name.startsWith("0x")) {
            value = new Python_Number(Integer.decode(name));
        } else {
            value = new Python_Number(Integer.parseInt(name));
        }
    }
}
