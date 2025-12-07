package BananaFructa.somnium.pyinterpreter;

public class ScopeInfo {
    String functionScopeName;
    int minSpaces;
    ASTNode lastAddedNode = null;

    public ScopeInfo(String functionScopeName, int minSpaces) {
        this.functionScopeName = functionScopeName;
        this.minSpaces = minSpaces;
    }

}
