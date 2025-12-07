package BananaFructa.somnium.pyinterpreter;

import java.util.ArrayList;
import java.util.List;

public class ASTLambda extends ASTNode{

    String internalFunctionName;

    public ASTLambda(String name) {
        super(name);
    }
}
