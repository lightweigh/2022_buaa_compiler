package frontend.grammar.decl;

import frontend.Lexer;
import frontend.grammar.def.VarDef;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class VarDecl extends Decl {
    //  VarDecl → BType VarDef { ',' VarDef } ';'
    private Token intTK;
    private ArrayList<VarDef> varDefs;
    private ArrayList<Token> seperators;
    private Token semicon;
    // private boolean hasComma = false;


    public VarDecl(Token intTK, ArrayList<VarDef> varDefs,
                   ArrayList<Token> seperators, Token semicon) {
        this.intTK = intTK;
        this.varDefs = varDefs;
        this.seperators = seperators;
        this.semicon = semicon;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(intTK.toString());
        Iterator<Token> iterSeperator = seperators.iterator();
        for (VarDef varDef : varDefs) {
            varDef.print(output);
            if (iterSeperator.hasNext()) {
                Token comma = iterSeperator.next();
                output.write(comma.toString());
            }
        }
        output.write(semicon.toString());
        output.write("<VarDecl>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("int ");
        Iterator<Token> iterSeperator = seperators.iterator();
        for (VarDef varDef : varDefs) {
            sb.append(varDef.toString());
            if (iterSeperator.hasNext()) {
                iterSeperator.next();
                sb.append(", ");
            }
        }
        sb.append(";");
        return sb.toString();
    }
}
