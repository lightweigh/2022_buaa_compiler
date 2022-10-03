package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.exp.Exp;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class ExpStmt extends Stmt {
    // Stmt → [Exp] ';'
    private Exp exp = null;
    private Token semicon;

    public void parser() {
        if (!Lexer.tokenList.equalPeekType(0, Token.Type.SEMICN)) {
            exp = (Exp) Parser.expressionParser("Exp");
        }

        if (!Lexer.tokenList.equalPeekType(0, Token.Type.SEMICN)) {
            Error.errorDetect(';');
        } else {
            semicon = Lexer.tokenList.poll();
        }
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
