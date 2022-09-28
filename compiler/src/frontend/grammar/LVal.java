package frontend.grammar;

import frontend.Lexer;
import frontend.grammar.exp.ConstExp;
import frontend.grammar.exp.Exp;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

// similar to Variable qwq\
public class LVal {
    //  LVal → Ident {'[' Exp ']'}

    private Ident ident;
    private ArrayList<Token> bracks;
    private int dimension = 0;
    private ArrayList<Exp> exps = new ArrayList<>();

    public LVal(Ident ident, ArrayList<Token> bracks,
                int dimension, ArrayList<Exp> exps) {
        this.ident = ident;
        this.bracks = bracks;
        this.dimension = dimension;
        this.exps = exps;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(ident.toString());
        for (int i = 0;i < exps.size();i++) {
            output.write(bracks.get(2*i).toString());
            exps.get(i).print(output);
            output.write(bracks.get(2*i+1).toString());
        }
        output.write("<LVal>\n");
    }
}
