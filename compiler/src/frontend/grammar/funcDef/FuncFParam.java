package frontend.grammar.funcDef;

import frontend.grammar.exp.ConstExp;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncFParam {
    //  FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private Token intTK;
    private Ident ident;
    private int type = 0;
    private ArrayList<Token> bracks = null;
    private ConstExp constExp = null;


    public FuncFParam(Token intTK, Ident ident, int type,
                      ArrayList<Token> bracks, ConstExp constExp) {
        this.intTK = intTK;
        this.ident = ident;
        this.type = type;
        this.bracks = bracks;
        this.constExp = constExp;
    }

    public Token getIntTK() {
        return intTK;
    }

    public Ident getIdent() {
        return ident;
    }

    public int getType() {
        return type;
    }

    public ArrayList<Token> getBracks() {
        return bracks;
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(intTK.toString());
        output.write(ident.toString());
        if (type != 0) {
            int i = -1;
            output.write(bracks.get(++i).toString());
            if (type == 2) {
                output.write(bracks.get(++i).toString());
                output.write(bracks.get(++i).toString());
                constExp.print(output);
                // output.write(bracks.get(++i).toString());
            }
            output.write(bracks.get(++i).toString());
        }
        output.write("<FuncFParam>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("int ");
        sb.append(ident.getContent());
        if (type != 0) {
            int i = -1;
            sb.append("[");
            if (type == 2) {
                sb.append("][").append(constExp.toString());
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
