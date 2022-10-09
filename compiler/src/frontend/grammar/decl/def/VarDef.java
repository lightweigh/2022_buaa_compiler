package frontend.grammar.decl.def;

import frontend.grammar.decl.def.init.Init;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class VarDef {
    //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private boolean isInit;

    /*public VarDef(Variable variable) {
        super(variable, null, null);
        this.isInit = false;
    }

    public VarDef(Variable variable, Token assign, Init initVal) {
        super(variable, assign, initVal);
        this.isInit = true;
    }*/

    public void print(BufferedWriter output) throws IOException {
        // super.getVariable().print(output);
        if (isInit) {
            // output.write(super.getAssign().toString());
            // super.getInit().print(output);
        }
        output.write("<VarDef>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append(super.getVariable().toString());
        if (isInit) {
            // sb.append(" = ").append(super.getInit().toString());
        }
        return sb.toString();
    }
}
