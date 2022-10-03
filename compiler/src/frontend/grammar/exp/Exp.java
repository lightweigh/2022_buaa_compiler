package frontend.grammar.exp;

import java.io.BufferedWriter;
import java.io.IOException;

public class Exp implements Expression {
    private AddExp addExp;

    public void setAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public void print(BufferedWriter output) throws IOException {
        addExp.print(output);
        output.write("<Exp>\n");
    }

    @Override
    public String toString() {
        return addExp.toString();
    }
}
