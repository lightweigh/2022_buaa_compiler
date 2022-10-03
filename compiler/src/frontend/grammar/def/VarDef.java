package frontend.grammar.def;

import frontend.grammar.init.Init;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class VarDef {
    //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private Variable variable;
    private boolean isInit = false;
    private Token assign;
    private Init initVal;

    public VarDef(Variable variable) {
        this.variable = variable;
        this.isInit = false;
    }

    public VarDef(Variable variable, Token assign, Init initVal) {
        this.variable = variable;
        this.isInit = true;
        this.assign = assign;
        this.initVal = initVal;
    }

    public void print(BufferedWriter output) throws IOException {
        variable.print(output);
        if (isInit) {
            output.write(assign.toString());
            initVal.print(output);
        }
        output.write("<VarDef>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(variable.toString());
        if (isInit) {
            sb.append(" = ").append(initVal.toString());
        }
        return sb.toString();
    }
}
