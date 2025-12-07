package BananaFructa.somnium.pyinterpreter;

import java.util.ArrayList;
import java.util.List;

public class ASTList extends ASTNode{

    public List<ASTNode> elements = new ArrayList<>();

    public ASTList(String name) {
        super(name);
    }

    @Override
    public void print(int spacing) {
        for (int i = 0;i < spacing;i++) System.out.print(" ");
        System.out.println("[");
        for (ASTNode e : elements) e.print(spacing);
        System.out.println("]");
        for (ASTNode c : children) c.print(spacing + 1);
    }
}
