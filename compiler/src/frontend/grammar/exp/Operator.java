package frontend.grammar.exp;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class Operator implements Expression {
    // '*' | '/' | '%' | '+' | '-'
    private final Token op;

    public Operator(Token op) {
        this.op = op;
    }

    public String getOp() {
        return op.getContent();
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(op.toString());
    }

    @Override
    public String toString() {
        return op.getContent();
    }
}
