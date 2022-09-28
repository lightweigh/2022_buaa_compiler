package frontend.grammar.exp.condExp;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class CondOp implements CondExp {
    private Token condOp;

    public CondOp(Token condOp) {
        this.condOp = condOp;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(condOp.toString());
    }
}
