package frontend.grammar.exp;

import frontend.grammar.exp.condExp.CondExp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class AddExp implements Expression, CondExp {
    //  AddExp → MulExp | AddExp ('+' | '−') MulExp
    private ArrayList<Expression> expressions = new ArrayList<>();
    private ArrayList<Operator> operators;

    public AddExp(ArrayList<Expression> expressions, ArrayList<Operator> operators) {
        this.expressions = expressions;
        this.operators = operators;
    }

    public ArrayList<Expression> getExpressions() {
        return expressions;
    }

    public void print(BufferedWriter output) throws IOException {
        Iterator<Expression> iter = expressions.iterator();
        Expression expression = iter.next();
        expression.print(output);
        for (Operator operator : operators) {
            output.write("<AddExp>\n");
            operator.print(output);
            expression = iter.next();
            expression.print(output);
        }
        output.write("<AddExp>\n");
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
