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

    private boolean addrNotValue = false;

    public LVal(Ident ident, ArrayList<Token> bracks,
                int dimension, ArrayList<Exp> exps) {
        this.ident = ident;
        this.bracks = bracks;
        this.dimension = dimension;
        this.exps = exps;
    }

    public Ident getIdent() {
        return ident;
    }

    public ArrayList<Token> getBracks() {
        return bracks;
    }

    public int getDimension() {
        return dimension;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public boolean isAddrNotValue() {
        return addrNotValue;
    }

    public void setAddrNotValue(boolean addrNotValue) {
        this.addrNotValue = addrNotValue;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.getContent());
        for (Exp exp : exps) {
            sb.append("[").append(exp.toString()).append("]");
        }
        return sb.toString();
    }
}
