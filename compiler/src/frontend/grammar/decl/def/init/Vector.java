package frontend.grammar.decl.def.init;

import frontend.grammar.exp.Expression;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Vector {
    // Vector := '{'Expression {',' Expression}'}'  | Expression
    // {1,2,3,...}
    // 2 维: a[1][1] = {a1[0]}
    private boolean isConst;
    private Token lBrace;
    private ArrayList<Expression> expressions;
    private ArrayList<Token> seperators;
    private Token rBrace;

    public Vector(Token lBrace, ArrayList<Expression> expressions,
                  ArrayList<Token> seperators, Token rBrace, String type) {
        this.lBrace = lBrace;
        this.expressions = expressions;
        this.seperators = seperators;
        this.rBrace = rBrace;
        this.isConst = type.equals("ConstExp");
    }

    public void print(BufferedWriter output) throws IOException {
        if (lBrace != null) {
            output.write(lBrace.toString());
        }
        Iterator<Token> iterSeperators = seperators.iterator();
        for (Expression expression : expressions) {
            expression.print(output);
            if (isConst) {
                output.write("<ConstInitVal>\n");
            } else {
                output.write("<InitVal>\n");
            }
            if (iterSeperators.hasNext()) {
                Token comma = iterSeperators.next();
                output.write(comma.toString());
            }
        }
        if (rBrace != null) {
            output.write(rBrace.toString());
            if (isConst) {
                output.write("<ConstInitVal>\n");
            } else {
                output.write("<InitVal>\n");
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<Token> iterSeperators = seperators.iterator();
        for (Expression expression : expressions) {
            sb.append(expression.toString());
            if (iterSeperators.hasNext()) {
                iterSeperators.next();
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
