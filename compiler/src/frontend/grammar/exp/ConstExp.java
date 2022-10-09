package frontend.grammar.exp;

import java.io.BufferedWriter;
import java.io.IOException;

public class ConstExp implements Expression {
    private AddExp addExp;
    private Integer constValue;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public Integer getConstValue() {
        return constValue;
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
