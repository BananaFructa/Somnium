package BananaFructa.somnium.pyinterpreter;

import java.util.ArrayList;

public class ScopeDefinition extends ExpressionGroup {

    String name;

    public ScopeDefinition(String name) {
        super(name);
        this.expressions = new ArrayList<>();
    }

}
