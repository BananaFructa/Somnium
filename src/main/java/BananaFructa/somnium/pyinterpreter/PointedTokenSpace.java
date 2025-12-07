package BananaFructa.somnium.pyinterpreter;

import java.util.ArrayList;
import java.util.List;

public class PointedTokenSpace {
    List<List<String>> data;
    int index;
    int line;

    public PointedTokenSpace() {

    }

    public static PointedTokenSpace fromMultiLine(List<List<String>> data) {
        PointedTokenSpace tokenSpace = new PointedTokenSpace();
        tokenSpace.data = data;
        tokenSpace.index = 0;
        tokenSpace.line = 0;
        return tokenSpace;
    }

    public static PointedTokenSpace fromSingleLine(List<String> data) {
        PointedTokenSpace tokenSpace = new PointedTokenSpace();
        tokenSpace.data = new ArrayList<>();
        tokenSpace.data.add(data);
        tokenSpace.index = 0;
        tokenSpace.line = 0;
        return tokenSpace;
    }

    // I fucked up the token to token advancement is check first them move
    // But for lines its move then check

    boolean hasNext() {
        if (!hasLine()) return false;
        return index != data.get(line).size();
    }

    String next() {
        return data.get(line).get(index++);
    }

    String peek() {
        return data.get(line).get(index);
    }

    String peekLast() {
        return data.get(line).get(data.get(line).size() - 1);
    }

    String peekPrev(int p) {
        if (index - p < 0) return null;
        return data.get(line).get(index - p);
    }

    int lineLength() {
        return data.get(line).size();
    }

    boolean hasLine() {
        return line != data.size();
    }

    void nextLine() {
        index = 0;
        line++;
    }

}
