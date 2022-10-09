package frontend.grammar.decl.def;

import frontend.grammar.decl.def.init.Init;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class ConstDef {
    //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal

    /*public ConstDef(Variable variable, Token assign, Init constInitVal) {
        super(variable, assign, constInitVal);
    }

    public void print(BufferedWriter output) throws IOException {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        super.getVariable().print(output);
        output.write(super.getAssign().toString());
        super.getInit().print(output);
        output.write("<ConstDef>\n");
    }

    @Override
    public String toString() {
        return super.getVariable().toString() +
                " = " +
                super.getInit().toString();
    }*/
}
