package frontend.grammar;

import frontend.grammar.exp.Exp;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class FuncRParams {
    // FuncRParams → Exp { ',' Exp }
    private ArrayList<Exp> exps;
    private ArrayList<Token> seperators;

    public FuncRParams(ArrayList<Exp> exps, ArrayList<Token> seperators) {
        this.exps = exps;
        this.seperators = seperators;
    }


    public void print(BufferedWriter output) throws IOException {
        Iterator<Exp> iter = exps.iterator();
        Exp exp = iter.next();
        exp.print(output);
        for (Token comma : seperators) {
            output.write(comma.toString());
            exp = iter.next();
            exp.print(output);
        }
        output.write("<FuncRParams>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Exp> iter = exps.iterator();
        Exp exp = iter.next();
        sb.append(exp.toString());
        for (Token comma : seperators) {
            exp = iter.next();
            sb.append(", ").append(exp.toString());
        }
        return sb.toString();
    }
}
