package BananaFructa.somnium.pyinterpreter;

import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ASTSwitch extends ASTNode{

    ASTNode conditionExpression;
    String trueScope;
    List<Tuple<ASTNode,String>> elifs = new ArrayList<>();
    String falseScope = null;

    public ASTSwitch(String name,ASTNode conditionExpression,String trueScope) {
        super(name);
        this.conditionExpression = conditionExpression;
        this.trueScope = trueScope;
    }
}
