package frontend.grammar.exp;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class UnaryOp implements Expression {
    // + | - | !
    private Token unaryOp;

    public UnaryOp(Token unaryOp) {
        this.unaryOp = unaryOp;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(unaryOp.toString());
    }
}
