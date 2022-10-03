package frontend.grammar.def;

import frontend.grammar.init.Init;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class ConstDef {
    //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Variable variable;
    private Token assign;
    private Init constInitVal;

    public ConstDef(Variable variable, Token assign, Init constInitVal) {
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

    @Override
    public String toString() {
        return variable.toString() +
                " = " +
                constInitVal.toString();
    }
}
