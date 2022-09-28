package frontend.grammar.stmt;

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
        if (!isSemicon(Lexer.tokenList.peek(0))) {
            System.out.println("Error occurs!");    //todo
        } else {
            semicon = Lexer.tokenList.poll();
        }
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(breakOrContinue.toString());
        output.write(semicon.toString());
        super.print(output);
    }
}
