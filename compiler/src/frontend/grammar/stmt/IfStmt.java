package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.exp.condExp.Cond;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class IfStmt extends Stmt {
    //   'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    private Token ifTK;
    private Token lParent;
    private Cond cond;
    private Token rParent;
    private Stmt stmt;
    private Token elseTK=null;
    private Stmt elseStmt=null;

    public void parser() {
        ifTK = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        cond = (Cond) Parser.condExpParser("Cond");
        rParent = Error.errorDetect(Token.Type.RPARENT);
        stmt = Stmt.stmtParser();
        if (Lexer.tokenList.equalPeekType(0, Token.Type.ELSETK)) {
            elseTK=Lexer.tokenList.poll();
            elseStmt = Stmt.stmtParser();
        }
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(ifTK.toString());
        output.write(lParent.toString());
        cond.print(output);
        output.write(rParent.toString());
        stmt.print(output); // todo check the child
        if (elseTK != null) {
            output.write(elseTK.toString());
            elseStmt.print(output);
        }
        super.print(output);
    }
}
