package frontend.grammar.decl;

import frontend.grammar.Component;
import frontend.grammar.decl.def.Def;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Decl implements Component {
    //  VarDecl → BType VarDef { ',' VarDef } ';'
    //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'

    //  Decl → {'const'} BType Def { ',' Def } ';'
    private Token constTK;
    private Token intTK;
    private ArrayList<Token> separators;
    private ArrayList<Def> defs;
    private Token semicon;

    public Decl(Token constTK, Token intTK,
                ArrayList<Token> separators, ArrayList<Def> defs, Token semicon) {
        this.constTK = constTK;
        this.intTK = intTK;
        this.separators = separators;
        this.defs = defs;
        this.semicon = semicon;
    }

    public boolean isConst() {
        return constTK != null;
    }

    public boolean hasSemicon() {
        return semicon != null;
    }

    public ArrayList<Def> getDefs() {
        return defs;
    }

    public void print(BufferedWriter output) throws IOException {
        // constDecl
        if (isConst()) {
            constTK.print(output);
        }
        intTK.print(output);
        Iterator<Token> iterSeperators = separators.iterator();
        for (Def def : defs) {
            def.print(output);
            if (iterSeperators.hasNext()) {
                Token comma = iterSeperators.next();
                comma.print(output);
            }
        }
        semicon.print(output);
        if (isConst()) {
            output.write("<ConstDecl>\n");
        } else {
            output.write("<VarDecl>\n");
        }

    }

    @Override
    public String toString() {
        // constDecl
        StringBuilder sb = new StringBuilder();
        if (isConst()) {
            sb.append("const ");
        }
        sb.append("int ");
        Iterator<Token> iterSeperators = separators.iterator();
        for (Def def : defs) {
            sb.append(def.toString());
            if (iterSeperators.hasNext()) {
                iterSeperators.next();
                sb.append(", ");
            }
        }
        sb.append(";");
        return sb.toString();
    }
}
