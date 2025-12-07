package BananaFructa.somnium.pyinterpreter;

public class ASTWhile extends ASTNode{

    public ASTNode statement;
    public String scope;

    public ASTWhile(String name) {
        super(name);
    }
}
