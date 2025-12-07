package BananaFructa.somnium.pyinterpreter;

import java.util.ArrayList;
import java.util.List;

public abstract class ASTNode {

    public List<ASTNode> children = new ArrayList<ASTNode>();
    public ASTNode parent;

    public String name;

    public ASTNode(String name) {
        this.name = name;
    }

    public void print(int spacing) {
        for (int i = 0;i < spacing;i++) System.out.print(" ");
        System.out.println(name);
        for (ASTNode c : children) c.print(spacing + 1);
    }

}
