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

    public String getOp() {
        return unaryOp.getContent();
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(unaryOp.toString());
        output.write("<UnaryOp>\n");
    }

    @Override
    public String toString() {
        Token.Type type = unaryOp.getRefType();
        return type == Token.Type.PLUS ? "+" : type == Token.Type.MINU ? "-" : "!";
    }
}
