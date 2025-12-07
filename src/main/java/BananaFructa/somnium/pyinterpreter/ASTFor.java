package BananaFructa.somnium.pyinterpreter;

public class ASTFor extends ASTNode{

    public String var;
    public ASTNode list;
    public String scope;

    public ASTFor(String name) {
        super(name);
    }
}
