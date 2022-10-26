package frontend.grammar.exp;

import frontend.Lexer;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MulExp implements Expression {
    //   MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private ArrayList<Expression> expressions = new ArrayList<>();
    private ArrayList<Operator> operators;

    public MulExp(ArrayList<Expression> expressions, ArrayList<Operator> operators) {
        this.expressions = expressions;
        this.operators = operators;
    }

    public ArrayList<Expression> getExpressions() {
        return expressions;
    }

    public Iterator<Expression> getExpIter() {
        return expressions.iterator();
    }

    public Iterator<Operator> getOpIter() {
        return operators.iterator();
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        Iterator<Expression> iter = expressions.iterator();
        Expression expression = iter.next();
        expression.print(output);
        for (Operator operator : operators) {
            output.write("<MulExp>\n");
            operator.print(output);
            expression = iter.next();
            expression.print(output);
        }
        output.write("<MulExp>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Expression> iter = expressions.iterator();
        Expression expression = iter.next();
        sb.append(expression.toString());
        for (Operator operator : operators) {
            sb.append(operator.toString());
            expression = iter.next();
            sb.append(expression.toString());
        }
        return sb.toString();
    }
}
