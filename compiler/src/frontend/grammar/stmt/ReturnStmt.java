package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.exp.Exp;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class ReturnStmt extends Stmt {
    //  'return' [Exp] ';'
    private Token returnTK;
    private Exp exp = null;
    private Token semicon;

    public void parser() {
        returnTK = Lexer.tokenList.poll();
        exp = (Exp) Parser.expressionParser("Exp");
        semicon = Error.errorDetect(Token.Type.SEMICN);
    }

    public boolean hasExp() {
        return exp != null;
    }

    public int getRetRow() {
        return returnTK.getRow();
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(returnTK.toString());
        if (exp != null) {
            exp.print(output);
        }
        output.write(semicon.toString());
        super.print(output);
    }
}
