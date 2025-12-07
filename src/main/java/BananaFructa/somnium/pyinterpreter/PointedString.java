package BananaFructa.somnium.pyinterpreter;

public class PointedString {
    String data;
    int index;

    public PointedString(String data) {
        this.data = data;
        this.index = 0;
    }

    boolean hasNext() {
        return index != data.length();
    }

    boolean hasNext(int n) {
        return (index + n) < data.length();
    }

    char next() {
        return data.charAt(index++);
    }

    char peek() {
        return data.charAt(index);
    }

    char peek(int n) {
        return data.charAt(index + n);
    }

    String nextLine() {
        String line = "";
        char c;
        while(hasNext() && (c = next()) != '\n') line += c;
        return line;
    }

}
