package frontend.grammar.decl.def;

import frontend.grammar.exp.ConstExp;
import frontend.grammar.exp.Exp;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Variable {
    // Variable → Ident { '[' ConstExp ']' }
    // const or non-const
    // a or a[0] or a[0][0]
    private Ident ident;
    private ArrayList<Token> bracks;
    private int dimension = 0;
    private ArrayList<ConstExp> constExps = new ArrayList<>();

    private HashMap<Integer, ArrayList<Exp>> initials = new HashMap<>();
    // a: initials.get(0).get(0)
    // a[i]: initials.get(0).get(i)
    // a[i][j]: initials.get(i).get(j)

    public Variable(Ident ident, ArrayList<Token> bracks,
                    int dimension, ArrayList<ConstExp> constExps) {
        this.ident = ident;
        this.bracks = bracks;
        this.dimension = dimension;
        this.constExps = constExps;
    }

    public Ident getIdent() {
        return ident;
    }

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public void print(BufferedWriter output) throws IOException {
        ident.print(output);
        for (int i = 0;i < getConstExps().size();i++) {
            output.write(bracks.get(2*i).toString());
            getConstExps().get(i).print(output);
            output.write(bracks.get(2*i+1).toString());
        }
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ident.getContent());
        for (int i = 0;i < getConstExps().size();i++) {
            sb.append("[");
            sb.append(constExps.get(i).toString());
            sb.append("]");
        }
        return sb.toString();
    }
}
