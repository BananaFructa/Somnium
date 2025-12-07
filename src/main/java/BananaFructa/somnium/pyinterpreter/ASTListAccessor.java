package BananaFructa.somnium.pyinterpreter;

public class ASTListAccessor extends ASTOperator {

    public ASTNode first;
    public ASTNode second;
    public boolean range = false;

    public ASTListAccessor(String name) {
        super(name,999);
        this.unary = true;
    }
}
