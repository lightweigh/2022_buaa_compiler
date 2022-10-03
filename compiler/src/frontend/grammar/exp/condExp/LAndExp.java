package frontend.grammar.exp.condExp;

import frontend.Lexer;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class LAndExp implements CondExp {
    //   LAndExp → EqExp | LAndExp '&&' EqExp
    private ArrayList<EqExp> eqExps;    // linked by '&&'
    private ArrayList<Token> seperators;

    public LAndExp(ArrayList<EqExp> eqExps, ArrayList<Token> seperators) {
        this.eqExps = eqExps;
        this.seperators = seperators;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        Iterator<EqExp> iter = eqExps.iterator();
        EqExp eqExp = iter.next();
        eqExp.print(output);
        for (Token andtk : seperators) {
            output.write("<LAndExp>\n");
            output.write(andtk.toString());
            eqExp = iter.next();
            eqExp.print(output);
        }
        output.write("<LAndExp>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<EqExp> iter = eqExps.iterator();
        EqExp eqExp = iter.next();
        sb.append(eqExp.toString());
        for (Token andtk : seperators) {
            sb.append(" && ");
            eqExp = iter.next();
            sb.append(eqExp.toString());
        }

        return sb.toString();
    }
}
