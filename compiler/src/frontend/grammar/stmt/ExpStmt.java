package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.LVal;
import frontend.grammar.exp.*;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExpStmt extends Stmt {
    // Stmt → [Exp] ';'
    private Exp exp = null;
    private Token semicon;

    public void parser(LVal lVal) {
        if (lVal == null) {
            exp = (Exp) Parser.expressionParser("Exp");
        } else {
            PrimaryExp primaryExp = new PrimaryExp(lVal);
            UnaryExp unaryExp = new UnaryExp(primaryExp);
            ArrayList<Expression> unaryExps = new ArrayList<>();
            unaryExps.add(unaryExp);
            ArrayList<Operator> separators = new ArrayList<>();
            MulExp mulExp = new MulExp(unaryExps,separators);
            ArrayList<Expression> mulExps = new ArrayList<>();
            mulExps.add(mulExp);
            AddExp addExp = new AddExp(mulExps,separators);
            exp = new Exp(addExp);
        }
        semicon = Error.errorDetect(Token.Type.SEMICN);
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        if (exp != null) {
            exp.print(output);
        }
        output.write(semicon.toString());
        super.print(output);
    }
}
