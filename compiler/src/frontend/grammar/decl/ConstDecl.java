package frontend.grammar.decl;

import frontend.grammar.decl.def.ConstDef;
import frontend.grammar.decl.def.Def;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ConstDecl {
    //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private Token constTK;
    private Token intTK;
    private ArrayList<Token> separators;
    private ArrayList<Def> constDefs;
    private Token semicon;

    /*public ConstDecl(Token constTK, Token intTK, ArrayList<Token> separators,
                     ArrayList<Def> constDefs, Token semicon) {
        super(constTK,intTK,separators,constDefs,semicon);

        this.constTK = constTK;
        this.intTK = intTK;
        this.separators = separators;
        this.constDefs = constDefs;
        this.semicon = semicon;
    }
*/
    public ArrayList<Def> getConstDefs() {
        return constDefs;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(constTK.toString());
        output.write(intTK.toString());
        Iterator<Token> iterSeperators = separators.iterator();
        for (int i = 0; i < constDefs.size(); i++) {
            constDefs.get(i).print(output);
            if (iterSeperators.hasNext()) {
                Token comma = iterSeperators.next();
                output.write(comma.toString());
            }
        }
        output.write(semicon.toString());
        output.write("<ConstDecl>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("const int ");
        Iterator<Token> iterSeperators = separators.iterator();
        for (Def constDef : constDefs) {
            sb.append(constDef.toString());
            if (iterSeperators.hasNext()) {
                iterSeperators.next();
                sb.append(", ");
            }
        }
        sb.append(";");
        return sb.toString();
    }
}
