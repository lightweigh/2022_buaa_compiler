package frontend.grammar.decl.def;

import frontend.grammar.decl.def.init.Init;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class Def {
    //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal

    //  Def → Variable [ '=' InitVal ]
    private Variable variable;
    private Token assign;
    private Init init;
    private boolean isConst;

    public Def(Variable variable, Token assign, Init init, boolean isConst) {
        this.variable = variable;
        this.assign = assign;
        this.init = init;
        this.isConst = isConst;
    }

    public Variable getVariable() {
        return variable;
    }

    public Token getAssign() {
        return assign;
    }

    public Init getInit() {
        return init;
    }

    public int getRow() {
        if (assign != null) {
            // return init
        }
        return  -1;
    }

    /*public Ident getIdent() {
        return variable.getIdent();
    }*/

    public int getDimension() {
        return variable.getDimension();
    }

    public void print(BufferedWriter output) throws IOException {
        variable.print(output);
        if (init != null) {
            assign.print(output);
            init.print(output);
        }
        if (isConst) {
            output.write("<ConstDef>\n");
        } else {
            output.write("<VarDef>\n");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isConst) {
            sb.append("const ");
        }
        sb.append(variable.toString());
        if (init != null) {
            sb.append(" = ").append(init.toString());
        }
        return sb.toString();
    }
}
