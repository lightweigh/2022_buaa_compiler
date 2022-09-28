package frontend.grammar.stmt;

import frontend.Lexer;
import frontend.grammar.exp.condExp.Cond;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class WhileStmt extends Stmt {
    //  'while' '(' Cond ')' Stmt
    private Token whileTK;
    private Token lParent;
    private Cond cond;
    private Token rParent;
    private Stmt stmt;

    public void parser() {
        whileTK = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        cond= (Cond) Parser.condExpParser("Cond");
        rParent = Lexer.tokenList.poll();
        stmt = Stmt.stmtParser();
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(whileTK.toString());
        output.write(lParent.toString());
        cond.print(output);
        output.write(rParent.toString());
        stmt.print(output);
        super.print(output);
    }
}
