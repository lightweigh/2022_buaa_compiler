package frontend.grammar.stmt;

import frontend.Error;
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

    public WhileStmt() {
    }

    public WhileStmt(Token lParent, Cond cond, Token rParent, Stmt stmt) {
        this.lParent = lParent;
        this.cond = cond;
        this.rParent = rParent;
        this.stmt = stmt;
    }

    public Token getWhileTK() {
        return whileTK;
    }

    public Token getlParent() {
        return lParent;
    }

    public Cond getCond() {
        return cond;
    }

    public Token getrParent() {
        return rParent;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void parser() {
        whileTK = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        cond= (Cond) Parser.condExpParser("Cond");
        rParent = Error.errorDetect(Token.Type.RPARENT);
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
