package frontend.grammar.exp.condExp;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class EqExp implements CondExp {
    //  EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private ArrayList<RelExp> relExps;
    private ArrayList<CondOp> seperators;

    public EqExp(ArrayList<RelExp> condExps, ArrayList<CondOp> seperators) {
        this.relExps = condExps;
        this.seperators = seperators;
    }

    public ArrayList<RelExp> getRelExps() {
        return relExps;
    }

    public ArrayList<CondOp> getCondOps() {
        return seperators;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        Iterator<RelExp> iter = relExps.iterator();
        RelExp condExp = iter.next();
        condExp.print(output);
        for (CondOp condOp : seperators) {
            output.write("<EqExp>\n");
            condOp.print(output);
            condExp = iter.next();
            condExp.print(output);
        }
        output.write("<EqExp>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<RelExp> iter = relExps.iterator();
        RelExp condExp = iter.next();
        sb.append(condExp);
        for (CondOp condOp : seperators) {
            sb.append(condOp.toString());
            condExp = iter.next();
            sb.append(condExp);
        }
        return sb.toString();
    }
}
