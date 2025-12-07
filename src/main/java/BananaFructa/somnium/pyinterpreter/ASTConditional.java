package BananaFructa.somnium.pyinterpreter;

public class ASTConditional extends ASTNode{

    public ASTNode condition;
    public ASTNode trueValue;
    public ASTNode falseValue;

    public ASTConditional(String name) {
        super(name);
    }
}
