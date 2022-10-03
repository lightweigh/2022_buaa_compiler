package frontend.grammar.exp.condExp;

import frontend.Lexer;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class LOrExp implements CondExp {
    //  LOrExp → LAndExp | LOrExp '||' LAndExp
    private ArrayList<Token> seperators;
    private ArrayList<LAndExp> lAndExps;    // linked by '||'

    public LOrExp(ArrayList<LAndExp> lAndExps, ArrayList<Token> seperators) {
        this.lAndExps = lAndExps;
        this.seperators = seperators;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        Iterator<LAndExp> iter = lAndExps.iterator();
        LAndExp lAndExp = iter.next();
        lAndExp.print(output);
        for (Token ortk : seperators) {
            output.write("<LOrExp>\n");
            output.write(ortk.toString());
            lAndExp = iter.next();
            lAndExp.print(output);
        }
        output.write("<LOrExp>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<LAndExp> iter = lAndExps.iterator();
        LAndExp lAndExp = iter.next();
        sb.append(lAndExp.toString());
        for (Token ortk : seperators) {
            sb.append(" || ");
            lAndExp = iter.next();
            sb.append(lAndExp.toString());
        }
        return sb.toString();
    }
}
