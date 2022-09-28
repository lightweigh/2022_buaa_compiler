package frontend.grammar.def;

import frontend.Lexer;
import frontend.grammar.init.InitVal;
import frontend.grammar.exp.ConstExp;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class VarDef {
    //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private Variable variable;
    private boolean isInit = false;
    private Token assign;
    private InitVal initVal;

    public VarDef(Variable variable) {
        this.variable = variable;
        this.isInit = false;
    }

    public VarDef(Variable variable, Token assign, InitVal initVal) {
        this.variable = variable;
        this.isInit = true;
        this.assign = assign;
        this.initVal = initVal;
    }

    public void setAssign(Token assign) {
        this.assign = assign;
        isInit = true;
    }

    public void setInitVal(InitVal initVal) {
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
}
