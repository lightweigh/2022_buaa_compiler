package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.Block;
import frontend.grammar.BlockItem;
import frontend.grammar.exp.condExp.Cond;
import frontend.parser.Parser;
import frontend.token.Token;

public class ForStmt extends Stmt {
    // 'for' '(' Stmt ';' Cond ';' Stmt ')' Stmt
    //  ->  stmt1; while (Cond) {stmt3; stmt2}
    private Token forTK;
    private Token lParent;
    private Stmt stmt1;
    private Token semicon1;
    private Cond cond;
    private Token semicon2;
    private Stmt stmt2;
    private Token rParent;
    private Stmt stmt3;

    public Token getForTK() {
        return forTK;
    }

    public Token getlParent() {
        return lParent;
    }

    public Stmt getStmt1() {
        return stmt1;
    }

    public Stmt getWhileStmt() {
        Block block = new Block(true);
        block.addBlockItems(new BlockItem(stmt3));
        block.addBlockItems(new BlockItem(stmt2));
        return new WhileStmt(lParent, cond, rParent, block);
    }

    public Token getSemicon1() {
        return semicon1;
    }

    public Cond getCond() {
        return cond;
    }

    public Token getSemicon2() {
        return semicon2;
    }

    public Stmt getStmt2() {
        return stmt2;
    }

    public Token getrParent() {
        return rParent;
    }

    public Stmt getStmt3() {
        return stmt3;
    }

    public void parser() {
        forTK = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        stmt1 = Stmt.stmtParser();
        // semicon1 = Lexer.tokenList.poll();
        cond = (Cond) Parser.condExpParser("Cond");
        semicon2 = Lexer.tokenList.poll();
        stmt2 = Stmt.stmtParser();
        rParent = Error.errorDetect(Token.Type.RPARENT);
        stmt3 = Stmt.stmtParser();
    }


}
