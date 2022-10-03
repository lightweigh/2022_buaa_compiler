package frontend.grammar.exp.condExp;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class CondOp implements CondExp {
    // '==' | '!=' | '<' | '>' | '<=' | '>='
    private Token condOp;

    public CondOp(Token condOp) {
        this.condOp = condOp;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(condOp.toString());
    }

    @Override
    public String toString() {
        Token.Type type = condOp.getRefType();
        return " " + (type == Token.Type.EQL ? "==" :
                type == Token.Type.NEQ ? "!=" :
                        type == Token.Type.LSS ? "<" :
                                type == Token.Type.GRE ? ">" :
                                        type == Token.Type.LEQ ? "<=" :
                                                type == Token.Type.GEQ ? ">=" : "") + " ";
    }
}
