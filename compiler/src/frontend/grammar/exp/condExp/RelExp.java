package frontend.grammar.exp.condExp;

import frontend.Lexer;
import frontend.grammar.exp.AddExp;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class RelExp implements CondExp {
    //  RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private ArrayList<CondExp> condExps;
    private ArrayList<CondOp> condOps;

    public RelExp(ArrayList<CondExp> condExps, ArrayList<CondOp> condOps) {
        this.condExps = condExps;
        this.condOps = condOps;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        Iterator<CondExp> iter = condExps.iterator();
        CondExp condExp = iter.next();
        condExp.print(output);
        for (CondOp condOp : condOps) {
            output.write("<RelExp\n>");
            condOp.print(output);
            condExp = iter.next();
            condExp.print(output);
        }
        output.write("<RelExp\n>");
    }
}
