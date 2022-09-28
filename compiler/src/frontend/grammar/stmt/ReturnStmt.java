package frontend.grammar.stmt;

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
        if (!Lexer.tokenList.equalPeekType(0, Token.Type.SEMICN)) {
            // System.out.println("error"); todo
        } else {
            semicon = Lexer.tokenList.poll();
        }
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
