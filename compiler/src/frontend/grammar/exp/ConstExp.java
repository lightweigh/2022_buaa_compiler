package frontend.grammar.exp;

import java.io.BufferedWriter;
import java.io.IOException;

public class ConstExp implements Expression {
    private AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public void print(BufferedWriter output) throws IOException {
        addExp.print(output);
        output.write("<ConstExp>\n");
    }

    @Override
    public String toString() {
        return addExp.toString();
    }
}
