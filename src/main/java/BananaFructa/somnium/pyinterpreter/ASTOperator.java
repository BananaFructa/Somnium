package BananaFructa.somnium.pyinterpreter;

public class ASTOperator extends ASTNode{


    public int precedence = -1;
    public boolean unary = false;

    public ASTOperator(String name, int precedence) {
        super(name);
        this.precedence = precedence;
    }

    public boolean full() {
        return (!unary && children.size() == 2) || (unary && children.size() == 1);
    }
}
