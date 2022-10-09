package frontend.grammar.exp;

import java.io.BufferedWriter;
import java.io.IOException;

public class Exp implements Expression {
    private AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    public void print(BufferedWriter output) throws IOException {
        addExp.print(output);
        output.write("<Exp>\n");
    }

    public AddExp getAddExp() {
        return addExp;
    }

    @Override
    public String toString() {
        return addExp.toString();
    }
}
