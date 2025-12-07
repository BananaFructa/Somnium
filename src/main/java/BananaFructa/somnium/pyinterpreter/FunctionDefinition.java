package BananaFructa.somnium.pyinterpreter;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefinition extends ExpressionGroup {

    String name;
    List<String> parameters;

    public FunctionDefinition(String name, List<String> parameters) {
        super(name);
        this.parameters = parameters;
        this.expressions = new ArrayList<>();
    }

}
