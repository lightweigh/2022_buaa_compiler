package frontend.grammar.exp;

import java.io.BufferedWriter;
import java.io.IOException;

public class ConstExp implements Expression {
    private AddExp addExp = null;
    private Integer constValue;

    public Integer getConstValue() {
        return constValue;
    }

    public void setAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public void print(BufferedWriter output) throws IOException {
        addExp.print(output);
    }
}
