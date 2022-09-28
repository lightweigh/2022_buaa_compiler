package frontend.grammar.def;

import frontend.grammar.init.ConstInitVal;
import frontend.grammar.exp.ConstExp;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ConstDef {
    //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Variable variable;
    private Token assign;
    private ConstInitVal constInitVal;

    public ConstDef(Variable variable, Token assign, ConstInitVal constInitVal) {
        this.variable = variable;
        this.assign = assign;
        this.constInitVal = constInitVal;
    }

    public void print(BufferedWriter output) throws IOException {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        variable.print(output);
        output.write(assign.toString());
        constInitVal.print(output);
        output.write("<ConstDef>\n");
    }
}
