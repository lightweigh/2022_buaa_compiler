package frontend.grammar.init;

import frontend.grammar.exp.ConstExp;
import frontend.grammar.exp.Exp;
import frontend.grammar.exp.Expression;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Vector {
    // Vector := '{'Expression {',' Expression}'}'
    // {1,2,3,...}
    private Token lBrace;
    private ArrayList<Expression> expressions;
    private ArrayList<Token> seperators;
    private Token rBrace;

    public Vector(Token lBrace, ArrayList<Expression> expressions,
                  ArrayList<Token> seperators, Token rBrace) {
        this.lBrace = lBrace;
        this.expressions = expressions;
        this.seperators = seperators;
        this.rBrace = rBrace;
    }

    public void print(BufferedWriter output, boolean isConst) throws IOException {
        output.write(lBrace.toString() + '\n');
        Iterator<Token> iterSeperators = seperators.iterator();
        for (Expression expression : expressions) {
            if (isConst) {
                ((ConstExp) expression).print(output);
            } else {
                ((Exp) expression).print(output);
            }
            if (iterSeperators.hasNext()) {
                Token comma = iterSeperators.next();
                output.write(comma.toString() + '\n');
            }
        }
        output.write(rBrace.toString() + '\n');
    }
}
