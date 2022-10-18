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

    public void parser() {
        exp = (Exp) Parser.expressionParser("Exp");
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
