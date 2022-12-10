package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class BreakOrContinueStmt extends Stmt {
    //   'break' ';' | 'continue' ';'
    private Token breakOrContinue;
    private Token semicon;

    public void parser() {
        breakOrContinue = Lexer.tokenList.poll();
        semicon = Error.errorDetect(Token.Type.SEMICN);
    }

    public boolean isBreak() {
        return breakOrContinue.getRefType() == Token.Type.BREAKTK;
    }

    public int getRow() {
        return breakOrContinue.getRow();
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(breakOrContinue.toString());
        output.write(semicon.toString());
        super.print(output);
    }
}
