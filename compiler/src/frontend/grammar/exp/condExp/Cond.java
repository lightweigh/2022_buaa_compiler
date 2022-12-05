package frontend.grammar.exp.condExp;

import frontend.grammar.exp.condExp.LOrExp;

import java.io.BufferedWriter;
import java.io.IOException;

public class Cond implements CondExp {
    //  Cond → LOrExp
    private LOrExp lOrExp = null;

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    public LOrExp getlOrExp() {
        return lOrExp;
    }

    public void print(BufferedWriter output) throws IOException {
        lOrExp.print(output);
        output.write("<Cond>\n");
    }

    @Override
    public String toString() {
        return lOrExp.toString();
    }
}
