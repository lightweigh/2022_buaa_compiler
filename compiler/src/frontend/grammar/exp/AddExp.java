package frontend.grammar.exp;

import frontend.Lexer;
import frontend.grammar.exp.condExp.CondExp;
import frontend.grammar.exp.condExp.LAndExp;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class AddExp implements Expression, CondExp {
    //  AddExp → MulExp | AddExp ('+' | '−') MulExp
    private ArrayList<Expression> expressions = new ArrayList<>();
    private ArrayList<UnaryOp> unaryOps;

    public AddExp(ArrayList<Expression> expressions, ArrayList<UnaryOp> unaryOps) {
        this.expressions = expressions;
        this.unaryOps = unaryOps;
    }

    public void print(BufferedWriter output) throws IOException {
        Iterator<Expression> iter = expressions.iterator();
        Expression expression = iter.next();
        expression.print(output);
        for (UnaryOp unaryOp : unaryOps) {
            output.write("<AddExp\n>");
            unaryOp.print(output);
            expression = iter.next();
            expression.print(output);
        }
        output.write("<AddExp>\n");
    }
}
