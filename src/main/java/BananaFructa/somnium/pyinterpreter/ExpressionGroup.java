package BananaFructa.somnium.pyinterpreter;

import java.util.List;

public abstract class ExpressionGroup {

    String name;
    List<ASTNode> expressions;

    public ExpressionGroup(String name) {
        this.name = name;
    }

}
